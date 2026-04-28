package pe.idat.BackEndConecta.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import pe.idat.BackEndConecta.entity.Recibo;
import pe.idat.BackEndConecta.entity.enums.EstadoPago;

import java.time.LocalDate;
import java.util.List;

public interface ReciboRepository extends JpaRepository<Recibo, Integer> {
    List<Recibo> findByEstadoPagoAndFechaVencimientoBefore(EstadoPago estadoPago, LocalDate fechaVencimiento);
    List<Recibo> findByEstadoPagoAndFechaVencimiento(EstadoPago estadoPago, LocalDate fecha);
    List<Recibo> findByEstadoPagoAndFechaVencimientoBetween(EstadoPago estadoPago, LocalDate fechaInicio, LocalDate fechaFin);

    @Query("SELECT COALESCE(SUM(r.montoTotal), 0) FROM Recibo r WHERE r.estadoPago = :estado")
    java.math.BigDecimal sumMontoTotalByEstado(@Param("estado") EstadoPago estado);

    @Query("SELECT r.estadoPago FROM Recibo r")
    List<EstadoPago> findAllEstadosPagos();

    @Query("SELECT COALESCE(SUM(r.montoTotal), 0) FROM Recibo r WHERE r.estadoPago = 'PAGADO' AND MONTH(r.fechaEmision) = MONTH(:fecha) AND YEAR(r.fechaEmision) = YEAR(:fecha)")
    java.math.BigDecimal sumIngresosDeMes(LocalDate fecha);
}
