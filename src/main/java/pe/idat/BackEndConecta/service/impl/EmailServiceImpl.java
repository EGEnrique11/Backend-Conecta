package pe.idat.BackEndConecta.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.core.io.ByteArrayResource;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import pe.idat.BackEndConecta.service.EmailService;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmailServiceImpl implements EmailService {
    private final JavaMailSender mailSender;

    @Override
    @Async
    public void enviarCorreo(String destinatario, String asunto, String messageHtml, byte[] adjunto,
            String nombreAdjunto) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            helper.setFrom("eynertdeadlyt@gmail.com");
            helper.setTo(destinatario);
            helper.setSubject(asunto);
            helper.setText(messageHtml, true);
            if(adjunto != null && adjunto.length>0){
                helper.addAttachment(nombreAdjunto, new ByteArrayResource(adjunto));
            }
            mailSender.send(message);
            log.info("Email '{}' enviado correctamente a {}", asunto, destinatario);
        } catch (MessagingException e) {
            log.error("Error al enviar email a {}: {}", destinatario, e.getMessage());
        }
    }
}
