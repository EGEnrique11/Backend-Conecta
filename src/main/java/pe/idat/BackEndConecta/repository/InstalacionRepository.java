package pe.idat.BackEndConecta.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import pe.idat.BackEndConecta.entity.Instalacion;

import java.time.LocalDate;
import java.util.List;

public interface InstalacionRepository extends JpaRepository<Instalacion, Integer> {

    @Query("SELECT COUNT(i) FROM Instalacion i WHERE i.contrato.cliente.id = :clienteId AND i.estado = 'PENDIENTE'")
    Long countPendientesByClienteId(@Param("clienteId") Integer clienteId);

    @Query("SELECT COUNT(i) FROM Instalacion i WHERE i.fechaProgramada = :fecha AND i.franjaHoraria = :franja AND i.estado IN ('PENDIENTE', 'REPROGRAMADA')")
    Long countInstalacionesEnFranja(@Param("fecha") LocalDate fecha, @Param("franja") String franja);

    @Query("SELECT i FROM Instalacion i WHERE i.fechaProgramada = :fecha AND i.franjaHoraria = :franja AND i.estado IN ('PENDIENTE', 'REPROGRAMADA')")
    List<Instalacion> findPendientesByFranja(@Param("fecha") LocalDate fecha, @Param("franja") String franja);

    @Query("SELECT CASE WHEN COUNT(i) > 0 THEN true ELSE false END FROM Instalacion i WHERE i.tecnico.id = :tecnicoId AND i.fechaProgramada = :fecha AND i.bloqueAsignado = :bloque AND i.estado != 'CANCELADA'")
    boolean existsByTecnicoIdAndBloque(@Param("tecnicoId") Integer tecnicoId, @Param("fecha") LocalDate fecha, @Param("bloque") String bloque);

    @Query("SELECT i.tecnico.nombres AS tecnico, " +
           "SUM(CASE WHEN i.estado = 'COMPLETADA' THEN 1 ELSE 0 END) AS completadas, " +
           "SUM(CASE WHEN i.estado = 'CANCELADA' THEN 1 ELSE 0 END) AS canceladas, " +
           "SUM(CASE WHEN i.estado = 'REPROGRAMADA' THEN 1 ELSE 0 END) AS reprogramadas " +
           "FROM Instalacion i WHERE i.tecnico IS NOT NULL GROUP BY i.tecnico.id, i.tecnico.nombres")
    List<pe.idat.BackEndConecta.dto.projections.ProductividadTecnicaProjection> findProductividadTecnica();

    @Query("SELECT i.estado FROM Instalacion i WHERE MONTH(i.fechaProgramada) = MONTH(:fecha) AND YEAR(i.fechaProgramada) = YEAR(:fecha)")
    List<pe.idat.BackEndConecta.entity.enums.EstadoInstalacion> findEstadosInstalacionPorMes(@Param("fecha") LocalDate fecha);

    Long countByEstadoAndFechaProgramada(pe.idat.BackEndConecta.entity.enums.EstadoInstalacion estado, LocalDate fecha);

    @Query("SELECT i FROM Instalacion i WHERE i.tecnico.id = :tecnicoId AND MONTH(i.fechaProgramada) = :mes AND YEAR(i.fechaProgramada) = :anio ORDER BY i.fechaProgramada ASC, i.bloqueAsignado ASC")
    List<Instalacion> findByTecnicoIdAndMesAndAnio(@Param("tecnicoId") Integer tecnicoId, @Param("mes") Integer mes, @Param("anio") Integer anio);
}
