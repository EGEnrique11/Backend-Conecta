package pe.idat.BackEndConecta.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import pe.idat.BackEndConecta.entity.EfectoPromocion;

import java.util.List;

public interface EfectoPromocionRepository extends JpaRepository<EfectoPromocion, Integer> {
    List<EfectoPromocion> findByPromocionId(Integer promocionId);
}
