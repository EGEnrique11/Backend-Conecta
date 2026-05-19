package pe.idat.BackEndConecta.controller;

import lombok.RequiredArgsConstructor;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import pe.idat.BackEndConecta.dto.ReciboListDTO;
import pe.idat.BackEndConecta.entity.enums.EstadoPago;
import pe.idat.BackEndConecta.service.FacturacionService;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/facturacion")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class FacturacionController {

    private final FacturacionService facturacionService;

    @PostMapping("/generar/{cicloId}")
    public ResponseEntity<Map<String, Object>> generarRecibos(@PathVariable Integer cicloId) {
        LocalDate fechaEjecucionActual = LocalDate.now();
        return ResponseEntity.ok(facturacionService.generarRecibosPorCiclo(cicloId, fechaEjecucionActual));
    }

    @GetMapping("/contratos/{contratoId}/recibos")
    public ResponseEntity<Page<ReciboListDTO>> obtenerRecibosPaginados(
            @PathVariable Integer contratoId,
            @RequestParam(required = false) List<EstadoPago> estados,
            @PageableDefault(size = 12) Pageable pageable) {

        if (estados == null || estados.isEmpty()) {
            // Default: all states if none selected
            estados = java.util.Arrays.asList(EstadoPago.values());
        }

        return ResponseEntity.ok(facturacionService.obtenerRecibosPaginados(contratoId, estados, pageable));
    }
}
