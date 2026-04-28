package pe.idat.BackEndConecta.service.impl;

import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pe.idat.BackEndConecta.entity.Contrato;
import pe.idat.BackEndConecta.entity.HistorialSuspension;
import pe.idat.BackEndConecta.entity.Recibo;
import pe.idat.BackEndConecta.entity.enums.EstadoContrato;
import pe.idat.BackEndConecta.entity.enums.EstadoPago;
import pe.idat.BackEndConecta.repository.HistorialSuspensionRepository;
import pe.idat.BackEndConecta.repository.ReciboRepository;
import pe.idat.BackEndConecta.service.SuspensionService;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class SuspensionServiceImpl implements SuspensionService {

    private static final Logger log = LoggerFactory.getLogger(SuspensionServiceImpl.class);

    private final ReciboRepository reciboRepository;
    private final HistorialSuspensionRepository historialSuspensionRepository;

    @Override
    @Transactional
    public Map<String, Object> procesarSuspensionesPorMora(LocalDate fechaEjecucion) {

        // 1. Buscar recibos PENDIENTES cuya fecha de vencimiento ya pasó respecto a
        // fechaEjecucion
        List<Recibo> recibosVencidos = reciboRepository.findByEstadoPagoAndFechaVencimientoBefore(
                EstadoPago.PENDIENTE, fechaEjecucion);

        int suspensionesAplicadas = 0;

        // 2. Iterar sobre todos los recibos morosos encontrados
        for (Recibo recibo : recibosVencidos) {

            // a) Cambiar estado de recibo
            recibo.setEstadoPago(EstadoPago.VENCIDO);

            // b) Evaluar y suspender Contrato (Solo si es ACTIVO)
            Contrato contrato = recibo.getContrato();

            if (contrato.getEstado() == EstadoContrato.ACTIVO) {
                // b.1) Marcar como suspendido
                contrato.setEstado(EstadoContrato.SUSPENDIDO);

                // b.2) Iniciar Historial de Suspensión
                HistorialSuspension nuevoHistorial = HistorialSuspension.builder()
                        .contrato(contrato)
                        .fechaSuspension(LocalDateTime.now())
                        // fechaReactivacion nace null nativamente
                        .diasSuspendidos(0)
                        // aplicadoEnRecibo null
                        .build();

                historialSuspensionRepository.save(nuevoHistorial);
                suspensionesAplicadas++;
            }
            // Todo se gestiona bajo el Cascade/Transactional, pero explicitamos saves si se
            // requiere flush exacto,
            // En métodos Batch se recomienda saveAll o confiar en transaccionalidad
        }

        // Si usamos dirty-checking con @Transactional, no requerimos .saveAll(),
        // pero .saveAll() optimiza lotes explícitos.
        // Spring Data/Hibernate trackeará las mutaciones a "recibos" y mutaciones a su
        // "contrato".

        return Map.of(
                "mensaje", "Job de Suspensión ejecutado con éxito.",
                "recibosMarcadosComoVencidos", recibosVencidos.size(),
                "contratosSuspendidos", suspensionesAplicadas);
    }

    // Cron Job: Ejecuta cada día a las 2:00 AM (Timezone de la JVM)
    @Scheduled(cron = "0 0 2 * * ?")
    public void jobSuspensionAutomática() {
        log.info("Iniciando CRON JOB de Suspensiones Automáticas AM 2:00...");
        Map<String, Object> resultado = procesarSuspensionesPorMora(LocalDate.now());
        log.info("CRON JOB FINALIZADO => {}", resultado);
    }
}
