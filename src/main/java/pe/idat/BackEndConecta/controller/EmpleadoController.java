package pe.idat.BackEndConecta.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import pe.idat.BackEndConecta.dto.EmpleadoRegistroDTO;
import pe.idat.BackEndConecta.service.EmpleadoService;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/empleados")
@RequiredArgsConstructor
public class EmpleadoController {

    private final EmpleadoService empleadoService;

    @PostMapping("/")
    /*@PreAuthorize("hasRole('ADMIN')")*/
    public ResponseEntity<Map<String, String>> registrarEmpleado(@Valid @RequestBody EmpleadoRegistroDTO request) {
        String mensaje = empleadoService.registrarEmpleado(request);
        return new ResponseEntity<>(Map.of("mensaje", mensaje), HttpStatus.CREATED);
    }

    @GetMapping("/rol/tecnico")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<java.util.List<Map<String, Object>>> obtenerTecnicos() {
        return ResponseEntity.ok(empleadoService.obtenerTecnicos());
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'DEVELOPER')")
    public ResponseEntity<org.springframework.data.domain.Page<pe.idat.BackEndConecta.dto.EmpleadoListDTO>> listarEmpleados(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "15") int size,
            @RequestParam(required = false) String rol,
            @RequestParam(required = false) pe.idat.BackEndConecta.entity.enums.TipoDocumento tipoDocumento,
            @RequestParam(required = false) String criterio,
            @RequestParam(required = false) String valor) {
        
        return ResponseEntity.ok(empleadoService.obtenerEmpleadosPaginados(page, size, rol, tipoDocumento, criterio, valor));
    }
}
