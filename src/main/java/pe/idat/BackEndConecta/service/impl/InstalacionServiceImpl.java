package pe.idat.BackEndConecta.service.impl;

import lombok.RequiredArgsConstructor;
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
import pe.idat.BackEndConecta.repository.ClienteRepository;
import pe.idat.BackEndConecta.repository.ContratoRepository;
import pe.idat.BackEndConecta.repository.EmpleadoRepository;
import pe.idat.BackEndConecta.repository.InstalacionRepository;
import pe.idat.BackEndConecta.service.InstalacionService;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class InstalacionServiceImpl implements InstalacionService {

    private final InstalacionRepository instalacionRepository;
    private final ContratoRepository contratoRepository;
    private final ClienteRepository clienteRepository;
    private final EmpleadoRepository empleadoRepository;

    @Override
    @Transactional
    public Map<String, String> completarInstalacion(Integer instalacionId, InstalacionObservacionDTO dto) {
        Instalacion instalacion = buscarYValidarInstalacion(instalacionId, "completar");

        instalacion.setEstado(EstadoInstalacion.COMPLETADA);
        agregarObservacion(instalacion, dto);
        instalacionRepository.save(instalacion);

        activarContratoYPromocion(instalacion.getContrato());
        activarClienteSiCorresponde(instalacion.getContrato().getCliente());

        // TODO: Generar PDF del contrato usando Thymeleaf
        return Map.of("mensaje", "La instalación ha sido completada y el contrato activado correctamente.");
    }

    @Override
    @Transactional
    public Map<String, String> cancelarInstalacion(Integer instalacionId, InstalacionObservacionDTO dto) {
        Instalacion instalacion = buscarYValidarInstalacion(instalacionId, "cancelar");

        instalacion.setEstado(EstadoInstalacion.CANCELADA);
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
        Instalacion instalacion = buscarYValidarInstalacion(instalacionId, "reprogramar");

        validarCuposDisponibles(dto.getNuevaFecha(), instalacion.getFechaProgramada());

        instalacion.setFechaProgramada(dto.getNuevaFecha());
        instalacion.setTecnico(null);
        instalacion.setBloqueHorario(null);
        instalacion.setEstado(EstadoInstalacion.REPROGRAMADA);

        if (dto.getMotivo() != null && !dto.getMotivo().isEmpty()) {
            instalacion.setObservaciones("Motivo de reprogramacion: " + dto.getMotivo());
        }

        instalacionRepository.save(instalacion);

        return Map.of("mensaje", "La instalación ha sido reprogramada con éxito.");
    }

    // Metodos
    private Instalacion buscarYValidarInstalacion(Integer instalacionId, String accion) {
        Instalacion instalacion = instalacionRepository.findById(instalacionId)
                .orElseThrow(() -> new IllegalArgumentException("Instalacion no encontrada con ID: " + instalacionId));
        if (instalacion.getEstado() != EstadoInstalacion.PENDIENTE
                && instalacion.getEstado() != EstadoInstalacion.REPROGRAMADA) {
            throw new IllegalArgumentException(
                    "La instalación no se puede " + accion + " en su estado actual (" + instalacion.getEstado() + ").");
        }
        return instalacion;
    }

    private void agregarObservacion(Instalacion instalacion, InstalacionObservacionDTO dto) {
        if (dto != null && dto.getObservaciones() != null && !dto.getObservaciones().isEmpty()) {
            instalacion.setObservaciones(dto.getObservaciones());
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

        Long agendadas = instalacionRepository.countInstalacionesEnFecha(nuevaFecha);
        boolean isSameSlot = fechaActual.equals(nuevaFecha);
        long efectivasAgendadas = isSameSlot ? (agendadas - 1) : agendadas;

        if (efectivasAgendadas >= cuposMaximos) {
            throw new IllegalArgumentException("Cupos agotados para el día " + nuevaFecha);
        }
    }
}
