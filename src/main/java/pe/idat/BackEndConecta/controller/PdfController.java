package pe.idat.BackEndConecta.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import pe.idat.BackEndConecta.service.PdfService;

@RestController
@RequestMapping("/api/v1/facturacion")
@RequiredArgsConstructor
public class PdfController {

    private final PdfService pdfService;

    @GetMapping("/recibo/{id}/pdf")
    public ResponseEntity<byte[]> descargarReciboPdf(@PathVariable("id") Integer id) {
        byte[] pdfMBytes = pdfService.generarReciboPdf(id);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_PDF);
        headers.setContentDispositionFormData("attachment", "recibo_conecta.pdf");

        return new ResponseEntity<>(pdfMBytes, headers, HttpStatus.OK);
    }
}
