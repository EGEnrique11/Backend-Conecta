package pe.idat.BackEndConecta.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import pe.idat.BackEndConecta.entity.Cliente;

import java.util.Optional;

public interface ClienteRepository extends JpaRepository<Cliente, Integer> {

    Optional<Cliente> findByDocumento(String documento);

    @Query("SELECT c FROM Cliente c WHERE " +
           "(:documento IS NULL OR c.documento = :documento) AND " +
           "(:celular IS NULL OR c.celular = :celular) AND " +
           "(:id IS NULL OR c.id = :id) AND " +
           "(:nombres IS NULL OR LOWER(c.nombres) LIKE LOWER(CONCAT('%', :nombres, '%'))) AND " +
           "(:apellidoPaterno IS NULL OR LOWER(c.apellidoPaterno) LIKE LOWER(CONCAT('%', :apellidoPaterno, '%')))")
    Page<Cliente> buscarClientesConFiltros(@Param("documento") String documento, 
                                           @Param("celular") String celular, 
                                           @Param("id") Integer id, 
                                           @Param("nombres") String nombres, 
                                           @Param("apellidoPaterno") String apellidoPaterno, 
                                           Pageable pageable);
}
