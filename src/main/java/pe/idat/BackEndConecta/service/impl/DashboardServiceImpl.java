package pe.idat.BackEndConecta.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pe.idat.BackEndConecta.dto.projections.CrecimientoMensualProjection;
import pe.idat.BackEndConecta.dto.projections.ProductividadTecnicaProjection;
import pe.idat.BackEndConecta.dto.projections.RankingVendedorProjection;
import pe.idat.BackEndConecta.entity.enums.EstadoContrato;
import pe.idat.BackEndConecta.entity.enums.EstadoInstalacion;
import pe.idat.BackEndConecta.entity.enums.EstadoPago;
import pe.idat.BackEndConecta.repository.ContratoRepository;
import pe.idat.BackEndConecta.repository.InstalacionRepository;
import pe.idat.BackEndConecta.repository.ReciboRepository;
import pe.idat.BackEndConecta.service.DashboardService;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class DashboardServiceImpl implements DashboardService {

    private final ContratoRepository contratoRepository;
    private final InstalacionRepository instalacionRepository;
    private final ReciboRepository reciboRepository;

    @Override
    @Transactional(readOnly = true)
    public List<RankingVendedorProjection> getRankingVendedores(LocalDate inicio, LocalDate fin) {
        return contratoRepository.findRankingVendedores(inicio, fin);
    }

    @Override
    @Transactional(readOnly = true)
    public List<CrecimientoMensualProjection> getCrecimientoMensual() {
        LocalDate haceUnAnio = LocalDate.now().minusYears(1);
        return contratoRepository.findCrecimientoMensual(haceUnAnio);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ProductividadTecnicaProjection> getProductividadTecnica() {
        return instalacionRepository.findProductividadTecnica();
    }

    @Override
    @Transactional(readOnly = true)
    public Map<String, Object> getTasaInstalacionMensual() {
        List<EstadoInstalacion> estados = instalacionRepository.findEstadosInstalacionPorMes(LocalDate.now());
        long totales = estados.size();
        long completadas = estados.stream().filter(e -> e == EstadoInstalacion.COMPLETADA).count();
        
        double porcentaje = totales > 0 ? ((double) completadas / totales) * 100 : 0.0;

        Map<String, Object> result = new HashMap<>();
        result.put("totalMes", totales);
        result.put("completadasMes", completadas);
        result.put("tasaExitoPorcentaje", BigDecimal.valueOf(porcentaje).setScale(2, RoundingMode.HALF_UP));
        return result;
    }

    @Override
    @Transactional(readOnly = true)
    public Map<String, BigDecimal> getFinanzasIngresosVsDeuda() {
        BigDecimal pagados = reciboRepository.sumMontoTotalByEstado(EstadoPago.PAGADO);
        BigDecimal vencidos = reciboRepository.sumMontoTotalByEstado(EstadoPago.VENCIDO);

        Map<String, BigDecimal> result = new HashMap<>();
        result.put("ingresosRealizados", pagados);
        result.put("deudasYMora", vencidos);
        return result;
    }

    @Override
    @Transactional(readOnly = true)
    public Map<String, Object> getEfectividadCobro() {
        // En una DB productiva masiva, el pago vs vencimiento se checa en Pago / Historial. 
        // Simplificado: Efectividad global de Recibos pagados antes de mora.
        List<EstadoPago> estados = reciboRepository.findAllEstadosPagos();
        
        long total = estados.size();
        long morosos = estados.stream().filter(e -> e == EstadoPago.VENCIDO).count();
        long pagados = estados.stream().filter(e -> e == EstadoPago.PAGADO).count();

        double moraPorcentaje = total > 0 ? ((double) morosos / total) * 100 : 0.0;
        double efecPorcentaje = total > 0 ? ((double) pagados / total) * 100 : 0.0;

        Map<String, Object> result = new HashMap<>();
        result.put("tasaEfectividad", BigDecimal.valueOf(efecPorcentaje).setScale(2, RoundingMode.HALF_UP));
        result.put("tasaMoraGlobal", BigDecimal.valueOf(moraPorcentaje).setScale(2, RoundingMode.HALF_UP));
        return result;
    }

    @Override
    @Transactional(readOnly = true)
    public Map<String, Object> getResumenKPIs() {
        long activos = contratoRepository.countByEstado(EstadoContrato.ACTIVO);
        BigDecimal ingresosMes = reciboRepository.sumIngresosDeMes(LocalDate.now());
        
        List<EstadoPago> estados = reciboRepository.findAllEstadosPagos();
        long morosos = estados.stream().filter(e -> e == EstadoPago.VENCIDO).count();
        double moraPx = estados.size() > 0 ? ((double) morosos / estados.size()) * 100 : 0.0;
        
        long pendientesHoy = instalacionRepository.countByEstadoAndFechaProgramada(EstadoInstalacion.PENDIENTE, LocalDate.now());

        Map<String, Object> result = new HashMap<>();
        result.put("totalClientesActivos", activos);
        result.put("ingresosMesActual", ingresosMes);
        result.put("tasaMoraPorcentual", BigDecimal.valueOf(moraPx).setScale(2, RoundingMode.HALF_UP));
        result.put("instalacionesPendientesHoy", pendientesHoy);
        
        return result;
    }
}
