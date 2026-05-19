package pe.idat.BackEndConecta.service;

import pe.idat.BackEndConecta.dto.projections.CrecimientoMensualProjection;
import pe.idat.BackEndConecta.dto.projections.ProductividadTecnicaProjection;
import pe.idat.BackEndConecta.dto.projections.RankingVendedorProjection;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

public interface DashboardService {
    List<RankingVendedorProjection> getRankingVendedores(java.time.LocalDate inicio, java.time.LocalDate fin);
    List<CrecimientoMensualProjection> getCrecimientoMensual();
    List<ProductividadTecnicaProjection> getProductividadTecnica();
    Map<String, Object> getTasaInstalacionMensual();
    Map<String, BigDecimal> getFinanzasIngresosVsDeuda();
    Map<String, Object> getEfectividadCobro();
    Map<String, Object> getResumenKPIs();
    Map<String, Long> getResumenInstalaciones(java.time.LocalDate inicio, java.time.LocalDate fin, pe.idat.BackEndConecta.entity.enums.EstadoInstalacion estado);
}
