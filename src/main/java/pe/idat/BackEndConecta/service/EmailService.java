package pe.idat.BackEndConecta.service;

public interface EmailService {
    void enviarCorreo(String destinatario, String asunto, String messageHtml, byte[] adjunto, String nombreAdjunto);
}
