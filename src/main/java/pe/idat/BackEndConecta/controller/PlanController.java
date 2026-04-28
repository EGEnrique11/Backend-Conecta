package pe.idat.BackEndConecta.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import pe.idat.BackEndConecta.dto.PlanDTO;
import pe.idat.BackEndConecta.dto.PlanRequestDTO;
import pe.idat.BackEndConecta.service.CatalogoService;

import java.util.List;

@RestController
@RequestMapping("/api/v1/catalogo/planes")
@RequiredArgsConstructor
@org.springframework.security.access.prepost.PreAuthorize("hasRole('ADMIN')")
public class PlanController {

    private final CatalogoService catalogoService;

    @GetMapping
    public ResponseEntity<List<PlanDTO>> listarPlanes() {
        return ResponseEntity.ok(catalogoService.listarPlanes());
    }

    @GetMapping("/servicio/{servicioId}")
    public ResponseEntity<List<PlanDTO>> listarPlanesPorServicio(@PathVariable Integer servicioId) {
        return ResponseEntity.ok(catalogoService.listarPlanesPorServicio(servicioId));
    }

    @GetMapping("/{id}")
    public ResponseEntity<PlanDTO> obtenerPlan(@PathVariable Integer id) {
        return ResponseEntity.ok(catalogoService.obtenerPlan(id));
    }

    @PostMapping
    public ResponseEntity<PlanDTO> crearPlan(@Valid @RequestBody PlanRequestDTO dto) {
        return new ResponseEntity<>(catalogoService.crearPlan(dto), HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    public ResponseEntity<PlanDTO> actualizarPlan(@PathVariable Integer id, @Valid @RequestBody PlanRequestDTO dto) {
        return ResponseEntity.ok(catalogoService.actualizarPlan(id, dto));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminarPlan(@PathVariable Integer id) {
        catalogoService.eliminarPlan(id);
        return ResponseEntity.noContent().build();
    }
}
