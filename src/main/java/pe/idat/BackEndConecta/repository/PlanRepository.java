package pe.idat.BackEndConecta.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import pe.idat.BackEndConecta.entity.Plan;

import java.util.List;

public interface PlanRepository extends JpaRepository<Plan, Integer> {
    List<Plan> findByServicioId(Integer servicioId);
}
