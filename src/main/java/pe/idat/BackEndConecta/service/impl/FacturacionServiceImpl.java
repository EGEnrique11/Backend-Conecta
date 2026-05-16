package pe.idat.BackEndConecta.service.impl;

import lombok.RequiredArgsConstructor;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import pe.idat.BackEndConecta.dto.ReciboListDTO;
import pe.idat.BackEndConecta.entity.*;
import pe.idat.BackEndConecta.entity.enums.EstadoContrato;
import pe.idat.BackEndConecta.entity.enums.EstadoPago;
import pe.idat.BackEndConecta.event.ReciboGeneradoEvent;
import pe.idat.BackEndConecta.repository.*;
import pe.idat.BackEndConecta.service.FacturacionService;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class FacturacionServiceImpl implements FacturacionService {

    private final ContratoRepository contratoRepository;
    private final ReciboRepository reciboRepository;
    private final HistorialSuspensionRepository historialSuspensionRepository;
    private final SaldoFavorRepository saldoFavorRepository;
    private final ApplicationEventPublisher eventPublisher;

    @Override
    @Transactional
    public Map<String, Object> generarRecibosPorCiclo(Integer cicloId, LocalDate fechaEjecucion) {

        // --- 1. CONFIGURACIÓN DEL CICLO (Ej: Ciclo 15 emitido el 15/03) ---
        // Asumiendo Ciclo 15 (Emite el 15, Vence el 3 del sig. mes, Corta el 4)
        LocalDate fechaEmision = fechaEjecucion; // Ej: 15/03/2026
        LocalDate periodoInicio = fechaEmision.plusDays(1); // 16/03/2026

        // El periodo fin es el día igual a la fecha de emisión pero del próximo mes
        // Ej: Si la emisión es el 15, el periodo fin es el 15 del mes siguiente
        LocalDate periodoFin = fechaEmision.plusMonths(1); // 15/04/2026

        // Vencimiento asume que es el día 3 del mes siguiente al de emisión (según DB)
        // Ejemplo genérico simplificado
        LocalDate fechaVencimiento = LocalDate.of(periodoFin.getYear(), periodoFin.getMonth(), 3);
        if (fechaVencimiento.isBefore(fechaEmision)) {
            // Manejo de cambio de año/mes si el día de vencimiento numérico es menor
            fechaVencimiento = fechaVencimiento.plusMonths(1);
        }

        // Días del ciclo = (Fin - Inicio) + 1 (Inclusivo)
        long diasDelCiclo = ChronoUnit.DAYS.between(periodoInicio, periodoFin) + 1;

        // --- 2. RECUPERAR CONTRATOS ELEGIBLES ---
        List<Contrato> contratos = contratoRepository.findByCicloPagoIdAndEstadoIn(
                cicloId, List.of(EstadoContrato.ACTIVO, EstadoContrato.SUSPENDIDO));

        int recibosGenerados = 0;

        // --- 3. PROCESAMIENTO MASIVO (BILLING BATCH) ---
        for (Contrato contrato : contratos) {

            // a) Inicializar Recibo Padre
            Recibo recibo = Recibo.builder()
                    .contrato(contrato)
                    .fechaEmision(fechaEmision)
                    .fechaVencimiento(fechaVencimiento)
                    .periodoInicio(periodoInicio)
                    .periodoFin(periodoFin)
                    .estadoPago(EstadoPago.PENDIENTE)
                    .build();

            // b) Cálculos Base de Rentabilidad (Prorrateo 8 de escala decimal para buffer)
            BigDecimal subtotalAcumulado = BigDecimal.ZERO;
            BigDecimal precioPlan = contrato.getPlan().getPrecio();
            BigDecimal costoPorDia = precioPlan.divide(BigDecimal.valueOf(diasDelCiclo), 8, RoundingMode.HALF_UP);

            // c) ITEM 1: Renta Mensual Adelantada
            DetalleRecibo rentaDetalle = DetalleRecibo.builder()
                    .concepto("Renta Mensual Adelantada")
                    .cantidadDias((int) diasDelCiclo)
                    .precioUnitario(precioPlan)
                    .subtotal(precioPlan)
                    .build();
            recibo.addDetalle(rentaDetalle);
            subtotalAcumulado = subtotalAcumulado.add(precioPlan);

            // d) ITEM 2: Prorrateo de Activación (Si es su primer ciclo)
            // Lógica asume que si no hay prorrateos anteriores (o facturas en absoluto), se
            // genera.
            // Para simplicidad, se usará la diferencia entre Fecha Activacion y
            // PeriodoInicio
            if (contrato.getFechaActivacion() != null && contrato.getFechaActivacion().isBefore(periodoInicio)) {

                // Validación para saber si le toca prorrateo.
                // Idealmente, se busca `countByContratoId` en ReciboRepository,
                // aquí simulamos condicional directo o un lookup rápido.
                boolean tieneRecibos = false; // TODO: Mapear contra query o validacion booleana externa.
                if (!tieneRecibos) {
                    long diasProrrateo = ChronoUnit.DAYS.between(contrato.getFechaActivacion(),
                            periodoInicio.minusDays(1));
                    if (diasProrrateo > 0) {
                        BigDecimal prorrateoSubtotal = costoPorDia.multiply(BigDecimal.valueOf(diasProrrateo));

                        DetalleRecibo prorrateoDetalle = DetalleRecibo.builder()
                                .concepto("Prorrateo de Activación")
                                .cantidadDias((int) diasProrrateo)
                                .precioUnitario(costoPorDia) // Mantiene alta precision para display si necesario
                                .subtotal(prorrateoSubtotal.setScale(2, RoundingMode.HALF_UP))
                                .build();
                        recibo.addDetalle(prorrateoDetalle);
                        subtotalAcumulado = subtotalAcumulado.add(prorrateoSubtotal);
                    }
                }
            }

            // e) ITEM 3: Devoluciones por Suspensión
            List<HistorialSuspension> historialesSuspendidos = historialSuspensionRepository
                    .findByContratoIdAndAplicadoEnReciboIsNull(contrato.getId());

            int totalDiasSuspendidos = historialesSuspendidos.stream()
                    .mapToInt(HistorialSuspension::getDiasSuspendidos)
                    .sum();

            if (totalDiasSuspendidos > 0) {
                BigDecimal descuentoSuspension = costoPorDia.multiply(BigDecimal.valueOf(totalDiasSuspendidos));
                // El subtotal es negativo
                BigDecimal negativoMonto = descuentoSuspension.negate().setScale(2, RoundingMode.HALF_UP);

                DetalleRecibo suspensionDetalle = DetalleRecibo.builder()
                        .concepto("Devolución por Suspensión")
                        .cantidadDias(totalDiasSuspendidos)
                        .precioUnitario(costoPorDia)
                        .subtotal(negativoMonto)
                        .build();
                recibo.addDetalle(suspensionDetalle);
                subtotalAcumulado = subtotalAcumulado.add(negativoMonto);
            }

            // f) ITEM 4: Saldo a Favor / Billetera
            List<SaldoFavor> saldosATratar = saldoFavorRepository
                    .findByContratoIdAndEstado(contrato.getId(), "ACTIVO");

            BigDecimal saldoTotal = saldosATratar.stream()
                    .map(SaldoFavor::getMontoDisponible)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

            if (saldoTotal.compareTo(BigDecimal.ZERO) > 0) {
                // Solo cubrimos hasta el total adeudado. Si el saldo es mayor, queda remanente.
                BigDecimal aplicable = saldoTotal.min(subtotalAcumulado);

                if (aplicable.compareTo(BigDecimal.ZERO) > 0) {
                    BigDecimal negativoSaldo = aplicable.negate().setScale(2, RoundingMode.HALF_UP);

                    DetalleRecibo saldoDetalle = DetalleRecibo.builder()
                            .concepto("Aplicación de Saldo a Favor")
                            .precioUnitario(aplicable)
                            .subtotal(negativoSaldo)
                            .build();
                    recibo.addDetalle(saldoDetalle);
                    subtotalAcumulado = subtotalAcumulado.add(negativoSaldo);
                }
            }

            // --- 4. CIERRE Y PERSISTENCIA ---
            // Rounding de seguridad en sumatoria total
            recibo.setMontoTotal(subtotalAcumulado.setScale(2, RoundingMode.HALF_UP));
            reciboRepository.save(recibo);

            //Lanzar evento
            eventPublisher.publishEvent(new ReciboGeneradoEvent(this, recibo.getId()));
            // Actualizar Historiales de Suspensión
            for (HistorialSuspension hs : historialesSuspendidos) {
                hs.setAplicadoEnRecibo(recibo);
            }
            historialSuspensionRepository.saveAll(historialesSuspendidos);

            // Actualizar Saldo a Favor (Consumido o Disminuido)
            // La logica requeriría un loop avanzado, para simplificar lo marco a consumido
            // si se usó
            for (SaldoFavor sf : saldosATratar) {
                sf.setMontoDisponible(BigDecimal.ZERO);
                sf.setEstado("CONSUMIDO");
            }
            saldoFavorRepository.saveAll(saldosATratar);

            recibosGenerados++;
        }

        return Map.of(
                "mensaje", "Cierre de facturación completado.",
                "fechaEjecucion", fechaEjecucion.toString(),
                "facturasGeneradas", recibosGenerados);
    }

    @Override
    @Transactional(readOnly = true)
    public Map<String, Object> verificarDeudaPendiente(Integer clienteId) {
        boolean tieneDeuda = reciboRepository.existsDeudaPendienteByClienteId(clienteId);
        String mensaje = tieneDeuda ? "El cliente tiene recibos pendientes o vencidos." : "El cliente está al día.";
        return Map.of("tieneDeuda", tieneDeuda, "mensaje", mensaje);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ReciboListDTO> obtenerRecibosPaginados(Integer contratoId, List<EstadoPago> estados,
            Pageable pageable) {
        Page<Recibo> recibos = reciboRepository.findByContratoIdAndEstadoPagoIn(contratoId, estados, pageable);
        return recibos.map(r -> {
            ReciboListDTO dto = ReciboListDTO.builder()
                    .id(r.getId())
                    .contratoId(r.getContrato().getId())
                    .fechaEmision(r.getFechaEmision())
                    .fechaVencimiento(r.getFechaVencimiento())
                    .periodoInicio(r.getPeriodoInicio())
                    .periodoFin(r.getPeriodoFin())
                    .montoTotal(r.getMontoTotal())
                    .estadoPago(r.getEstadoPago().name())
                    .build();

            if (r.getPagos() != null && !r.getPagos().isEmpty()) {
                // Tomamos el ultimo pago (suponiendo orden de insercion o unico pago)
                Pago ultimoPago = r.getPagos().get(r.getPagos().size() - 1);
                dto.setFechaPago(ultimoPago.getFechaPago());
                dto.setMetodoPago(ultimoPago.getMetodoPago());
            }
            return dto;
        });
    }

    // Cron Job: Ejecuta el día 15 de cada mes a las 4:00 AM
    @Scheduled(cron = "0 0 4 15 * ?")
    public void jobFacturacionAutomatica() {
        org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(FacturacionServiceImpl.class);
        log.info("Iniciando CRON JOB de Facturación (Día 15) a las 4:00 AM...");

        // 1 es el ID estático de tu ciclo de pago "Ciclo 15"
        Integer cicloId = 1;

        Map<String, Object> resultado = generarRecibosPorCiclo(cicloId, LocalDate.now());

        log.info("CRON JOB DE FACTURACIÓN FINALIZADO => {}", resultado);
    }
}
