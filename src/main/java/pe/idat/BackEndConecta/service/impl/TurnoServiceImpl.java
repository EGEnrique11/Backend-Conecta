package pe.idat.BackEndConecta.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pe.idat.BackEndConecta.dto.BloqueHorarioRequestDTO;
import pe.idat.BackEndConecta.dto.TurnoRequestDTO;
import pe.idat.BackEndConecta.entity.BloqueHorario;
import pe.idat.BackEndConecta.entity.Empleado;
import pe.idat.BackEndConecta.entity.Turno;
import pe.idat.BackEndConecta.repository.BloqueHorarioRepository;
import pe.idat.BackEndConecta.repository.EmpleadoRepository;
import pe.idat.BackEndConecta.repository.TurnoRepository;
import pe.idat.BackEndConecta.service.TurnoService;

import java.util.List;

@Service
@RequiredArgsConstructor
public class TurnoServiceImpl implements TurnoService {

    private final TurnoRepository turnoRepository;
    private final BloqueHorarioRepository bloqueHorarioRepository;
    private final EmpleadoRepository empleadoRepository;

    @Override
    @Transactional
    public Turno crearTurno(TurnoRequestDTO dto) {
        long count = turnoRepository.count();
        String nombre = "T" + (count + 1);
        
        Turno turno = Turno.builder()
                .nombre(nombre)
                .horaInicio(dto.getHoraInicio())
                .horaFin(dto.getHoraFin())
                .build();
                
        Turno savedTurno = turnoRepository.save(turno);

        // Generar bloques 4-1-4
        java.time.LocalTime current = dto.getHoraInicio();
        
        // Bloque 1: 2 horas
        crearBloqueInterno(savedTurno, current, current.plusHours(2), false);
        current = current.plusHours(2);
        
        // Bloque 2: 2 horas
        crearBloqueInterno(savedTurno, current, current.plusHours(2), false);
        current = current.plusHours(2);
        
        // Bloque 3: 1 hora (Refrigerio)
        crearBloqueInterno(savedTurno, current, current.plusHours(1), true);
        current = current.plusHours(1);
        
        // Bloque 4: 2 horas
        crearBloqueInterno(savedTurno, current, current.plusHours(2), false);
        current = current.plusHours(2);
        
        // Bloque 5: Restante (2 horas típicamente)
        if (current.isBefore(dto.getHoraFin())) {
            crearBloqueInterno(savedTurno, current, dto.getHoraFin(), false);
        }

        return savedTurno;
    }

    @Override
    @Transactional
    public BloqueHorario agregarBloqueATurno(Integer turnoId, BloqueHorarioRequestDTO dto) {
        Turno turno = turnoRepository.findById(turnoId)
                .orElseThrow(() -> new IllegalArgumentException("Turno no encontrado"));

        if (dto.getHoraInicio().isBefore(turno.getHoraInicio()) || dto.getHoraFin().isAfter(turno.getHoraFin())) {
            throw new IllegalArgumentException("El bloque está fuera del límite del turno");
        }

        BloqueHorario bloque = BloqueHorario.builder()
                .turno(turno)
                .esRefrigerio(dto.getEsRefrigerio() != null ? dto.getEsRefrigerio() : false)
                .horaInicio(dto.getHoraInicio())
                .horaFin(dto.getHoraFin())
                .build();

        return bloqueHorarioRepository.save(bloque);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Turno> obtenerTurnos() {
        return turnoRepository.findAll();
    }

    @Override
    @Transactional(readOnly = true)
    public List<BloqueHorario> obtenerBloquesPorTurno(Integer turnoId) {
        return bloqueHorarioRepository.findByTurnoId(turnoId);
    }

    @Override
    @Transactional
    public BloqueHorario actualizarBloque(Integer bloqueId, BloqueHorarioRequestDTO dto) {
        BloqueHorario bloque = bloqueHorarioRepository.findById(bloqueId)
                .orElseThrow(() -> new IllegalArgumentException("Bloque no encontrado"));
                
        bloque.setHoraInicio(dto.getHoraInicio());
        bloque.setHoraFin(dto.getHoraFin());
        bloque.setEsRefrigerio(dto.getEsRefrigerio() != null ? dto.getEsRefrigerio() : false);
        
        return bloqueHorarioRepository.save(bloque);
    }

    @Override
    @Transactional
    public void eliminarBloque(Integer bloqueId) {
        bloqueHorarioRepository.deleteById(bloqueId);
    }

    @Override
    @Transactional(readOnly = true)
    public Turno obtenerTurnoDeTecnico(Integer tecnicoId) {
        Empleado tecnico = empleadoRepository.findById(tecnicoId)
                .orElseThrow(() -> new IllegalArgumentException("Técnico no encontrado"));
        return tecnico.getTurno();
    }

    @Override
    @Transactional
    public void asignarTurnoATecnico(Integer tecnicoId, Integer turnoId) {
        Empleado tecnico = empleadoRepository.findById(tecnicoId)
                .orElseThrow(() -> new IllegalArgumentException("Técnico no encontrado"));
        Turno turno = turnoRepository.findById(turnoId)
                .orElseThrow(() -> new IllegalArgumentException("Turno no encontrado"));
        tecnico.setTurno(turno);
        empleadoRepository.save(tecnico);
    }

    private void crearBloqueInterno(Turno turno, java.time.LocalTime inicio, java.time.LocalTime fin, boolean esRefrigerio) {
        if (inicio.isBefore(turno.getHoraFin())) {
            java.time.LocalTime realFin = fin.isAfter(turno.getHoraFin()) ? turno.getHoraFin() : fin;
            BloqueHorario bloque = BloqueHorario.builder()
                    .turno(turno)
                    .esRefrigerio(esRefrigerio)
                    .horaInicio(inicio)
                    .horaFin(realFin)
                    .build();
            bloqueHorarioRepository.save(bloque);
        }
    }
}
