package pe.idat.BackEndConecta.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import pe.idat.BackEndConecta.entity.Distrito;

import java.util.List;

public interface DistritoRepository extends JpaRepository<Distrito, Integer> {
    List<Distrito> findByProvinciaId(Integer provinciaId);
}
