package pe.idat.BackEndConecta.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.data.domain.Page;
import pe.idat.BackEndConecta.dto.EmpleadoListDTO;
import pe.idat.BackEndConecta.dto.EmpleadoRegistroDTO;
import pe.idat.BackEndConecta.dto.TecnicoResumenDTO;
import pe.idat.BackEndConecta.entity.enums.TipoDocumento;
import pe.idat.BackEndConecta.service.EmpleadoService;
import pe.idat.BackEndConecta.service.TurnoService;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/empleados")
@RequiredArgsConstructor
public class EmpleadoController {

    private final EmpleadoService empleadoService;
    private final TurnoService turnoService;

    @PostMapping("/")
    /* @PreAuthorize("hasRole('ADMIN')") */
    public ResponseEntity<Map<String, String>> registrarEmpleado(@Valid @RequestBody EmpleadoRegistroDTO request) {
        String mensaje = empleadoService.registrarEmpleado(request);
        return new ResponseEntity<>(Map.of("mensaje", mensaje), HttpStatus.CREATED);
    }

    @GetMapping("/rol/tecnico")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<Map<String, Object>>> obtenerTecnicos() {
        return ResponseEntity.ok(empleadoService.obtenerTecnicos());
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'DEVELOPER')")
    public ResponseEntity<Page<EmpleadoListDTO>> listarEmpleados(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "15") int size,
            @RequestParam(required = false) String rol,
            @RequestParam(required = false) TipoDocumento tipoDocumento,
            @RequestParam(required = false) String criterio,
            @RequestParam(required = false) String valor) {

        return ResponseEntity
                .ok(empleadoService.obtenerEmpleadosPaginados(page, size, rol, tipoDocumento, criterio, valor));
    }

    @GetMapping("/tecnicos/buscar")
    @PreAuthorize("hasAnyRole('ADMIN', 'DEVELOPER')")
    public ResponseEntity<List<TecnicoResumenDTO>> buscarTecnicos(@RequestParam String term) {
        return ResponseEntity.ok(empleadoService.buscarTecnicosResumen(term));
    }

    @GetMapping("/tecnicos/paginados")
    @PreAuthorize("hasAnyRole('ADMIN', 'DEVELOPER')")
    public ResponseEntity<Page<TecnicoResumenDTO>> obtenerTecnicosPaginados(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String term) {
        return ResponseEntity.ok(empleadoService.obtenerTecnicosPaginados(page, size, term));
    }

    @PutMapping("/{tecnicoId}/turno/{turnoId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, String>> asignarTurno(@PathVariable Integer tecnicoId,
            @PathVariable Integer turnoId) {

        turnoService.asignarTurnoATecnico(tecnicoId, turnoId);
        Map<String, String> response = new HashMap<>();
        response.put("message", "Turno asignado al técnico correctamente.");
        return ResponseEntity.ok(response);
    }
}
