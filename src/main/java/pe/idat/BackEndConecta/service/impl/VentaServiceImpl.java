package pe.idat.BackEndConecta.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pe.idat.BackEndConecta.dto.VentaCompletaRequestDTO;
import pe.idat.BackEndConecta.dto.VentaResponseDTO;
import pe.idat.BackEndConecta.entity.*;
import pe.idat.BackEndConecta.entity.enums.EstadoContrato;
import pe.idat.BackEndConecta.entity.enums.EstadoInstalacion;
import pe.idat.BackEndConecta.mapper.ClienteMapper;
import pe.idat.BackEndConecta.repository.*;
import pe.idat.BackEndConecta.service.VentaService;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.StringJoiner;

@Service
@RequiredArgsConstructor
public class VentaServiceImpl implements VentaService {

    private final ClienteRepository clienteRepository;
    private final DireccionRepository direccionRepository;
    private final DistritoRepository distritoRepository;
    private final PlanRepository planRepository;
    private final PromocionRepository promocionRepository;
    private final EfectoPromocionRepository efectoPromocionRepository;
    private final ContratoRepository contratoRepository;
    private final InstalacionRepository instalacionRepository;
    private final EmpleadoRepository empleadoRepository;
    private final ClienteMapper clienteMapper;

    @Override
    @Transactional
    public VentaResponseDTO generarVenta(VentaCompletaRequestDTO dto) {

        // --- VALIDACIONES DE TIEMPO Y CUPOS ---
        LocalDate hoy = LocalDate.now();
        LocalTime ahora = LocalTime.now();
        LocalTime limite = LocalTime.of(12, 0);

        if (dto.getFechaProgramada().isBefore(hoy)) {
            throw new IllegalArgumentException("La fecha programada no puede ser en el pasado.");
        }

        if (ahora.isAfter(limite) && dto.getFechaProgramada().isEqual(hoy)) {
            throw new IllegalArgumentException("Ya no es posible agendar para el día de hoy.");
        }

        if (dto.getFechaProgramada().isAfter(hoy.plusDays(7))) {
            throw new IllegalArgumentException("La fecha programada no puede exceder los 7 días.");
        }

        long totalTecnicos = empleadoRepository.count();
        long cuposMaximos = totalTecnicos * 2; // O ajustado según turnos
        
        Long agendadas = instalacionRepository.countInstalacionesEnFecha(dto.getFechaProgramada());
        if (agendadas != null && agendadas >= cuposMaximos) {
            throw new IllegalArgumentException("Cupos agotados para este día.");
        }

        // --- GESTIÓN DE CLIENTE ---
        Cliente cliente;
        if (dto.getClienteId() != null) {
            cliente = clienteRepository.findById(dto.getClienteId())
                    .orElseThrow(() -> new IllegalArgumentException("Cliente no encontrado con ID " + dto.getClienteId()));
            
            Long pendientes = instalacionRepository.countPendientesByClienteId(cliente.getId());
            if (pendientes > 0) {
                throw new IllegalArgumentException("El cliente ya tiene una instalación en curso.");
            }
        } else {
            if (dto.getDatosCliente() == null) {
                throw new IllegalArgumentException("Debe enviar los datos del cliente si no envía un clienteId.");
            }
            if (clienteRepository.findByDocumento(dto.getDatosCliente().getDocumento()).isPresent()) {
                throw new IllegalArgumentException("Ya existe un cliente con ese documento. Busque el DNI.");
            }
            cliente = clienteMapper.toClienteEntity(dto.getDatosCliente());
            cliente = clienteRepository.save(cliente);
        }

        // --- GESTIÓN DE DIRECCIÓN ---
        Direccion direccion;
        if (dto.getDireccionId() != null) {
            direccion = direccionRepository.findById(dto.getDireccionId())
                    .orElseThrow(() -> new IllegalArgumentException("Dirección no encontrada."));
        } else {
            if (dto.getDatosDireccion() == null) {
                throw new IllegalArgumentException("Debe enviar los datos de la dirección si no envía un direccionId.");
            }
            Distrito distrito = distritoRepository.findById(dto.getDatosDireccion().getDistritoId())
                    .orElseThrow(() -> new IllegalArgumentException("No se encontró el distrito."));
            
            direccion = clienteMapper.toDireccionEntity(dto.getDatosDireccion());
            direccion.setCliente(cliente);
            direccion.setDistrito(distrito);
            
            StringJoiner sj = new StringJoiner(" ");
            sj.add(dto.getDatosDireccion().getTipoVia().name());
            if (dto.getDatosDireccion().getNombreVia() != null) sj.add(dto.getDatosDireccion().getNombreVia());
            if (dto.getDatosDireccion().getNumero() != null) sj.add("Nro " + dto.getDatosDireccion().getNumero());
            sj.add("- " + distrito.getNombre());
            
            direccion.setDireccionCompleta(sj.toString());
            direccion.setIsPrincipal(true);
            direccion.setActivo(true);
            direccion = direccionRepository.save(direccion);
        }

        // --- BÚSQUEDA DE ENTIDADES RELACIONADAS ---
        Plan plan = planRepository.findById(dto.getPlanId())
                .orElseThrow(() -> new IllegalArgumentException("Plan no encontrado."));

        CicloPago cicloPago = new CicloPago(); 
        cicloPago.setId(1); 
        
        Empleado vendedor = new Empleado();
        vendedor.setId(2); 

        // --- LÓGICA DE PROMOCIÓN ---
        Promocion promocion = null;
        LocalDate fechaFinPromocion = null;

        if (dto.getPromocionId() != null) {
            promocion = promocionRepository.findById(dto.getPromocionId())
                    .orElseThrow(() -> new IllegalArgumentException("Promoción no encontrada."));
            
            List<EfectoPromocion> efectos = efectoPromocionRepository.findByPromocionId(promocion.getId());
            if (!efectos.isEmpty()) {
                int maxMeses = efectos.stream().mapToInt(EfectoPromocion::getDuracionMeses).max().orElse(0);
                if (maxMeses > 0) {
                    fechaFinPromocion = hoy.plusMonths(maxMeses);
                }
            }
        }

        // --- CREACIÓN DEL CONTRATO ---
        Contrato contrato = Contrato.builder()
                .cliente(cliente)
                .direccion(direccion)
                .plan(plan)
                .promocion(promocion)
                .fechaFinPromocion(fechaFinPromocion)
                .cicloPago(cicloPago)
                .empleadoRegistro(vendedor)
                .fechaContrato(hoy)
                .costoInstalacion(BigDecimal.ZERO)
                .estado(EstadoContrato.PENDIENTE)
                .build();

        contrato = contratoRepository.save(contrato);

        // --- CREACIÓN DE LA INSTALACIÓN ---
        Instalacion instalacion = Instalacion.builder()
                .contrato(contrato)
                .fechaProgramada(dto.getFechaProgramada())
                .estado(EstadoInstalacion.PENDIENTE)
                .build();

        instalacion = instalacionRepository.save(instalacion);

        return new VentaResponseDTO(
                contrato.getId(),
                instalacion.getId(),
                "Venta generada e instalación agendada con éxito."
        );
    }
}
