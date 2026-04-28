package pe.idat.BackEndConecta.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import pe.idat.BackEndConecta.entity.HistorialSuspension;

import java.util.List;

public interface HistorialSuspensionRepository extends JpaRepository<HistorialSuspension, Integer> {
    List<HistorialSuspension> findByContratoIdAndAplicadoEnReciboIsNull(Integer contratoId);
    
    List<HistorialSuspension> findByContratoIdAndFechaReactivacionIsNull(Integer contratoId);
}
