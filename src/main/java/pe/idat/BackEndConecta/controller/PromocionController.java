package pe.idat.BackEndConecta.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import pe.idat.BackEndConecta.dto.EfectoPromocionDTO;
import pe.idat.BackEndConecta.dto.EfectoPromocionRequestDTO;
import pe.idat.BackEndConecta.dto.PromocionDTO;
import pe.idat.BackEndConecta.dto.PromocionRequestDTO;
import pe.idat.BackEndConecta.service.CatalogoService;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/catalogo/promociones")
@RequiredArgsConstructor
@org.springframework.security.access.prepost.PreAuthorize("hasRole('ADMIN')")
public class PromocionController {

    private final CatalogoService catalogoService;

    // --- PROMOCION CRUD ---
    @GetMapping
    public ResponseEntity<List<PromocionDTO>> listarPromociones() {
        return ResponseEntity.ok(catalogoService.listarPromociones());
    }

    @GetMapping("/plan/{planId}")
    public ResponseEntity<List<PromocionDTO>> obtenerPromocionesPorPlan(@PathVariable Integer planId) {
        return ResponseEntity.ok(catalogoService.listarPromocionesPorPlan(planId));
    }

    @GetMapping("/{id}")
    public ResponseEntity<PromocionDTO> obtenerPromocion(@PathVariable Integer id) {
        return ResponseEntity.ok(catalogoService.obtenerPromocion(id));
    }

    @PostMapping
    public ResponseEntity<PromocionDTO> crearPromocion(@Valid @RequestBody PromocionRequestDTO dto) {
        return new ResponseEntity<>(catalogoService.crearPromocion(dto), HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    public ResponseEntity<PromocionDTO> actualizarPromocion(@PathVariable Integer id, @Valid @RequestBody PromocionRequestDTO dto) {
        return ResponseEntity.ok(catalogoService.actualizarPromocion(id, dto));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminarPromocion(@PathVariable Integer id) {
        catalogoService.eliminarPromocion(id);
        return ResponseEntity.noContent().build();
    }

    // --- EFECTO PROMOCION CRUD ---
    @GetMapping("/{promocionId}/efectos")
    public ResponseEntity<List<EfectoPromocionDTO>> listarEfectos(@PathVariable Integer promocionId) {
        return ResponseEntity.ok(catalogoService.listarEfectosPorPromocion(promocionId));
    }

    @PostMapping("/{promocionId}/efectos")
    public ResponseEntity<EfectoPromocionDTO> anadirEfecto(@PathVariable Integer promocionId, @Valid @RequestBody EfectoPromocionRequestDTO dto) {
        return new ResponseEntity<>(catalogoService.anadirEfectoAPromocion(promocionId, dto), HttpStatus.CREATED);
    }

    @DeleteMapping("/efectos/{efectoId}")
    public ResponseEntity<Void> eliminarEfecto(@PathVariable Integer efectoId) {
        catalogoService.eliminarEfecto(efectoId);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{promocionId}/planes")
    public ResponseEntity<Map<String, Object>> asociarPlanes(@PathVariable Integer promocionId, @RequestBody List<Integer> planIds) {
        catalogoService.asociarPlanesAPromocion(promocionId, planIds);
        return ResponseEntity.ok(Map.of("mensaje", "Planes asociados correctamente a la promoción"));
    }
}
