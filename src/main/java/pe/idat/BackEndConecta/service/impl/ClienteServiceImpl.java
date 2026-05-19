package pe.idat.BackEndConecta.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pe.idat.BackEndConecta.dto.ClienteRegistrationDTO;
import pe.idat.BackEndConecta.entity.Cliente;
import pe.idat.BackEndConecta.entity.Direccion;
import pe.idat.BackEndConecta.entity.Distrito;
import pe.idat.BackEndConecta.mapper.ClienteMapper;
import pe.idat.BackEndConecta.repository.ClienteRepository;
import pe.idat.BackEndConecta.repository.DireccionRepository;
import pe.idat.BackEndConecta.repository.DistritoRepository;
import pe.idat.BackEndConecta.repository.PersonaRepository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import pe.idat.BackEndConecta.dto.ClienteContactoDTO;
import pe.idat.BackEndConecta.dto.ClienteDTO;
import pe.idat.BackEndConecta.dto.ClienteUpdateDTO;
import pe.idat.BackEndConecta.entity.enums.EstadoCliente;
import pe.idat.BackEndConecta.exception.ResourceNotFoundException;

import java.util.List;
import java.util.Optional;
import java.util.StringJoiner;

@Service
@RequiredArgsConstructor
public class ClienteServiceImpl implements pe.idat.BackEndConecta.service.ClienteService {

    private final PersonaRepository personaRepository;
    private final ClienteRepository clienteRepository;
    private final DireccionRepository direccionRepository;
    private final DistritoRepository distritoRepository;
    private final ClienteMapper clienteMapper;

    @Override
    @Transactional
    public ClienteRegistrationDTO registrarCliente(ClienteRegistrationDTO dto) {
        // 1. Validar si el cliente ya existe
        Optional<Cliente> clienteOpt = clienteRepository.findByDocumento(dto.getDocumento());
        Cliente cliente;

        if (clienteOpt.isPresent()) {
            cliente = clienteOpt.get();
            if (cliente.getEstado() != EstadoCliente.BAJA) {
                throw new IllegalArgumentException("El cliente ya existe y tiene un proceso activo");
            }

            // Es una Reactivación (BAJA)
            // TODO: Validar si el cliente tiene deuda en ReciboService antes de reactivar.

            // Actualizar datos personales y de contacto
            cliente.setNombres(dto.getNombres());
            cliente.setApellidoPaterno(dto.getApellidoPaterno());
            cliente.setApellidoMaterno(dto.getApellidoMaterno());
            cliente.setCorreo(dto.getCorreo());
            cliente.setCelular(dto.getCelular());

            cliente.setEstado(EstadoCliente.PRECLIENTE);
            cliente = clienteRepository.save(cliente);

            // Buscar direcciones antiguas y poner isPrincipal = false
            List<Direccion> direccionesAntiguas = direccionRepository.findByClienteId(cliente.getId());
            for (Direccion d : direccionesAntiguas) {
                d.setIsPrincipal(false);
            }
            direccionRepository.saveAll(direccionesAntiguas);

        } else {
            // Validar que el documento no exista en otra entidad Persona (ej. Empleado)
            if (personaRepository.findByDocumento(dto.getDocumento()).isPresent()) {
                throw new IllegalArgumentException(
                        "Ya existe una persona registrada con el documento: " + dto.getDocumento());
            }

            // Creación Nueva
            cliente = clienteMapper.toClienteEntity(dto);
            cliente = clienteRepository.save(cliente);
        }

        // 2. Buscar Distrito
        Distrito distrito = distritoRepository.findById(dto.getDistritoId())
                .orElseThrow(() -> new IllegalArgumentException(
                        "No se encontró el distrito con ID: " + dto.getDistritoId()));

        // 3. Mapear y guardar Dirección nueva
        Direccion direccion = clienteMapper.toDireccionEntity(dto);
        direccion.setCliente(cliente);
        direccion.setDistrito(distrito);
        direccion.setDireccionCompleta(generarDireccionCompleta(dto, distrito.getNombre()));

        direccion.setIsPrincipal(true);
        direccion.setActivo(true);

        direccionRepository.save(direccion);

        return dto;
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ClienteDTO> buscarClientes(String documento, String celular, Integer id, String nombres,
            String apellidoPaterno, Pageable pageable) {
        Page<Cliente> clientes = clienteRepository.buscarClientesConFiltros(documento, celular, id, nombres,
                apellidoPaterno, pageable);
        return clientes.map(clienteMapper::toClienteDTO);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ClienteDTO> buscarClientesPaginados(String criterio, String valor, Pageable pageable) {
        Page<Cliente> clientes;
        if (criterio == null || criterio.trim().isEmpty() || valor == null || valor.trim().isEmpty()
                || "TODOS".equalsIgnoreCase(criterio)) {
            clientes = clienteRepository.findAll(pageable);
        } else {
            clientes = clienteRepository.buscarPorCriterio(criterio, valor, pageable);
        }
        return clientes.map(clienteMapper::toClienteDTO);
    }

    @Override
    @Transactional
    public ClienteDTO actualizarCliente(Integer id, ClienteUpdateDTO dto) {
        Cliente cliente = clienteRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Cliente no encontrado con ID: " + id));

        clienteMapper.updateClienteFromDto(dto, cliente);
        cliente = clienteRepository.save(cliente);
        return clienteMapper.toClienteDTO(cliente);
    }

    @Override
    @Transactional
    public void eliminarCliente(Integer id) {
        Cliente cliente = clienteRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Cliente no encontrado con ID: " + id));
        cliente.setEstado(EstadoCliente.BAJA);
        clienteRepository.save(cliente);
    }

    @Override
    @Transactional(readOnly = true)
    public ClienteDTO buscarClientePorId(Integer id) {
        return clienteRepository.findById(id)
                .map(clienteMapper::toClienteDTO)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Cliente no encontrado con id: " + id));
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<ClienteDTO> buscarPorDni(String dni) {
        return clienteRepository.findByDocumento(dni)
                .map(clienteMapper::toClienteDTO);
    }

    private String generarDireccionCompleta(ClienteRegistrationDTO dto, String nombreDistrito) {
        StringJoiner sj = new StringJoiner(" ");
        sj.add(dto.getTipoVia().name());

        if (dto.getNombreVia() != null)
            sj.add(dto.getNombreVia());
        if (dto.getNumero() != null)
            sj.add("Nro " + dto.getNumero());

        if (dto.getTipoUrbanizacion() != null) {
            sj.add(dto.getTipoUrbanizacion().name());
            if (dto.getNombreUrbanizacion() != null) {
                sj.add(dto.getNombreUrbanizacion());
            }
        }

        if (dto.getManzana() != null)
            sj.add("Mz. " + dto.getManzana());
        if (dto.getLote() != null)
            sj.add("Lt. " + dto.getLote());
        if (dto.getPiso() != null)
            sj.add("Piso " + dto.getPiso());
        if (dto.getInterior() != null)
            sj.add("Int. " + dto.getInterior());

        sj.add("- " + nombreDistrito);
        return sj.toString();
    }

    @Override
    @Transactional
    public ClienteDTO actualizarContacto(Integer id, ClienteContactoDTO dto) {
        Cliente cliente = clienteRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Cliente no encontrado con el Id: " + id));

        cliente.setCorreo(dto.getCorreo());
        cliente.setCelular(dto.getCelular());
        cliente = clienteRepository.save(cliente);
        return clienteMapper.toClienteDTO(cliente);
    }
}
