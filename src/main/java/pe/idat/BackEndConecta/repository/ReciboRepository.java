package pe.idat.BackEndConecta.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import pe.idat.BackEndConecta.entity.Recibo;
import pe.idat.BackEndConecta.entity.enums.EstadoPago;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public interface ReciboRepository extends JpaRepository<Recibo, Integer> {
    List<Recibo> findByEstadoPagoAndFechaVencimientoBefore(EstadoPago estadoPago, LocalDate fechaVencimiento);

    List<Recibo> findByEstadoPagoAndFechaVencimiento(EstadoPago estadoPago, LocalDate fecha);

    List<Recibo> findByEstadoPagoAndFechaVencimientoBetween(EstadoPago estadoPago, LocalDate fechaInicio,
            LocalDate fechaFin);

    Page<Recibo> findByContratoIdAndEstadoPagoIn(Integer contratoId, List<EstadoPago> estados, Pageable pageable);

    @Query("SELECT COALESCE(SUM(r.montoTotal), 0) FROM Recibo r WHERE r.estadoPago = :estado")
    BigDecimal sumMontoTotalByEstado(@Param("estado") EstadoPago estado);

    @Query("SELECT r.estadoPago FROM Recibo r")
    List<EstadoPago> findAllEstadosPagos();

    @Query("SELECT COALESCE(SUM(r.montoTotal), 0) FROM Recibo r WHERE r.estadoPago = 'PAGADO' AND MONTH(r.fechaEmision) = MONTH(:fecha) AND YEAR(r.fechaEmision) = YEAR(:fecha)")
    BigDecimal sumIngresosDeMes(LocalDate fecha);

    @Query("SELECT COUNT(r) > 0 FROM Recibo r WHERE r.contrato.cliente.id = :clienteId AND (r.estadoPago = 'VENCIDO' OR (r.estadoPago = 'PENDIENTE' AND r.fechaVencimiento < CURRENT_DATE))")
    boolean existsDeudaPendienteByClienteId(@Param("clienteId") Integer clienteId);
}
