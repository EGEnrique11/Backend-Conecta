package pe.idat.BackEndConecta.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import pe.idat.BackEndConecta.service.FacturacionService;

import java.time.LocalDate;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/facturacion")
@RequiredArgsConstructor
@org.springframework.security.access.prepost.PreAuthorize("hasRole('ADMIN')")
public class FacturacionController {

    private final FacturacionService facturacionService;

    @PostMapping("/generar/{cicloId}")
    public ResponseEntity<Map<String, Object>> generarRecibos(@PathVariable Integer cicloId) {
        LocalDate fechaEjecucionActual = LocalDate.now();
        return ResponseEntity.ok(facturacionService.generarRecibosPorCiclo(cicloId, fechaEjecucionActual));
    }
}
