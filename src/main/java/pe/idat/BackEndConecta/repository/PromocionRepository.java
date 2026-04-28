package pe.idat.BackEndConecta.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import pe.idat.BackEndConecta.entity.Promocion;

import java.util.List;

public interface PromocionRepository extends JpaRepository<Promocion, Integer> {
    List<Promocion> findByPlanes_Id(Integer planId);
}
