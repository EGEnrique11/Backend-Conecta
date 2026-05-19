package pe.idat.BackEndConecta.service.impl;

import lombok.RequiredArgsConstructor;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pe.idat.BackEndConecta.dto.InstalacionObservacionDTO;
import pe.idat.BackEndConecta.dto.InstalacionReprogramarDTO;
import pe.idat.BackEndConecta.entity.Cliente;
import pe.idat.BackEndConecta.entity.Contrato;
import pe.idat.BackEndConecta.entity.Instalacion;
import pe.idat.BackEndConecta.entity.enums.EstadoCliente;
import pe.idat.BackEndConecta.entity.enums.EstadoContrato;
import pe.idat.BackEndConecta.entity.enums.EstadoInstalacion;
import pe.idat.BackEndConecta.event.ContratoActivoEvent;
import pe.idat.BackEndConecta.repository.ClienteRepository;
import pe.idat.BackEndConecta.repository.ContratoRepository;
import pe.idat.BackEndConecta.repository.EmpleadoRepository;
import pe.idat.BackEndConecta.repository.InstalacionRepository;
import pe.idat.BackEndConecta.service.InstalacionService;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class InstalacionServiceImpl implements InstalacionService {

    private final InstalacionRepository instalacionRepository;
    private final ContratoRepository contratoRepository;
    private final ClienteRepository clienteRepository;
    private final EmpleadoRepository empleadoRepository;
    private final ApplicationEventPublisher eventPublisher;

    @Override
    @Transactional
    public Map<String, String> completarInstalacion(Integer instalacionId, InstalacionObservacionDTO dto) {
        Instalacion instalacion = obtenerInstalacion(instalacionId);
        validarParaCompletar(instalacion.getEstado());

        instalacion.setEstado(EstadoInstalacion.COMPLETADA);
        agregarObservacion(instalacion, dto);
        instalacionRepository.save(instalacion);

        activarContratoYPromocion(instalacion.getContrato());
        activarClienteSiCorresponde(instalacion.getContrato().getCliente());
        // Se anuncia el evento para poder generar el pdf del contrato
        eventPublisher.publishEvent(new ContratoActivoEvent(this, instalacion.getContrato().getId()));
        return Map.of("mensaje", "La instalación ha sido completada y el contrato activado correctamente.");
    }

    @Override
    @Transactional
    public Map<String, String> cancelarInstalacion(Integer instalacionId, InstalacionObservacionDTO dto) {
        Instalacion instalacion = obtenerInstalacion(instalacionId);
        validarParaCancelar(instalacion.getEstado());

        instalacion.setEstado(EstadoInstalacion.CANCELADA);
        instalacion.setTecnico(null);
        instalacion.setBloqueHorario(null);
        agregarObservacion(instalacion, dto);
        instalacionRepository.save(instalacion);

        Contrato contrato = instalacion.getContrato();
        contrato.setEstado(EstadoContrato.CANCELADO);
        contratoRepository.save(contrato);

        return Map.of("mensaje", "La instalación y el contrato han sido cancelados.");
    }

    @Override
    @Transactional
    public Map<String, String> reprogramarInstalacion(Integer instalacionId, InstalacionReprogramarDTO dto) {
        Instalacion instalacion = obtenerInstalacion(instalacionId);
        validarParaReprogramar(instalacion.getEstado());

        validarCuposDisponibles(dto.getNuevaFecha(), instalacion.getFechaProgramada());

        instalacion.setFechaProgramada(dto.getNuevaFecha());
        instalacion.setTecnico(null);
        instalacion.setBloqueHorario(null);
        instalacion.setEstado(EstadoInstalacion.REPROGRAMADA);

        if (dto.getMotivo() != null && !dto.getMotivo().isEmpty()) {
            String observacionesAntiguas = instalacion.getObservaciones() != null
                    ? instalacion.getObservaciones() + " | "
                    : "";
            instalacion.setObservaciones(observacionesAntiguas + "Motivo de Reprogramación: " + dto.getMotivo());
        }

        instalacionRepository.save(instalacion);

        return Map.of("mensaje", "La instalación ha sido reprogramada con éxito.");
    }

    @Override
    @Transactional
    public Map<String, String> iniciarInstalacion(Integer instalacionId) {
        // Obtener
        Instalacion instalacion = obtenerInstalacion(instalacionId);
        // Validar
        validarParaIniciar(instalacion.getEstado());
        // Ejecutar la logica
        instalacion.setEstado(EstadoInstalacion.EN_PROCESO);
        String observacionesAntiguas = instalacion.getObservaciones() != null ? instalacion.getObservaciones() + " | "
                : "";
        instalacion.setObservaciones(observacionesAntiguas + "Instalación iniciada en campo por el técnico.");
        instalacionRepository.save(instalacion);
        return Map.of("mensaje", "Instalación iniciada con éxito.");
    }

    // Metodos
    private Instalacion obtenerInstalacion(Integer instalacionId) {
        return instalacionRepository.findById(instalacionId)
                .orElseThrow(() -> new IllegalArgumentException("Instalacion no encontrada con ID: " + instalacionId));
    }

    private void validarParaIniciar(EstadoInstalacion estado) {
        if (estado != EstadoInstalacion.EN_RUTA) {
            throw new IllegalArgumentException(
                    "La instalación no se puede iniciar en su estado actual (" + estado + "). " +
                            "Debe estar EN_RUTA (asignada a un técnico).");
        }
    }

    private void validarParaCompletar(EstadoInstalacion estado) {
        if (estado != EstadoInstalacion.EN_PROCESO) {
            throw new IllegalArgumentException(
                    "La instalación no se puede completar en su estado actual (" + estado + "). " +
                            "Primero debe ser iniciada.");
        }
    }

    private void validarParaCancelar(EstadoInstalacion estado) {
        if (estado == EstadoInstalacion.COMPLETADA || estado == EstadoInstalacion.CANCELADA) {
            throw new IllegalArgumentException(
                    "La instalación no se puede cancelar en su estado actual (" + estado + ").");
        }
    }

    private void validarParaReprogramar(EstadoInstalacion estado) {
        if (estado == EstadoInstalacion.COMPLETADA || estado == EstadoInstalacion.CANCELADA) {
            throw new IllegalArgumentException(
                    "La instalacion no se puede reprogramar en su estado actual (" + estado + ").");
        }
    }

    private void agregarObservacion(Instalacion instalacion, InstalacionObservacionDTO dto) {
        if (dto != null && dto.getObservaciones() != null && !dto.getObservaciones().isEmpty()) {
            String observacionesAntiguas = instalacion.getObservaciones() != null
                    ? instalacion.getObservaciones() + " | "
                    : "";
            instalacion.setObservaciones(observacionesAntiguas + dto.getObservaciones());
        }
    }

    private void activarContratoYPromocion(Contrato contrato) {
        LocalDate hoy = LocalDate.now();
        contrato.setEstado(EstadoContrato.ACTIVO);
        contrato.setFechaActivacion(hoy);

        if (contrato.getFechaFinPromocion() != null) {
            long diferenciaDias = ChronoUnit.DAYS.between(contrato.getFechaContrato(), hoy);
            if (diferenciaDias > 0) {
                contrato.setFechaFinPromocion(contrato.getFechaFinPromocion().plusDays(diferenciaDias));
            }
        }
        contratoRepository.save(contrato);
    }

    private void activarClienteSiCorresponde(Cliente cliente) {
        if (cliente.getEstado() == EstadoCliente.PRECLIENTE) {
            cliente.setEstado(EstadoCliente.ACTIVO);
            clienteRepository.save(cliente);
        }
    }

    private void validarCuposDisponibles(LocalDate nuevaFecha, LocalDate fechaActual) {
        long totalTecnicos = empleadoRepository.count();
        long cuposMaximos = totalTecnicos * 2;
        // 1. Definimos los estados que contabilizan como "cupo ocupado"
        List<EstadoInstalacion> estadosQueOcupanCupo = List.of(EstadoInstalacion.PENDIENTE,
                EstadoInstalacion.REPROGRAMADA);
        // 2. Pasamos la lista de Enums al repositorio
        Long agendadas = instalacionRepository.countInstalacionesEnFecha(nuevaFecha, estadosQueOcupanCupo);
        boolean isSameSlot = fechaActual.equals(nuevaFecha);
        long efectivasAgendadas = isSameSlot ? (agendadas - 1) : agendadas;

        if (efectivasAgendadas >= cuposMaximos) {
            throw new IllegalArgumentException("Cupos agotados para el día " + nuevaFecha);
        }
    }
}
