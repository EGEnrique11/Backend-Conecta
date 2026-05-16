package pe.idat.BackEndConecta.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import pe.idat.BackEndConecta.dto.ServicioDTO;
import pe.idat.BackEndConecta.dto.ServicioRequestDTO;
import pe.idat.BackEndConecta.service.CatalogoService;

import java.util.List;

@RestController
@RequestMapping("/api/v1/catalogo/servicios")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class ServicioController {

    private final CatalogoService catalogoService;

    @GetMapping
    public ResponseEntity<List<ServicioDTO>> listarServicios() {
        return ResponseEntity.ok(catalogoService.listarServicios());
    }

    @GetMapping("/{id}")
    public ResponseEntity<ServicioDTO> obtenerServicio(@PathVariable Integer id) {
        return ResponseEntity.ok(catalogoService.obtenerServicio(id));
    }

    @PostMapping
    public ResponseEntity<ServicioDTO> crearServicio(@Valid @RequestBody ServicioRequestDTO dto) {
        return new ResponseEntity<>(catalogoService.crearServicio(dto), HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    public ResponseEntity<ServicioDTO> actualizarServicio(@PathVariable Integer id, @Valid @RequestBody ServicioRequestDTO dto) {
        return ResponseEntity.ok(catalogoService.actualizarServicio(id, dto));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminarServicio(@PathVariable Integer id) {
        catalogoService.eliminarServicio(id);
        return ResponseEntity.noContent().build();
    }
}
