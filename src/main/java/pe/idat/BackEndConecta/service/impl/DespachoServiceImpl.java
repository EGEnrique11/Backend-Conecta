package pe.idat.BackEndConecta.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pe.idat.BackEndConecta.dto.AsignarTecnicoDTO;
import pe.idat.BackEndConecta.dto.InstalacionPendienteDTO;
import pe.idat.BackEndConecta.dto.InstalacionReprogramarDTO;
import pe.idat.BackEndConecta.entity.BloqueHorario;
import pe.idat.BackEndConecta.entity.Contrato;
import pe.idat.BackEndConecta.entity.Empleado;
import pe.idat.BackEndConecta.entity.Instalacion;
import pe.idat.BackEndConecta.entity.enums.EstadoContrato;
import pe.idat.BackEndConecta.entity.enums.EstadoInstalacion;
import pe.idat.BackEndConecta.repository.BloqueHorarioRepository;
import pe.idat.BackEndConecta.repository.ContratoRepository;
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
    private final BloqueHorarioRepository bloqueRepository;
    private final ContratoRepository contratoRepository;

    @Override
    @Transactional(readOnly = true)
    public List<InstalacionPendienteDTO> obtenerPendientesPorFechaYFranja(LocalDate fecha, String franja) {
        List<EstadoInstalacion> estadosValidos = List.of(EstadoInstalacion.PENDIENTE, EstadoInstalacion.REPROGRAMADA);
        return instalacionRepository.findPendientes(fecha, estadosValidos).stream()
                .map(this::mapearAInstalacionPendienteDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<InstalacionPendienteDTO> obtenerAsignadasPorFecha(LocalDate fecha) {
        List<EstadoInstalacion> estadosValidos = List.of(EstadoInstalacion.PENDIENTE, EstadoInstalacion.REPROGRAMADA);
        return instalacionRepository.findRutasActivas(fecha, estadosValidos).stream()
                .map(this::mapearAInstalacionPendienteDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public Map<String, String> asignarTecnicoABloque(Integer instalacionId, AsignarTecnicoDTO dto) {
        Instalacion instalacion = buscarInstalacionValidada(instalacionId);
        Empleado tecnico = buscarTecnicoValidado(dto.getTecnicoId());

        if (dto.getBloqueId() != null) {
            BloqueHorario bloque = bloqueRepository.findById(dto.getBloqueId())
                    .orElseThrow(() -> new IllegalArgumentException("Bloque no encontrado"));

            boolean bloqueOcupado = instalacionRepository.existsByTecnicoIdAndBloqueId(tecnico.getId(),
                    instalacion.getFechaProgramada(), bloque.getId());
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

        return Map.of("message", "Técnico asignado correctamente al bloque.");
    }

    @Override
    @Transactional
    public Map<String, String> actualizarEstado(Integer instalacionId, EstadoInstalacion estado) {
        Instalacion instalacion = instalacionRepository.findById(instalacionId)
                .orElseThrow(() -> new IllegalArgumentException("Instalación no encontrada con ID: " + instalacionId));

        instalacion.setEstado(estado);
        instalacionRepository.save(instalacion);

        if (estado == EstadoInstalacion.COMPLETADA) {
            Contrato contrato = instalacion.getContrato();
            if (contrato != null) {
                contrato.setEstado(EstadoContrato.ACTIVO);
                contrato.setFechaActivacion(LocalDate.now());
                contratoRepository.save(contrato);
            }
        }

        return Map.of("mensaje", "Estado de la instalación actualizado a: " + estado.name());
    }

    @Override
    @Transactional(readOnly = true)
    public List<InstalacionPendienteDTO> obtenerAgendaTecnico(Integer mes, Integer anio, String username) {
        Empleado tecnico = empleadoRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("Técnico no encontrado con el username: " + username));

        return instalacionRepository.findByTecnicoIdAndMesAndAnio(tecnico.getId(), mes, anio).stream()
                .map(this::mapearAInstalacionPendienteDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<InstalacionPendienteDTO> buscarInstalaciones(String term) {
        return instalacionRepository.buscarPorTermino(term).stream()
                .map(this::mapearAInstalacionPendienteDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public Map<String, String> reprogramarInstalacion(Integer instalacionId, InstalacionReprogramarDTO dto) {
        Instalacion instalacion = instalacionRepository.findById(instalacionId)
                .orElseThrow(() -> new IllegalArgumentException("Instalación no encontrada con ID: " + instalacionId));

        instalacion.setFechaProgramada(dto.getNuevaFecha());
        instalacion.setEstado(EstadoInstalacion.REPROGRAMADA);

        // Liberar técnico y bloque horario
        instalacion.setTecnico(null);
        instalacion.setBloqueHorario(null);

        // Actualizar observaciones
        if (dto.getMotivo() != null && !dto.getMotivo().isEmpty()) {
            String observacionesAntiguas = instalacion.getObservaciones() != null
                    ? instalacion.getObservaciones() + " | "
                    : "";
            instalacion.setObservaciones(observacionesAntiguas + "Motivo de Reprogramación: " + dto.getMotivo());
        }

        instalacionRepository.save(instalacion);

        return Map.of("mensaje", "Instalación reprogramada exitosamente. Recursos liberados.");
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
        return empleadoRepository.findByIdAndRoleName(tecnicoId, "ROLE_TECNICO")
                .orElseThrow(() -> new IllegalArgumentException(
                        "El empleado no existe o no tiene el rol de Técnico asignado."));
    }

    // NUEVO MÉTODO DE MAPEO
    private InstalacionPendienteDTO mapearAInstalacionPendienteDTO(Instalacion inst) {
        InstalacionPendienteDTO dto = new InstalacionPendienteDTO();
        dto.setId(inst.getId());
        dto.setContratoId(inst.getContrato().getId());
        dto.setNombreCliente(inst.getContrato().getCliente().getNombres() + " "
                + inst.getContrato().getCliente().getApellidoPaterno());
        dto.setDireccionCompleta(inst.getContrato().getDireccion().getDireccionCompleta());
        dto.setFechaProgramada(inst.getFechaProgramada());

        if (inst.getBloqueHorario() != null) {
            dto.setFranjaHoraria(inst.getBloqueHorario().getHoraInicio().toString() + " - "
                    + inst.getBloqueHorario().getHoraFin().toString());
        } else {
            dto.setFranjaHoraria("SIN ASIGNAR");
        }

        dto.setEstado(inst.getEstado().name());
        dto.setCelularCliente(inst.getContrato().getCliente().getCelular());
        dto.setDocumentoCliente(inst.getContrato().getCliente().getDocumento());

        // Se unifica la lógica del nombre del técnico para todos los DTOs
        dto.setTecnicoNombre(inst.getTecnico() != null
                ? inst.getTecnico().getNombres() + " " + inst.getTecnico().getApellidoPaterno()
                : "Desconocido");

        return dto;
    }
}
