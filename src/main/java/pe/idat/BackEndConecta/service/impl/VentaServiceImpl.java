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
        validarTiemposYCupos(dto.getFechaProgramada());
        
        // --- GESTIÓN DE CLIENTE ---
        Cliente cliente = obtenerOCrearCliente(dto);
        // --- GESTIÓN DE DIRECCIÓN ---
        Direccion direccion = obtenerOCrearDireccion(dto, cliente);

        // --- BÚSQUEDA DE ENTIDADES RELACIONADAS ---
        Plan plan = planRepository.findById(dto.getPlanId())
                .orElseThrow(() -> new IllegalArgumentException("Plan no encontrado."));
        LocalDate fechaFinPromocion = calcularFechaFinPromocion(dto.getPromocionId());

        CicloPago cicloPago = new CicloPago();
        cicloPago.setId(1);

        Empleado vendedor = new Empleado();
        vendedor.setId(2);

        // --- CREACIÓN DEL CONTRATO ---
        Contrato contrato = crearYGuardarContrato(cliente, direccion, plan, dto.getPromocionId(), fechaFinPromocion, cicloPago, vendedor);
        // --- CREACIÓN DE LA INSTALACIÓN ---
        Instalacion instalacion = crearYGuardarInstalacion(contrato, dto.getFechaProgramada());

        return new VentaResponseDTO(
                contrato.getId(),
                instalacion.getId(),
                "Venta generada e instalación agendada con éxito.");
    }

    private void validarTiemposYCupos(LocalDate fechaProgramada) {
        LocalDate hoy = LocalDate.now();
        LocalTime ahora = LocalTime.now();
        LocalTime limite = LocalTime.of(12, 0);

        if (fechaProgramada.isBefore(hoy)) {
            throw new IllegalArgumentException("La fecha programada no puede ser en el pasado.");
        }
        if (ahora.isAfter(limite) && fechaProgramada.isEqual(hoy)) {
            throw new IllegalArgumentException("Ya no es posible agendar para el día de hoy.");
        }
        if (fechaProgramada.isAfter(hoy.plusDays(7))) {
            throw new IllegalArgumentException("La fecha programada no puede exceder los 7 días.");
        }
        long totalTecnicos = empleadoRepository.count();
        long cuposMaximos = totalTecnicos * 2;
        // CORRECCIÓN: Pasamos la lista de Enums
        List<EstadoInstalacion> estadosOcupados = List.of(EstadoInstalacion.PENDIENTE, EstadoInstalacion.REPROGRAMADA);
        Long agendadas = instalacionRepository.countInstalacionesEnFecha(fechaProgramada, estadosOcupados);

        if (agendadas != null && agendadas >= cuposMaximos) {
            throw new IllegalArgumentException("Cupos agotados para el día " + fechaProgramada);
        }
    }

    private Cliente obtenerOCrearCliente(VentaCompletaRequestDTO dto) {
        if (dto.getClienteId() != null) {
            Cliente cliente = clienteRepository.findById(dto.getClienteId())
                    .orElseThrow(
                            () -> new IllegalArgumentException("Cliente no encontrado con ID " + dto.getClienteId()));

            // CORRECCIÓN: Pasamos el Enum en lugar del String
            Long pendientes = instalacionRepository.countPendientesByClienteId(cliente.getId(),
                    EstadoInstalacion.PENDIENTE);
            if (pendientes != null && pendientes > 0) {
                throw new IllegalArgumentException("El cliente ya tiene una instalación en curso.");
            }
            return cliente;
        }

        if (dto.getDatosCliente() == null) {
            throw new IllegalArgumentException("Debe enviar los datos del cliente si no envía un clienteId.");
        }
        if (clienteRepository.findByDocumento(dto.getDatosCliente().getDocumento()).isPresent()) {
            throw new IllegalArgumentException("Ya existe un cliente con ese documento. Busque el DNI.");
        }

        Cliente nuevoCliente = clienteMapper.toClienteEntity(dto.getDatosCliente());
        return clienteRepository.save(nuevoCliente);
    }

    private Direccion obtenerOCrearDireccion(VentaCompletaRequestDTO dto, Cliente cliente) {
        if (dto.getDireccionId() != null) {
            return direccionRepository.findById(dto.getDireccionId())
                    .orElseThrow(() -> new IllegalArgumentException("Dirección no encontrada."));
        }

        if (dto.getDatosDireccion() == null) {
            throw new IllegalArgumentException("Debe enviar los datos de la dirección si no envía un direccionId.");
        }

        Distrito distrito = distritoRepository.findById(dto.getDatosDireccion().getDistritoId())
                .orElseThrow(() -> new IllegalArgumentException("No se encontró el distrito."));

        Direccion direccion = clienteMapper.toDireccionEntity(dto.getDatosDireccion());
        direccion.setCliente(cliente);
        direccion.setDistrito(distrito);

        StringJoiner sj = new StringJoiner(" ");
        sj.add(dto.getDatosDireccion().getTipoVia().name());
        if (dto.getDatosDireccion().getNombreVia() != null)
            sj.add(dto.getDatosDireccion().getNombreVia());
        if (dto.getDatosDireccion().getNumero() != null)
            sj.add("Nro " + dto.getDatosDireccion().getNumero());
        sj.add("- " + distrito.getNombre());

        direccion.setDireccionCompleta(sj.toString());
        direccion.setIsPrincipal(true);
        direccion.setActivo(true);

        return direccionRepository.save(direccion);
    }

    private LocalDate calcularFechaFinPromocion(Integer promocionId) {
        if (promocionId == null)
            return null;

        Promocion promocion = promocionRepository.findById(promocionId)
                .orElseThrow(() -> new IllegalArgumentException("Promoción no encontrada."));

        List<EfectoPromocion> efectos = efectoPromocionRepository.findByPromocionId(promocion.getId());
        if (efectos.isEmpty())
            return null;

        int maxMeses = efectos.stream().mapToInt(EfectoPromocion::getDuracionMeses).max().orElse(0);
        return maxMeses > 0 ? LocalDate.now().plusMonths(maxMeses) : null;
    }

    private Contrato crearYGuardarContrato(Cliente cliente, Direccion direccion, Plan plan, Integer promocionId,
            LocalDate fechaFinPromocion, CicloPago cicloPago, Empleado vendedor) {
        Promocion promocion = null;
        if (promocionId != null) {
            promocion = promocionRepository.findById(promocionId).orElse(null);
        }

        Contrato contrato = Contrato.builder()
                .cliente(cliente)
                .direccion(direccion)
                .plan(plan)
                .promocion(promocion)
                .fechaFinPromocion(fechaFinPromocion)
                .cicloPago(cicloPago)
                .empleadoRegistro(vendedor)
                .fechaContrato(LocalDate.now())
                .costoInstalacion(BigDecimal.ZERO)
                .estado(EstadoContrato.PENDIENTE)
                .build();

        return contratoRepository.save(contrato);
    }

    private Instalacion crearYGuardarInstalacion(Contrato contrato, LocalDate fechaProgramada) {
        Instalacion instalacion = Instalacion.builder()
                .contrato(contrato)
                .fechaProgramada(fechaProgramada)
                .estado(EstadoInstalacion.PENDIENTE)
                .build();

        return instalacionRepository.save(instalacion);
    }
}
