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
    private final pe.idat.BackEndConecta.repository.BloqueHorarioRepository bloqueRepository;

    @Override
    @Transactional(readOnly = true)
    public List<InstalacionPendienteDTO> obtenerPendientesPorFechaYFranja(LocalDate fecha, String franja) {
        // Franja is deprecated. The UI currently sends it, but we can ignore it or use it to filter Bloques later.
        List<Instalacion> instalaciones = instalacionRepository.findPendientes(fecha);

        return instalaciones.stream().map(inst -> {
            InstalacionPendienteDTO dto = new InstalacionPendienteDTO();
            dto.setId(inst.getId());
            dto.setContratoId(inst.getContrato().getId());
            dto.setNombreCliente(inst.getContrato().getCliente().getNombres() + " " + inst.getContrato().getCliente().getApellidoPaterno());
            dto.setDireccionCompleta(inst.getContrato().getDireccion().getDireccionCompleta());
            dto.setFechaProgramada(inst.getFechaProgramada());
            if (inst.getBloqueHorario() != null) {
                dto.setFranjaHoraria(inst.getBloqueHorario().getHoraInicio().toString() + " - " + inst.getBloqueHorario().getHoraFin().toString());
            } else {
                dto.setFranjaHoraria("SIN ASIGNAR");
            }
            dto.setEstado(inst.getEstado().name());
            return dto;
        }).collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<InstalacionPendienteDTO> obtenerAsignadasPorFecha(LocalDate fecha) {
        List<Instalacion> instalaciones = instalacionRepository.findRutasActivas(fecha);

        return instalaciones.stream().map(inst -> {
            InstalacionPendienteDTO dto = new InstalacionPendienteDTO();
            dto.setId(inst.getId());
            dto.setContratoId(inst.getContrato().getId());
            dto.setNombreCliente(inst.getContrato().getCliente().getNombres() + " " + inst.getContrato().getCliente().getApellidoPaterno());
            dto.setDireccionCompleta(inst.getContrato().getDireccion().getDireccionCompleta());
            dto.setFechaProgramada(inst.getFechaProgramada());
            if (inst.getBloqueHorario() != null) {
                dto.setFranjaHoraria(inst.getBloqueHorario().getHoraInicio().toString() + " - " + inst.getBloqueHorario().getHoraFin().toString());
            } else {
                dto.setFranjaHoraria("SIN ASIGNAR");
            }
            dto.setEstado(inst.getEstado().name());
            dto.setTecnicoNombre(inst.getTecnico() != null ? inst.getTecnico().getNombres() + " " + inst.getTecnico().getApellidoPaterno() : "Desconocido");
            return dto;
        }).collect(Collectors.toList());
    }

    @Override
    @Transactional
    public Map<String, String> asignarTecnicoABloque(Integer instalacionId, AsignarTecnicoDTO dto) {
        Instalacion instalacion = instalacionRepository.findById(instalacionId)
                .orElseThrow(() -> new IllegalArgumentException("Instalación no encontrada"));

        Empleado tecnico = empleadoRepository.findById(dto.getTecnicoId())
                .orElseThrow(() -> new IllegalArgumentException("Técnico no encontrado"));

        if (!tecnico.getRoles().stream().anyMatch(r -> r.getRoleName().equals("ROLE_TECNICO"))) {
            throw new IllegalArgumentException("El empleado seleccionado no es un técnico");
        }

        pe.idat.BackEndConecta.entity.BloqueHorario bloque = null;
        if (dto.getBloqueId() != null) {
            bloque = bloqueRepository.findById(dto.getBloqueId())
                .orElseThrow(() -> new IllegalArgumentException("Bloque no encontrado"));
                
            boolean bloqueOcupado = instalacionRepository.existsByTecnicoIdAndBloqueId(tecnico.getId(), instalacion.getFechaProgramada(), bloque.getId());
            if (bloqueOcupado) {
                throw new IllegalArgumentException("El técnico ya tiene una instalación asignada en este bloque");
            }
            instalacion.setBloqueHorario(bloque);
        }

        instalacion.setTecnico(tecnico);
        
        // Cambio Automático: De PENDIENTE a EN_RUTA
        if (instalacion.getEstado() == EstadoInstalacion.PENDIENTE) {
            instalacion.setEstado(EstadoInstalacion.EN_RUTA);
        }

        instalacionRepository.save(instalacion);

        Map<String, String> response = new java.util.HashMap<>();
        response.put("message", "Técnico asignado correctamente al bloque.");
        return response;
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

        return agenda.stream().map(inst -> {
            InstalacionPendienteDTO dto = new InstalacionPendienteDTO();
            dto.setId(inst.getId());
            dto.setContratoId(inst.getContrato().getId());
            dto.setNombreCliente(inst.getContrato().getCliente().getNombres() + " " + inst.getContrato().getCliente().getApellidoPaterno());
            dto.setDireccionCompleta(inst.getContrato().getDireccion().getDireccionCompleta());
            dto.setFechaProgramada(inst.getFechaProgramada());
            if (inst.getBloqueHorario() != null) {
                dto.setFranjaHoraria(inst.getBloqueHorario().getHoraInicio().toString() + " - " + inst.getBloqueHorario().getHoraFin().toString());
            } else {
                dto.setFranjaHoraria("SIN ASIGNAR");
            }
            dto.setEstado(inst.getEstado().name());
            return dto;
        }).collect(Collectors.toList());
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
        return empleadoRepository.findByIdAndRoleName(tecnicoId, "ROLE_TECNICO")
                .orElseThrow(() -> new IllegalArgumentException("El empleado no existe o no tiene el rol de Técnico asignado."));
    }

}
