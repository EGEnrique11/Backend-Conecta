package pe.idat.BackEndConecta.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pe.idat.BackEndConecta.dto.InstalacionObservacionDTO;
import pe.idat.BackEndConecta.dto.InstalacionReprogramarDTO;
import pe.idat.BackEndConecta.entity.Cliente;
import pe.idat.BackEndConecta.entity.Contrato;
import pe.idat.BackEndConecta.entity.Instalacion;
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

        // 1. Buscar Instalación
        Instalacion instalacion = instalacionRepository.findById(instalacionId)
                .orElseThrow(() -> new IllegalArgumentException("Instalación no encontrada con ID: " + instalacionId));

        if (instalacion.getEstado() != EstadoInstalacion.PENDIENTE &&
                instalacion.getEstado() != EstadoInstalacion.REPROGRAMADA) {
            throw new IllegalArgumentException(
                    "La instalación no se puede completar en su estado actual (" + instalacion.getEstado() + ").");
        }

        // 2. Marcar como Completada
        instalacion.setEstado(EstadoInstalacion.COMPLETADA);
        if (dto != null && dto.getObservaciones() != null) {
            instalacion.setObservaciones(dto.getObservaciones());
        }
        instalacionRepository.save(instalacion);

        // 3. Obtener Contrato y Activar
        Contrato contrato = instalacion.getContrato();
        LocalDate hoy = LocalDate.now();
        contrato.setEstado(EstadoContrato.ACTIVO);
        contrato.setFechaActivacion(hoy);

        // 4. Optimización de Promoción
        if (contrato.getFechaFinPromocion() != null) {
            long diferenciaDias = ChronoUnit.DAYS.between(contrato.getFechaContrato(), hoy);
            if (diferenciaDias > 0) {
                LocalDate nuevaFechaFinPromocion = contrato.getFechaFinPromocion().plusDays(diferenciaDias);
                contrato.setFechaFinPromocion(nuevaFechaFinPromocion);
            }
        }
        contratoRepository.save(contrato);

        // 5. Activar Cliente si es PRECLIENTE
        Cliente cliente = contrato.getCliente();
        if (cliente.getEstado() == pe.idat.BackEndConecta.entity.enums.EstadoCliente.PRECLIENTE) {
            cliente.setEstado(pe.idat.BackEndConecta.entity.enums.EstadoCliente.ACTIVO);
            clienteRepository.save(cliente);
        }

        // TODO: Generar PDF del contrato usando Thymeleaf
        return Map.of("mensaje", "La instalación ha sido completada y el contrato activado correctamente.");
    }

    @Override
    @Transactional
    public Map<String, String> cancelarInstalacion(Integer instalacionId, InstalacionObservacionDTO dto) {
        Instalacion instalacion = instalacionRepository.findById(instalacionId)
                .orElseThrow(() -> new IllegalArgumentException("Instalación no encontrada."));

        if (instalacion.getEstado() != EstadoInstalacion.PENDIENTE &&
                instalacion.getEstado() != EstadoInstalacion.REPROGRAMADA) {
            throw new IllegalArgumentException("La instalación no se puede cancelar en su estado actual.");
        }

        instalacion.setEstado(EstadoInstalacion.CANCELADA);
        if (dto != null && dto.getObservaciones() != null) {
            instalacion.setObservaciones(dto.getObservaciones());
        }
        instalacionRepository.save(instalacion);

        Contrato contrato = instalacion.getContrato();
        contrato.setEstado(EstadoContrato.CANCELADO);
        contratoRepository.save(contrato);

        return Map.of("mensaje", "La instalación y el contrato han sido cancelados.");
    }

    @Override
    @Transactional
    public Map<String, String> reprogramarInstalacion(Integer instalacionId, InstalacionReprogramarDTO dto) {
        Instalacion instalacion = instalacionRepository.findById(instalacionId)
                .orElseThrow(() -> new IllegalArgumentException("Instalación no encontrada."));

        if (instalacion.getEstado() != EstadoInstalacion.PENDIENTE &&
                instalacion.getEstado() != EstadoInstalacion.REPROGRAMADA) {
            throw new IllegalArgumentException("La instalación no se puede reprogramar en su estado actual.");
        }

        // Validar cupos en vez de cruces exactos de hora
        long totalTecnicos = empleadoRepository.count();
        long cuposMaximos = totalTecnicos * 2;

        Long agendadas = instalacionRepository.countInstalacionesEnFecha(dto.getFechaProgramada());

        boolean isSameSlot = instalacion.getFechaProgramada().equals(dto.getFechaProgramada());

        long efectivasAgendadas = isSameSlot ? (agendadas - 1) : agendadas;

        if (efectivasAgendadas >= cuposMaximos) {
            throw new IllegalArgumentException("Cupos agotados para este día.");
        }

        instalacion.setFechaProgramada(dto.getFechaProgramada());
        instalacion.setTecnico(null);
        instalacion.setBloqueHorario(null);
        instalacion.setEstado(EstadoInstalacion.REPROGRAMADA);

        if (dto.getObservaciones() != null) {
            instalacion.setObservaciones(dto.getObservaciones());
        }

        instalacionRepository.save(instalacion);

        return Map.of("mensaje", "La instalación ha sido reprogramada con éxito.");
    }
}
