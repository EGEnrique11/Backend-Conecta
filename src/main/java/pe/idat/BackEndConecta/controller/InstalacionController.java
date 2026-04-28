package pe.idat.BackEndConecta.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import pe.idat.BackEndConecta.dto.InstalacionObservacionDTO;
import pe.idat.BackEndConecta.dto.InstalacionReprogramarDTO;
import pe.idat.BackEndConecta.service.InstalacionService;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/instalaciones")
@RequiredArgsConstructor
@org.springframework.security.access.prepost.PreAuthorize("hasAnyRole('ADMIN', 'TECNICO')")
public class InstalacionController {

    private final InstalacionService instalacionService;

    @PutMapping("/{id}/completar")
    public ResponseEntity<Map<String, String>> completarInstalacion(
            @PathVariable Integer id, 
            @RequestBody(required = false) InstalacionObservacionDTO dto) {
        return ResponseEntity.ok(instalacionService.completarInstalacion(id, dto));
    }

    @PutMapping("/{id}/cancelar")
    public ResponseEntity<Map<String, String>> cancelarInstalacion(
            @PathVariable Integer id, 
            @RequestBody(required = false) InstalacionObservacionDTO dto) {
        return ResponseEntity.ok(instalacionService.cancelarInstalacion(id, dto));
    }

    @PutMapping("/{id}/reprogramar")
    public ResponseEntity<Map<String, String>> reprogramarInstalacion(
            @PathVariable Integer id, 
            @Valid @RequestBody InstalacionReprogramarDTO dto) {
        return ResponseEntity.ok(instalacionService.reprogramarInstalacion(id, dto));
    }
}
