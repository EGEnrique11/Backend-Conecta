package pe.idat.BackEndConecta.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pe.idat.BackEndConecta.dto.PagoRequestDTO;
import pe.idat.BackEndConecta.entity.*;
import pe.idat.BackEndConecta.entity.enums.EstadoContrato;
import pe.idat.BackEndConecta.entity.enums.EstadoPago;
import pe.idat.BackEndConecta.repository.*;
import pe.idat.BackEndConecta.service.PagoService;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class PagoServiceImpl implements PagoService {

    private final ReciboRepository reciboRepository;
    private final PagoRepository pagoRepository;
    private final SaldoFavorRepository saldoFavorRepository;
    private final ContratoRepository contratoRepository;
    private final HistorialSuspensionRepository historialSuspensionRepository;

    @Override
    @Transactional
    public Map<String, Object> registrarPago(PagoRequestDTO dto) {

        // 1. Validación Inicial
        Recibo recibo = reciboRepository.findById(dto.getReciboId())
                .orElseThrow(() -> new IllegalArgumentException("Recibo no encontrado."));

        if (recibo.getEstadoPago() == EstadoPago.PAGADO || recibo.getEstadoPago() == EstadoPago.ANULADO) {
            throw new IllegalArgumentException("No se puede pagar este recibo. Estado actual: " + recibo.getEstadoPago());
        }

        // 2. Validación de Monto
        BigDecimal montoTotalRecibo = recibo.getMontoTotal();
        if (dto.getMontoPagado().compareTo(montoTotalRecibo) < 0) {
            throw new IllegalArgumentException("El monto pagado (" + dto.getMontoPagado() + 
                    ") es inferior al monto total del recibo (" + montoTotalRecibo + "). No se permiten pagos parciales.");
        }

        // 3. Actualización del Recibo
        recibo.setEstadoPago(EstadoPago.PAGADO);

        // 4. Generación de Saldo a Favor (Excedentes)
        BigDecimal excedente = dto.getMontoPagado().subtract(montoTotalRecibo);
        boolean generoSaldoAFavor = false;
        if (excedente.compareTo(BigDecimal.ZERO) > 0) {
            SaldoFavor nuevoSaldo = SaldoFavor.builder()
                    .contrato(recibo.getContrato())
                    .montoOriginal(excedente)
                    .montoDisponible(excedente)
                    .origen("PAGO_EXCEDENTE")
                    .build(); // estado = ACTIVO es default
            saldoFavorRepository.save(nuevoSaldo);
            generoSaldoAFavor = true;
        }

        // 5. Reactivación de Servicio (Si aplica)
        Contrato contrato = recibo.getContrato();
        boolean contratoReactivado = false;
        if (contrato.getEstado() == EstadoContrato.SUSPENDIDO) {
            contrato.setEstado(EstadoContrato.ACTIVO);
            contratoRepository.save(contrato);
            contratoReactivado = true;

            // Cortar la suspensión en el historial
            List<HistorialSuspension> suspensionesActivas = historialSuspensionRepository
                    .findByContratoIdAndFechaReactivacionIsNull(contrato.getId());
            
            if (!suspensionesActivas.isEmpty()) {
                LocalDateTime ahora = LocalDateTime.now();
                for (HistorialSuspension hs : suspensionesActivas) {
                    hs.setFechaReactivacion(ahora);
                    // Opcionalmente se puede recalcular dias_suspendidos aquí para el futuro
                }
                historialSuspensionRepository.saveAll(suspensionesActivas);
            }
        }

        // 6. Persistencia del Pago
        Empleado cajero = new Empleado();
        cajero.setId(dto.getEmpleadoRegistroId());

        Pago pago = Pago.builder()
                .recibo(recibo)
                .fechaPago(LocalDateTime.now())
                .montoPagado(dto.getMontoPagado())
                .metodoPago(dto.getMetodoPago())
                .nroOperacion(dto.getNroOperacion())
                .observaciones(dto.getObservaciones())
                .empleadoRegistro(cajero)
                .build();

        pagoRepository.save(pago);
        reciboRepository.save(recibo); // Flush explícito

        // Mensaje dinámico de respuesta
        String mensaje = "Pago registrado exitosamente. Recibo PAGADO.";
        if (generoSaldoAFavor) {
            mensaje += " Se ha generado un saldo a favor de S/ " + excedente + " por el excedente abonado.";
        }
        if (contratoReactivado) {
            mensaje += " El servicio ha sido reactivado automáticamente.";
        }

        return Map.of(
                "pagoId", pago.getId(),
                "mensaje", mensaje,
                "saldoGenerado", excedente
        );
    }
}
