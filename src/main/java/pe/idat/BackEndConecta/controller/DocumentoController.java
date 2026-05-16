package pe.idat.BackEndConecta.controller;

import lombok.RequiredArgsConstructor;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import pe.idat.BackEndConecta.dto.EmailRequestDTO;
import pe.idat.BackEndConecta.service.NotificacionService;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/documentos")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class DocumentoController {

    private final NotificacionService notificacionService;

    @PostMapping("/contrato/{id}/enviar-correo")
    public ResponseEntity<Map<String, String>> enviarContratoCorreo(
            @PathVariable Integer id,
            @RequestBody(required = false) EmailRequestDTO request) {
        try {
            String correoDestino = (request != null) ? request.getCorreoDestino() : null;
            notificacionService.enviarContratoManualAsync(id, correoDestino).join();
            String destinoText = (correoDestino != null) ? correoDestino : "su correo registrado";
            return ResponseEntity.ok(Map.of("mensaje", "Contrato enviado exitosamente a " + destinoText));
        } catch (Exception e) {
            String mensajeError = (e.getCause() != null) ? e.getCause().getMessage(): e.getMessage();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "No se pudo enviar el contrato. Motivo: " + mensajeError));
        }
    }

    @PostMapping("/recibo/{id}/enviar-correo")
    public ResponseEntity<Map<String, String>> enviarReciboCorreo(
            @PathVariable Integer id,
            @RequestBody(required = false) EmailRequestDTO request) {
        try {
            String correoDestino = (request != null) ? request.getCorreoDestino() : null;
            notificacionService.enviarReciboMensualManualAsync(id, correoDestino).join();
            String destinoText = (correoDestino != null) ? correoDestino : "su correo registrado";
            return ResponseEntity.ok(Map.of("mensaje", "Correo enviado exitosamente a " + destinoText));
        } catch (Exception e) {
            String mensajeError = (e.getCause() != null) ? e.getCause().getMessage(): e.getMessage();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "No se pudo enviar el recibo. Motivo: " + mensajeError));
        }

    }
}
