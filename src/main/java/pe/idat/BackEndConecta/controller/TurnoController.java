package pe.idat.BackEndConecta.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import pe.idat.BackEndConecta.dto.BloqueHorarioRequestDTO;
import pe.idat.BackEndConecta.dto.TurnoRequestDTO;
import pe.idat.BackEndConecta.entity.BloqueHorario;
import pe.idat.BackEndConecta.entity.Turno;
import pe.idat.BackEndConecta.service.TurnoService;

import java.util.List;

@RestController
@RequestMapping("/api/v1/despacho/turnos")
@RequiredArgsConstructor
public class TurnoController {

    private final TurnoService turnoService;

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'TECNICO')")
    public ResponseEntity<List<Turno>> obtenerTurnos() {
        return ResponseEntity.ok(turnoService.obtenerTurnos());
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Turno> crearTurno(@RequestBody TurnoRequestDTO dto) {
        return ResponseEntity.ok(turnoService.crearTurno(dto));
    }

    @PostMapping("/{turnoId}/bloques")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<BloqueHorario> agregarBloqueATurno(
            @PathVariable Integer turnoId,
            @RequestBody BloqueHorarioRequestDTO dto) {
        return ResponseEntity.ok(turnoService.agregarBloqueATurno(turnoId, dto));
    }

    @GetMapping("/{turnoId}/bloques")
    @PreAuthorize("hasAnyRole('ADMIN', 'TECNICO')")
    public ResponseEntity<List<BloqueHorario>> obtenerBloquesPorTurno(@PathVariable Integer turnoId) {
        return ResponseEntity.ok(turnoService.obtenerBloquesPorTurno(turnoId));
    }

    @PutMapping("/bloques/{bloqueId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<BloqueHorario> actualizarBloque(
            @PathVariable Integer bloqueId,
            @RequestBody BloqueHorarioRequestDTO dto) {
        return ResponseEntity.ok(turnoService.actualizarBloque(bloqueId, dto));
    }

    @DeleteMapping("/bloques/{bloqueId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> eliminarBloque(@PathVariable Integer bloqueId) {
        turnoService.eliminarBloque(bloqueId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/tecnico/{tecnicoId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'TECNICO')")
    public ResponseEntity<Turno> obtenerTurnoDeTecnico(@PathVariable Integer tecnicoId) {
        return ResponseEntity.ok(turnoService.obtenerTurnoDeTecnico(tecnicoId));
    }
}
