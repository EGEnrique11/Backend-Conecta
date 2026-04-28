package pe.idat.BackEndConecta.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pe.idat.BackEndConecta.dto.AsignarTecnicoDTO;
import pe.idat.BackEndConecta.dto.InstalacionPendienteDTO;
import pe.idat.BackEndConecta.entity.Empleado;
import pe.idat.BackEndConecta.entity.Instalacion;
import pe.idat.BackEndConecta.entity.enums.EstadoInstalacion;
import pe.idat.BackEndConecta.repository.EmpleadoRepository;
import pe.idat.BackEndConecta.repository.InstalacionRepository;
import pe.idat.BackEndConecta.service.DespachoService;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class DespachoServiceImpl implements DespachoService {

    private final InstalacionRepository instalacionRepository;
    private final EmpleadoRepository empleadoRepository;

    @Override
    @Transactional(readOnly = true)
    public List<InstalacionPendienteDTO> obtenerPendientesPorFechaYFranja(LocalDate fecha, String franja) {
        List<Instalacion> pendientes = instalacionRepository.findPendientesByFranja(fecha, franja);
        
        return pendientes.stream().map(inst -> InstalacionPendienteDTO.builder()
                .id(inst.getId())
                .contratoId(inst.getContrato().getId())
                .nombreCliente(inst.getContrato().getCliente().getNombres() + " " + inst.getContrato().getCliente().getApellidoPaterno())
                .direccionCompleta(inst.getContrato().getDireccion().getDireccionCompleta())
                .fechaProgramada(inst.getFechaProgramada())
                .franjaHoraria(inst.getFranjaHoraria())
                .build()
        ).collect(Collectors.toList());
    }

    @Override
    @Transactional
    public Map<String, String> asignarTecnicoABloque(Integer instalacionId, AsignarTecnicoDTO dto) {
        // 1. Validar Instalación
        Instalacion instalacion = buscarInstalacionValidada(instalacionId);

        // 2. Validar Técnico y su Rol
        Empleado tecnico = buscarTecnicoValidado(dto.getTecnicoId());

        // 3. Validación de Cruces de Horario (SRP rules aplicadas localmente)
        validarCrucesDeHorario(dto.getTecnicoId(), instalacion.getFechaProgramada(), dto.getBloqueAsignado());

        // 4. Persistencia
        instalacion.setTecnico(tecnico);
        instalacion.setBloqueAsignado(dto.getBloqueAsignado());
        instalacionRepository.save(instalacion);

        return Map.of("mensaje", "Técnico asignado correctamente al bloque " + dto.getBloqueAsignado());
    }

    @Override
    @Transactional
    public Map<String, String> actualizarEstado(Integer instalacionId, EstadoInstalacion estado) {
        Instalacion instalacion = instalacionRepository.findById(instalacionId)
                .orElseThrow(() -> new IllegalArgumentException("Instalación no encontrada con ID: " + instalacionId));
        
        instalacion.setEstado(estado);
        instalacionRepository.save(instalacion);

        // TODO: Si el estado es COMPLETADA, deberíamos cambiar el estado del Contrato a ACTIVO
        // Pero eso podría ir en InstalacionService. Por ahora, solo actualizamos el estado de la instalación.

        return Map.of("mensaje", "Estado de la instalación actualizado a: " + estado.name());
    }

    @Override
    @Transactional(readOnly = true)
    public List<InstalacionPendienteDTO> obtenerAgendaTecnico(Integer mes, Integer anio, String username) {
        Empleado tecnico = empleadoRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("Técnico no encontrado con el username: " + username));
        
        List<Instalacion> agenda = instalacionRepository.findByTecnicoIdAndMesAndAnio(tecnico.getId(), mes, anio);

        return agenda.stream().map(inst -> InstalacionPendienteDTO.builder()
                .id(inst.getId())
                .contratoId(inst.getContrato().getId())
                .nombreCliente(inst.getContrato().getCliente().getNombres() + " " + inst.getContrato().getCliente().getApellidoPaterno())
                .direccionCompleta(inst.getContrato().getDireccion().getDireccionCompleta())
                .fechaProgramada(inst.getFechaProgramada())
                .franjaHoraria(inst.getFranjaHoraria())
                .estado(inst.getEstado().name())
                .build()
        ).collect(Collectors.toList());
    }

    // --- MÉTODOS PRIVADOS SRP ---

    private Instalacion buscarInstalacionValidada(Integer id) {
        Instalacion instalacion = instalacionRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Instalación no encontrada."));

        if (instalacion.getEstado() != EstadoInstalacion.PENDIENTE && 
            instalacion.getEstado() != EstadoInstalacion.REPROGRAMADA) {
            throw new IllegalArgumentException("La instalación no está pendiente ni reprogramada.");
        }
        return instalacion;
    }

    private Empleado buscarTecnicoValidado(Integer tecnicoId) {
        // Validación 2: Verifica que exista y tenga ROLE_TECNICO
        return empleadoRepository.findByIdAndRoleNameNative(tecnicoId, "ROLE_TECNICO")
                .orElseThrow(() -> new IllegalArgumentException("El empleado no existe o no tiene el rol de Técnico asignado."));
    }

    private void validarCrucesDeHorario(Integer tecnicoId, LocalDate fecha, String bloque) {
        boolean conflicto = instalacionRepository.existsByTecnicoIdAndBloque(tecnicoId, fecha, bloque);
        if (conflicto) {
            throw new IllegalArgumentException("El técnico ya tiene una instalación asignada en ese bloque horario para la fecha seleccionada.");
        }
    }
}
