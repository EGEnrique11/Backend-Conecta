package pe.idat.BackEndConecta.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import pe.idat.BackEndConecta.entity.Turno;

import java.util.Optional;

public interface TurnoRepository extends JpaRepository<Turno, Integer> {
    Optional<Turno> findByNombre(String nombre);
}
