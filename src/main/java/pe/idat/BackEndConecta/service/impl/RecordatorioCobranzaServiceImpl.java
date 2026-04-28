package pe.idat.BackEndConecta.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import pe.idat.BackEndConecta.entity.Recibo;
import pe.idat.BackEndConecta.entity.enums.EstadoPago;
import pe.idat.BackEndConecta.repository.ReciboRepository;
import pe.idat.BackEndConecta.service.NotificacionService;
import pe.idat.BackEndConecta.service.RecordatorioCobranzaService;

import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class RecordatorioCobranzaServiceImpl implements RecordatorioCobranzaService {

    private final ReciboRepository reciboRepository;
    private final NotificacionService notificacionService;
    
    private final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    private void procesarRecordatorioPreventivo() {
        LocalDate fechaObjetivo = LocalDate.now().plusDays(1);
        List<Recibo> recibos = reciboRepository.findByEstadoPagoAndFechaVencimiento(EstadoPago.PENDIENTE, fechaObjetivo);
        
        int cont = 0;
        for (Recibo recibo : recibos) {
            String montoFormat = recibo.getMontoTotal().setScale(2, RoundingMode.HALF_UP).toString();
            String fechaFormat = fechaObjetivo.format(formatter);
            
            String html = String.format("<p>Tu recibo por <strong>S/%s</strong> vence ma&ntilde;ana <strong>%s</strong>. Evita cortes de servicio y mantente conectado.</p>", 
                    montoFormat, fechaFormat);
            
            notificacionService.enviarCorreoAsincrono(recibo.getContrato().getCliente(), 
                    "RECORDATORIO_PREVENTIVO", 
                    "Aviso de Vencimiento Próximo", 
                    html);
            cont++;
        }
        
        log.info("Dunning Preventivo Finalizado: {} correos de aviso emitidos en base {}.", cont, fechaObjetivo);
    }

    private void procesarAvisoMora() {
        LocalDate fechaFin = LocalDate.now().minusDays(1);
        LocalDate fechaInicio = LocalDate.now().minusDays(4);
        
        List<Recibo> recibos = reciboRepository.findByEstadoPagoAndFechaVencimientoBetween(EstadoPago.VENCIDO, fechaInicio, fechaFin);

        int cont = 0;
        for (Recibo recibo : recibos) {
            String montoFormat = recibo.getMontoTotal().setScale(2, RoundingMode.HALF_UP).toString();
            String fechaFormat = recibo.getFechaVencimiento().format(formatter);
            
            String html = String.format("<p>Tu recibo por <strong>S/%s</strong> se encuentra <span style='color:red;'>VENCIDO</span> desde el <strong>%s</strong>. Tu servicio ha sido suspendido. Por favor, regulariza tu pago para gestionar la reactivaci&oacute;n autom&aacute;tica.</p>", 
                    montoFormat, fechaFormat);
            
            notificacionService.enviarCorreoAsincrono(recibo.getContrato().getCliente(), 
                    "AVISO_MORA", 
                    "URGENTE: Recibo Vencido y Servicio Suspendido", 
                    html);
            cont++;
        }
        
        log.info("Dunning de Mora Finalizado: {} correos de contingencia emitidos.", cont);
    }

    @Override
    @Scheduled(cron = "0 0 9 * * ?") // Todos los días a las 9:00 AM
    public void enviarRecordatoriosGenerales() {
        log.info("--- Iniciando JOB de Dunning (Cobranza Automática) 09:00 AM ---");
        procesarRecordatorioPreventivo();
        procesarAvisoMora();
        log.info("--- JOB de Dunning finalizado con éxito ---");
    }
}
