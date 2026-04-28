package pe.idat.BackEndConecta.service;

import pe.idat.BackEndConecta.entity.Cliente;

import java.util.concurrent.CompletableFuture;

public interface NotificacionService {
    CompletableFuture<Void> enviarCorreoConAdjuntoAsincrono(Cliente cliente, String tipo, String asunto, String mensajeHtml, byte[] archivoAdjunto, String nombreArchivo);
    CompletableFuture<Void> enviarCorreoAsincrono(Cliente cliente, String tipo, String asunto, String mensajeHtml);
}
