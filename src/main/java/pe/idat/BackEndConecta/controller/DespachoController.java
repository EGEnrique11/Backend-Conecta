package pe.idat.BackEndConecta.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
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
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<InstalacionPendienteDTO>> obtenerPendientes(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fecha,
            @RequestParam String franja) {

        return ResponseEntity.ok(despachoService.obtenerPendientesPorFechaYFranja(fecha, franja));
    }

    @GetMapping("/asignadas")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<InstalacionPendienteDTO>> obtenerAsignadas(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fecha) {

        return ResponseEntity.ok(despachoService.obtenerAsignadasPorFecha(fecha));
    }

    @PutMapping("/asignar/{instalacionId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, String>> asignarTecnico(
            @PathVariable Integer instalacionId,
            @Valid @RequestBody AsignarTecnicoDTO dto) {

        return ResponseEntity.ok(despachoService.asignarTecnicoABloque(instalacionId, dto));
    }

    @GetMapping("/buscar")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<InstalacionPendienteDTO>> buscarInstalaciones(
            @RequestParam String criterio,
            @RequestParam String valor) {
        return ResponseEntity.ok(despachoService.buscarInstalaciones(criterio, valor));
    }

    @GetMapping("/tecnico/agenda")
    @PreAuthorize("hasAnyRole('ADMIN', 'TECNICO')")
    public ResponseEntity<List<InstalacionPendienteDTO>> obtenerAgendaTecnico(
            @RequestParam Integer tecnicoId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fecha) {

        return ResponseEntity.ok(despachoService.obtenerAgendaTecnico(tecnicoId, fecha));
    }
}
