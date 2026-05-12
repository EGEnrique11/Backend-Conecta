package pe.idat.BackEndConecta.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import pe.idat.BackEndConecta.entity.BloqueHorario;

import java.util.List;

public interface BloqueHorarioRepository extends JpaRepository<BloqueHorario, Integer> {
    List<BloqueHorario> findByTurnoId(Integer turnoId);
}
