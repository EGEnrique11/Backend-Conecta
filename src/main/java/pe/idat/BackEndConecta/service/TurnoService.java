package pe.idat.BackEndConecta.service;

import pe.idat.BackEndConecta.dto.BloqueHorarioRequestDTO;
import pe.idat.BackEndConecta.dto.TurnoRequestDTO;
import pe.idat.BackEndConecta.entity.BloqueHorario;
import pe.idat.BackEndConecta.entity.Turno;

import java.util.List;

public interface TurnoService {
    Turno crearTurno(TurnoRequestDTO dto);
    BloqueHorario agregarBloqueATurno(Integer turnoId, BloqueHorarioRequestDTO dto);
    List<Turno> obtenerTurnos();
    List<BloqueHorario> obtenerBloquesPorTurno(Integer turnoId);
    BloqueHorario editarBloque(Integer bloqueId, BloqueHorarioRequestDTO dto);
    void eliminarBloque(Integer bloqueId);
    Turno obtenerTurnoDeTecnico(Integer tecnicoId);
    void asignarTurnoATecnico(Integer tecnicoId, Integer turnoId);
}
