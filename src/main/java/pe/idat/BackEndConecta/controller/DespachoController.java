package pe.idat.BackEndConecta.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import pe.idat.BackEndConecta.dto.AsignarTecnicoDTO;
import pe.idat.BackEndConecta.dto.BloqueHorarioRequestDTO;
import pe.idat.BackEndConecta.dto.InstalacionPendienteDTO;
import pe.idat.BackEndConecta.dto.TurnoRequestDTO;
import pe.idat.BackEndConecta.entity.BloqueHorario;
import pe.idat.BackEndConecta.entity.Turno;
import pe.idat.BackEndConecta.entity.enums.EstadoInstalacion;
import pe.idat.BackEndConecta.service.DespachoService;
import pe.idat.BackEndConecta.service.TurnoService;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.security.Principal;

@RestController
@RequestMapping("/api/v1/despacho")
@RequiredArgsConstructor
public class DespachoController {

    private final DespachoService despachoService;
    private final TurnoService turnoService;

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

    @PutMapping("/estado/{instalacionId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, String>> actualizarEstado(
            @PathVariable Integer instalacionId,
            @RequestParam EstadoInstalacion estado) {
        
        return ResponseEntity.ok(despachoService.actualizarEstado(instalacionId, estado));
    }

    @GetMapping("/buscar")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<InstalacionPendienteDTO>> buscarInstalaciones(@RequestParam String term) {
        return ResponseEntity.ok(despachoService.buscarInstalaciones(term));
    }

    @PutMapping("/reprogramar/{instalacionId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, String>> reprogramarInstalacion(
            @PathVariable Integer instalacionId,
            @Valid @RequestBody pe.idat.BackEndConecta.dto.InstalacionReprogramarDTO dto) {
        return ResponseEntity.ok(despachoService.reprogramarInstalacion(instalacionId, dto));
    }

    @GetMapping("/tecnico/agenda")
    @PreAuthorize("hasAnyRole('ADMIN', 'TECNICO')")
    public ResponseEntity<List<InstalacionPendienteDTO>> obtenerAgendaTecnico(
            @RequestParam Integer mes,
            @RequestParam Integer anio,
            Principal principal) {
        
        String username = principal.getName();
        return ResponseEntity.ok(despachoService.obtenerAgendaTecnico(mes, anio, username));
    }

    @GetMapping("/turnos")
    @PreAuthorize("hasAnyRole('ADMIN', 'TECNICO')")
    public ResponseEntity<List<Turno>> obtenerTurnos() {
        return ResponseEntity.ok(turnoService.obtenerTurnos());
    }

    @PostMapping("/turnos")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Turno> crearTurno(
            @RequestBody TurnoRequestDTO dto) {
        return ResponseEntity.ok(turnoService.crearTurno(dto));
    }

    @PostMapping("/turnos/{turnoId}/bloques")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<BloqueHorario> agregarBloqueATurno(
            @PathVariable Integer turnoId,
            @RequestBody BloqueHorarioRequestDTO dto) {
        return ResponseEntity.ok(turnoService.agregarBloqueATurno(turnoId, dto));
    }

    @GetMapping("/turnos/{turnoId}/bloques")
    @PreAuthorize("hasAnyRole('ADMIN', 'TECNICO')")
    public ResponseEntity<List<BloqueHorario>> obtenerBloquesPorTurno(@PathVariable Integer turnoId) {
        return ResponseEntity.ok(turnoService.obtenerBloquesPorTurno(turnoId));
    }

    @PutMapping("/turnos/bloques/{bloqueId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<BloqueHorario> editarBloque(
            @PathVariable Integer bloqueId,
            @RequestBody BloqueHorarioRequestDTO dto) {
        return ResponseEntity.ok(turnoService.editarBloque(bloqueId, dto));
    }

    @DeleteMapping("/turnos/bloques/{bloqueId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> eliminarBloque(@PathVariable Integer bloqueId) {
        turnoService.eliminarBloque(bloqueId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/turnos/tecnico/{tecnicoId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'TECNICO')")
    public ResponseEntity<Turno> obtenerTurnoDeTecnico(@PathVariable Integer tecnicoId) {
        return ResponseEntity.ok(turnoService.obtenerTurnoDeTecnico(tecnicoId));
    }
}
