package pe.idat.BackEndConecta.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import pe.idat.BackEndConecta.entity.Empleado;

import java.util.Optional;

public interface EmpleadoRepository extends JpaRepository<Empleado, Integer> {
    
    Optional<Empleado> findByUsername(String username);
    Boolean existsByUsername(String username);
    Boolean existsByDocumento(String documento);

    @Query(value = "SELECT e.* FROM empleado e JOIN user_role ur ON e.id = ur.user_id JOIN role r ON ur.role_id = r.id WHERE e.id = :id AND r.role_name = :roleName", nativeQuery = true)
    Optional<Empleado> findByIdAndRoleNameNative(@Param("id") Integer id, @Param("roleName") String roleName);

    @Query(value = "SELECT e.* FROM empleado e JOIN user_role ur ON e.id = ur.user_id JOIN role r ON ur.role_id = r.id WHERE r.role_name = :roleName", nativeQuery = true)
    java.util.List<Empleado> findAllByRoleNameNative(@Param("roleName") String roleName);

    @Query("SELECT DISTINCT e FROM Empleado e LEFT JOIN e.roles r WHERE " +
           "(:rol IS NULL OR r.roleName = :rol) AND " +
           "(:tipoDocumento IS NULL OR e.tipoDocumento = :tipoDocumento) AND " +
           "(:criterio IS NULL OR :valor IS NULL " +
           "  OR (:criterio = 'NOMBRE' AND (LOWER(e.nombres) LIKE LOWER(CONCAT('%', :valor, '%')) OR LOWER(e.apellidoPaterno) LIKE LOWER(CONCAT('%', :valor, '%')))) " +
           "  OR (:criterio = 'DOCUMENTO' AND e.documento = :valor) " +
           "  OR (:criterio = 'CELULAR' AND e.celular = :valor))")
    org.springframework.data.domain.Page<Empleado> buscarEmpleadosEstricto(
            @Param("rol") String rol,
            @Param("tipoDocumento") pe.idat.BackEndConecta.entity.enums.TipoDocumento tipoDocumento,
            @Param("criterio") String criterio,
            @Param("valor") String valor,
            org.springframework.data.domain.Pageable pageable);
}
