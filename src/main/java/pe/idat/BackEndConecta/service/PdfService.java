package pe.idat.BackEndConecta.service;

public interface PdfService {
    byte[] generarReciboPdf(Integer reciboId);
    byte[] generarContratoPdf(Integer contratoId);
}
