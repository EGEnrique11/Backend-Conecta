package pe.idat.BackEndConecta.service.impl;

import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pe.idat.BackEndConecta.entity.Cliente;
import pe.idat.BackEndConecta.entity.Notificacion;
import pe.idat.BackEndConecta.repository.NotificacionRepository;
import pe.idat.BackEndConecta.service.NotificacionService;

import java.time.LocalDateTime;
import java.util.concurrent.CompletableFuture;

@Service
@RequiredArgsConstructor
public class NotificacionServiceImpl implements NotificacionService {

    private static final Logger log = LoggerFactory.getLogger(NotificacionServiceImpl.class);

    private final JavaMailSender javaMailSender;
    private final NotificacionRepository notificacionRepository;

    @Value("${spring.mail.username:sender@conecta.com}")
    private String senderEmail;

    @Override
    @Async
    @Transactional
    public CompletableFuture<Void> enviarCorreoConAdjuntoAsincrono(Cliente cliente, String tipo, String asunto, String mensajeHtml, byte[] archivoAdjunto, String nombreArchivo) {
        
        // 1. Registro Inicial en Base de Datos
        Notificacion notificacion = Notificacion.builder()
                .cliente(cliente)
                .tipo(tipo)
                .estado("PENDIENTE")
                .mensaje(mensajeHtml)
                .build();
        
        notificacion = notificacionRepository.save(notificacion);

        try {
            // 2. Transmisión del Correo (MimeMessage)
            MimeMessage message = javaMailSender.createMimeMessage();
            // Soporte Multipart (Mixed/Related para HTML y Attachments)
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setFrom(senderEmail);
            helper.setTo(cliente.getCorreo());
            helper.setSubject(asunto);
            helper.setText(mensajeHtml, true); // true = soporta HTML format

            // Agregar Adjunto si se incluyó
            if (archivoAdjunto != null && nombreArchivo != null) {
                helper.addAttachment(nombreArchivo, new ByteArrayResource(archivoAdjunto));
            }

            // Ejecución
            javaMailSender.send(message);

            // 3. Resultado de Éxito
            notificacion.setEstado("ENVIADO");
            notificacion.setFechaEnvio(LocalDateTime.now());
            log.info("Notificación asíncrona ENVIADA a {} (Tipo: {})", cliente.getCorreo(), tipo);

        } catch (Exception e) {
            // 4. Caso de Fallo
            notificacion.setEstado("ERROR");
            log.error("Error enviando notificación SMTP a {}: {}", cliente.getCorreo(), e.getMessage());
        }

        // 5. Persistencia de Cierre
        notificacionRepository.save(notificacion);
        
        return CompletableFuture.completedFuture(null);
    }

    @Override
    @Async
    public CompletableFuture<Void> enviarCorreoAsincrono(Cliente cliente, String tipo, String asunto, String mensajeHtml) {
        return enviarCorreoConAdjuntoAsincrono(cliente, tipo, asunto, mensajeHtml, null, null);
    }
}
