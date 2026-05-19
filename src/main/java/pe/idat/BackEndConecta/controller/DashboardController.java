package pe.idat.BackEndConecta.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import pe.idat.BackEndConecta.dto.projections.CrecimientoMensualProjection;
import pe.idat.BackEndConecta.dto.projections.ProductividadTecnicaProjection;
import pe.idat.BackEndConecta.dto.projections.RankingVendedorProjection;
import pe.idat.BackEndConecta.service.DashboardService;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/dashboard")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class DashboardController {

    private final DashboardService dashboardService;

    @GetMapping("/rendimiento/vendedores")
    public ResponseEntity<List<RankingVendedorProjection>> getRankingVendedores(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate inicio,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fin) {
        return ResponseEntity.ok(dashboardService.getRankingVendedores(inicio, fin));
    }

    @GetMapping("/rendimiento/crecimiento")
    public ResponseEntity<List<CrecimientoMensualProjection>> getCrecimientoMensual() {
        return ResponseEntity.ok(dashboardService.getCrecimientoMensual());
    }

    @GetMapping("/operaciones/productividad")
    public ResponseEntity<List<ProductividadTecnicaProjection>> getProductividadTecnica() {
        return ResponseEntity.ok(dashboardService.getProductividadTecnica());
    }

    @GetMapping("/operaciones/tasa-instalacion")
    public ResponseEntity<Map<String, Object>> getTasaInstalacionMensual() {
        return ResponseEntity.ok(dashboardService.getTasaInstalacionMensual());
    }

    @GetMapping("/finanzas/ingresos-vs-deuda")
    public ResponseEntity<Map<String, BigDecimal>> getFinanzasIngresosVsDeuda() {
        return ResponseEntity.ok(dashboardService.getFinanzasIngresosVsDeuda());
    }

    @GetMapping("/finanzas/efectividad")
    public ResponseEntity<Map<String, Object>> getEfectividadCobro() {
        return ResponseEntity.ok(dashboardService.getEfectividadCobro());
    }

    @GetMapping("/resumen")
    public ResponseEntity<Map<String, Object>> getResumenKPIs() {
        return ResponseEntity.ok(dashboardService.getResumenKPIs());
    }
}
