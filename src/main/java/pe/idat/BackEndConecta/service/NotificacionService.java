package pe.idat.BackEndConecta.service;

import java.util.concurrent.CompletableFuture;

public interface NotificacionService {
    CompletableFuture<Void> enviarContratoAsync(Integer contratoId);
    CompletableFuture<Void> enviarContratoManualAsync(Integer contradoId, String correoDestino);

    CompletableFuture<Void> enviarReciboMensualAsync(Integer reciboId);
    CompletableFuture<Void> enviarReciboMensualManualAsync(Integer reciboId, String correoDestino);

    CompletableFuture<Void> enviarRecordatorioPreventivoAsync(Integer reciboId);
    CompletableFuture<Void> enviarAvisoMoraAsync(Integer reciboId);
}
