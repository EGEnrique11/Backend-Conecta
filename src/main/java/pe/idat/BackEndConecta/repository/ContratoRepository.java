package pe.idat.BackEndConecta.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import pe.idat.BackEndConecta.entity.Contrato;
import pe.idat.BackEndConecta.entity.enums.EstadoContrato;
import pe.idat.BackEndConecta.dto.projections.RankingVendedorProjection;
import pe.idat.BackEndConecta.dto.projections.CrecimientoMensualProjection;

import java.time.LocalDate;
import java.util.List;

public interface ContratoRepository extends JpaRepository<Contrato, Integer> {

    @Query("SELECT c.empleadoRegistro.nombres AS vendedor, COUNT(c) AS cantidad " +
    "FROM Contrato c WHERE c.fechaContrato BETWEEN :inicio AND :fin " +
    "GROUP BY c.empleadoRegistro.id, c.empleadoRegistro.nombres ORDER BY cantidad DESC")
    List<RankingVendedorProjection> findRankingVendedores(@Param("inicio") LocalDate inicio, @Param("fin") LocalDate fin);

    @Query("SELECT MONTH(c.fechaContrato) AS mes, YEAR(c.fechaContrato) AS anio, COUNT(c) AS cantidad FROM Contrato c WHERE c.fechaContrato >= :haceUnAnio GROUP BY YEAR(c.fechaContrato), MONTH(c.fechaContrato) ORDER BY anio ASC, mes ASC")
    List<CrecimientoMensualProjection> findCrecimientoMensual(@Param("haceUnAnio") LocalDate haceUnAnio);

    Long countByEstado(EstadoContrato estado);

    List<Contrato> findByCicloPagoIdAndEstadoIn(Integer cicloPagoId, List<EstadoContrato> estados);
    List<Contrato> findByClienteId(Integer clienteId);
}
