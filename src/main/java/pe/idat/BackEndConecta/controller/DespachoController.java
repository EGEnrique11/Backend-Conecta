package pe.idat.BackEndConecta.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import pe.idat.BackEndConecta.dto.AsignarTecnicoDTO;
import pe.idat.BackEndConecta.dto.InstalacionPendienteDTO;
import pe.idat.BackEndConecta.service.DespachoService;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.security.Principal;

@RestController
@RequestMapping("/api/v1/despacho")
@RequiredArgsConstructor
public class DespachoController {

    private final DespachoService despachoService;

    @GetMapping("/pendientes")
    @org.springframework.security.access.prepost.PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<InstalacionPendienteDTO>> obtenerPendientes(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fecha,
            @RequestParam String franja) {
        
        return ResponseEntity.ok(despachoService.obtenerPendientesPorFechaYFranja(fecha, franja));
    }

    @PutMapping("/asignar/{instalacionId}")
    @org.springframework.security.access.prepost.PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, String>> asignarTecnico(
            @PathVariable Integer instalacionId,
            @Valid @RequestBody AsignarTecnicoDTO dto) {
        
        return ResponseEntity.ok(despachoService.asignarTecnicoABloque(instalacionId, dto));
    }

    @PutMapping("/estado/{instalacionId}")
    @org.springframework.security.access.prepost.PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, String>> actualizarEstado(
            @PathVariable Integer instalacionId,
            @RequestParam pe.idat.BackEndConecta.entity.enums.EstadoInstalacion estado) {
        
        return ResponseEntity.ok(despachoService.actualizarEstado(instalacionId, estado));
    }

    @GetMapping("/tecnico/agenda")
    @org.springframework.security.access.prepost.PreAuthorize("hasAnyRole('ADMIN', 'TECNICO')")
    public ResponseEntity<List<InstalacionPendienteDTO>> obtenerAgendaTecnico(
            @RequestParam Integer mes,
            @RequestParam Integer anio,
            Principal principal) {
        
        String username = principal.getName();
        return ResponseEntity.ok(despachoService.obtenerAgendaTecnico(mes, anio, username));
    }
}
