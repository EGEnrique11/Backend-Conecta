package pe.idat.BackEndConecta.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import pe.idat.BackEndConecta.entity.Cliente;
import pe.idat.BackEndConecta.entity.Contrato;
import pe.idat.BackEndConecta.entity.Notificacion;
import pe.idat.BackEndConecta.entity.Recibo;
import pe.idat.BackEndConecta.repository.ContratoRepository;
import pe.idat.BackEndConecta.repository.NotificacionRepository;
import pe.idat.BackEndConecta.repository.ReciboRepository;
import pe.idat.BackEndConecta.service.EmailService;
import pe.idat.BackEndConecta.service.NotificacionService;
import pe.idat.BackEndConecta.service.PdfService;

import java.time.LocalDateTime;
import java.util.concurrent.CompletableFuture;

@Service
@RequiredArgsConstructor
@Slf4j
public class NotificacionServiceImpl implements NotificacionService {
        private final ContratoRepository contratoRepository;
        private final ReciboRepository reciboRepository;
        private final PdfService pdfService;
        private final EmailService emailService;

        private final NotificacionRepository notificacionRepository;

        @Override
        @Async
        @Transactional
        public CompletableFuture<Void> enviarContratoAsync(Integer contratoId) {
                return procesarEnvioContrato(contratoId, null);
        }

        @Override
        @Async
        @Transactional
        public CompletableFuture<Void> enviarContratoManualAsync(Integer contradoId, String correoDestino) {
                return procesarEnvioContrato(contradoId, correoDestino);
        }

        @Override
        @Async
        @Transactional
        public CompletableFuture<Void> enviarReciboMensualAsync(Integer reciboId) {
                return procesarEnvioRecibo(reciboId, null);
        }

        @Override
        @Async
        @Transactional
        public CompletableFuture<Void> enviarReciboMensualManualAsync(Integer reciboId, String correoDestino) {
                return procesarEnvioRecibo(reciboId, correoDestino);
        }

        @Override
        @Async
        public CompletableFuture<Void> enviarRecordatorioPreventivoAsync(Integer reciboId) {
                String asunto = "Aviso de Vencimiento Próximo - CONECTA";
                String plantillaHtml = "<p>Tu recibo por <strong>S/%s</strong> vence ma&ntilde;ana <strong>%s</strong>. Evita cortes de servicio y mantente conectado.</p>";
                return procesarAlertaCobranza(reciboId, "RECORDATORIO_PREVENTIVO", asunto, plantillaHtml);
        }

        @Override
        @Async
        public CompletableFuture<Void> enviarAvisoMoraAsync(Integer reciboId) {
                String asunto = "URGENTE: Recibo Vencido y Servicio Suspendido";
                String plantillaHtml = "<p>Tu recibo por <strong>S/%s</strong> se encuentra <span style='color:red;'>VENCIDO</span> desde el <strong>%s</strong>. Tu servicio ha sido suspendido. Por favor, regulariza tu pago para gestionar la reactivaci&oacute;n autom&aacute;tica.</p>";
                return procesarAlertaCobranza(reciboId, "AVISO_MORA", asunto, plantillaHtml);
        }

        // Centralizada la logica de procesamiento de notificaciones
        private CompletableFuture<Void> procesarEnvioContrato(Integer contratoId, String correoDestino) {
                try {
                        Contrato contrato = contratoRepository.findById(contratoId)
                                        .orElseThrow(() -> new RuntimeException("Contrato no encontrado."));
                        // Validamos el correo
                        String destinatario = (correoDestino != null && !correoDestino.trim().isEmpty())
                                        ? correoDestino
                                        : contrato.getCliente().getCorreo();
                        // Validamos posible nulos
                        if (destinatario == null || destinatario.trim().isEmpty()) {
                                log.warn("El contrato {} no pudo ser enviado: Destinatario inválido o vacio.",
                                                contratoId);
                                return CompletableFuture.completedFuture(null);
                        }
                        // Generar el pdf
                        byte[] pdfContrato = pdfService.generarContratoPdf(contratoId);
                        String messageHtml = String.format(
                                        "<h3>¡Tu servicio está Activo, %s!</h3>" +
                                                        "<p>Adjunto encontrarás tu contrato digital en formato PDF.</p>",
                                        contrato.getCliente().getNombres());
                        // Auditoria
                        Notificacion auditoria = Notificacion.builder()
                                        .cliente(contrato.getCliente())
                                        .tipo("Contrato Digital")
                                        .estado("ENVIADO")
                                        .mensaje(messageHtml + " | Enviado a: " + destinatario)
                                        .fechaEnvio(LocalDateTime.now())
                                        .build();
                        notificacionRepository.save(auditoria);
                        // Envio
                        emailService.enviarCorreo(destinatario, "Bienvenido a Conecta", messageHtml, pdfContrato,
                                        "Contrato_" + contratoId + ".pdf");
                        return CompletableFuture.completedFuture(null);
                } catch (Exception e) {
                        log.error("Error al procesar el envío del contrato {}: {}", contratoId, e.getMessage());
                        CompletableFuture<Void> future = new CompletableFuture<>();
                        future.completeExceptionally(e);
                        return future;
                }
        }

        private CompletableFuture<Void> procesarEnvioRecibo(Integer reciboId, String correoDestino) {
                try {
                        Recibo recibo = reciboRepository.findById(reciboId)
                                        .orElseThrow(() -> new RuntimeException("Recibo no encontrado."));
                        Cliente cliente = recibo.getContrato().getCliente();
                        String destinatario = (correoDestino != null && !correoDestino.trim().isEmpty())
                                        ? correoDestino
                                        : cliente.getCorreo();
                        if (destinatario == null || destinatario.trim().isEmpty()) {
                                log.warn("El recibo {} no pudo enviarse: Destinatario inválido o el cliente no tiene correo.",
                                                reciboId);
                                return CompletableFuture.completedFuture(null);
                        }
                        // Generar el pdf
                        byte[] pdfRecibo = pdfService.generarReciboPdf(reciboId);
                        // Armar el mensaje
                        String periodo = recibo.getPeriodoInicio()
                                        .format(java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy")) +
                                        " al " + recibo.getPeriodoFin().format(
                                                        java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy"));

                        String messageHtml = String.format(
                                        "<h3>Hola, %s</h3>" +
                                                        "<p>Adjuntamos tu recibo correspondiente al periodo <strong>%s</strong>.</p>"
                                                        +
                                                        "<p>Recuerda que el monto a pagar es <strong>S/ %s</strong> y vence el <strong>%s</strong>.</p>",
                                        cliente.getNombres(), periodo, recibo.getMontoTotal(),
                                        recibo.getFechaVencimiento());
                        // Guardar auditoria
                        Notificacion auditoria = Notificacion.builder()
                                        .cliente(cliente)
                                        .tipo("RECIBO_MENSUAL")
                                        .estado("ENVIADO")
                                        .mensaje(messageHtml + " | Enviado a: " + destinatario)
                                        .fechaEnvio(LocalDateTime.now())
                                        .build();
                        notificacionRepository.save(auditoria);
                        // Enviar al servicio
                        emailService.enviarCorreo(destinatario, "Recibo Conecta", messageHtml, pdfRecibo,
                                        "Recibo_Conecta_" + reciboId + ".pdf");
                        return CompletableFuture.completedFuture(null);
                } catch (Exception e) {
                        log.error("Fallo asíncrono al enviar recibo {}: {}", reciboId, e.getMessage());
                        CompletableFuture<Void> future = new CompletableFuture<>();
                        future.completeExceptionally(e);
                        return future;
                }
        }

        private CompletableFuture<Void> procesarAlertaCobranza(Integer reciboId, String tipoNotificacion, String asunto,
                        String plantillaHtml) {
                try {
                        Recibo recibo = reciboRepository.findById(reciboId)
                                        .orElseThrow(() -> new RuntimeException(
                                                        "Recibo no encontrado con el Id: " + reciboId));
                        String destinatario = recibo.getContrato().getCliente().getCorreo();
                        // Validación de correo
                        if (destinatario == null || destinatario.trim().isEmpty()) {
                                log.warn("Alerta {} no enviada: El cliente del recibo {} no tiene correo.",
                                                tipoNotificacion, reciboId);
                                return CompletableFuture.completedFuture(null);
                        }
                        String montoFormat = recibo.getMontoTotal().setScale(2, java.math.RoundingMode.HALF_UP)
                                        .toString();
                        String fechaFormat = recibo.getFechaVencimiento()
                                        .format(java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy"));
                        String messageHtml = String.format(plantillaHtml, montoFormat, fechaFormat);

                        // Guardar y auditoria
                        Notificacion auditoria = Notificacion.builder()
                                        .cliente(recibo.getContrato().getCliente())
                                        .tipo(tipoNotificacion)
                                        .estado("ENVIADO")
                                        .mensaje(messageHtml)
                                        .fechaEnvio(java.time.LocalDateTime.now())
                                        .build();
                        notificacionRepository.save(auditoria);
                        // enviar recordatorio por correo
                        emailService.enviarCorreo(destinatario, asunto, messageHtml, null, null);
                        return CompletableFuture.completedFuture(null);
                } catch (Exception e) {
                        log.error("Fallo asíncrono al procesar alerta {} para recibo {}: {}", tipoNotificacion,
                                        reciboId, e.getMessage());
                        CompletableFuture<Void> future = new CompletableFuture<>();
                        future.completeExceptionally(e);
                        return future;
                }
        }
}
