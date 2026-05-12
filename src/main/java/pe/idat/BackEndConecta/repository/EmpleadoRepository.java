package pe.idat.BackEndConecta.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import pe.idat.BackEndConecta.entity.Empleado;
import pe.idat.BackEndConecta.entity.enums.TipoDocumento;

import java.util.Optional;

public interface EmpleadoRepository extends JpaRepository<Empleado, Integer> {
    
    Optional<Empleado> findByUsername(String username);
    Boolean existsByUsername(String username);
    Boolean existsByDocumento(String documento);

    @Query("SELECT e FROM Empleado e JOIN e.roles r WHERE e.id = :id AND r.roleName = :roleName")
    Optional<Empleado> findByIdAndRoleName(@Param("id") Integer id, @Param("roleName") String roleName);

    @Query("SELECT e FROM Empleado e JOIN e.roles r WHERE r.roleName = :roleName")
    java.util.List<Empleado> findAllByRoleName(@Param("roleName") String roleName);

    @Query("SELECT DISTINCT e FROM Empleado e LEFT JOIN e.roles r WHERE " +
           "(:rol IS NULL OR r.roleName = :rol) AND " +
           "(:tipoDocumento IS NULL OR e.tipoDocumento = :tipoDocumento) AND " +
           "(:criterio IS NULL OR :valor IS NULL " +
           "  OR (:criterio = 'NOMBRE' AND (LOWER(e.nombres) LIKE LOWER(CONCAT('%', :valor, '%')) OR LOWER(e.apellidoPaterno) LIKE LOWER(CONCAT('%', :valor, '%')))) " +
           "  OR (:criterio = 'DOCUMENTO' AND e.documento = :valor) " +
           "  OR (:criterio = 'CELULAR' AND e.celular = :valor))")
    Page<Empleado> buscarEmpleadosEstricto(
            @Param("rol") String rol,
            @Param("tipoDocumento") TipoDocumento tipoDocumento,
            @Param("criterio") String criterio,
            @Param("valor") String valor,
            Pageable pageable);

    @Query("SELECT DISTINCT e FROM Empleado e LEFT JOIN e.roles r WHERE r.roleName = 'ROLE_TECNICO' AND " +
           "(LOWER(e.nombres) LIKE LOWER(CONCAT('%', :term, '%')) " +
           "OR LOWER(e.apellidoPaterno) LIKE LOWER(CONCAT('%', :term, '%')) " +
           "OR e.documento LIKE CONCAT('%', :term, '%') " +
           "OR e.celular LIKE CONCAT('%', :term, '%'))")
    java.util.List<Empleado> buscarTecnicosPorTermino(@Param("term") String term);

    Page<Empleado> findByRolesRoleName(String roleName, Pageable pageable);

    @Query("SELECT DISTINCT e FROM Empleado e LEFT JOIN e.roles r WHERE r.roleName = 'ROLE_TECNICO' AND " +
           "(LOWER(e.nombres) LIKE LOWER(CONCAT('%', :term, '%')) " +
           "OR LOWER(e.apellidoPaterno) LIKE LOWER(CONCAT('%', :term, '%')) " +
           "OR e.documento LIKE CONCAT('%', :term, '%') " +
           "OR e.celular LIKE CONCAT('%', :term, '%'))")
    Page<Empleado> buscarTecnicosPaginadoPorTermino(@Param("term") String term, Pageable pageable);
}
