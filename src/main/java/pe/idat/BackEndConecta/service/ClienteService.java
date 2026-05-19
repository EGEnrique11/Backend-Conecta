package pe.idat.BackEndConecta.service;

import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import pe.idat.BackEndConecta.dto.ClienteContactoDTO;
import pe.idat.BackEndConecta.dto.ClienteDTO;
import pe.idat.BackEndConecta.dto.ClienteRegistrationDTO;
import pe.idat.BackEndConecta.dto.ClienteUpdateDTO;

public interface ClienteService {
    ClienteRegistrationDTO registrarCliente(ClienteRegistrationDTO dto);
    ClienteDTO buscarClientePorId(Integer id);
    Page<ClienteDTO> buscarClientes(String documento, String celular, Integer id, String nombres, String apellidoPaterno, Pageable pageable);
    ClienteDTO actualizarCliente(Integer id, ClienteUpdateDTO dto);
    void eliminarCliente(Integer id);
    Optional<ClienteDTO> buscarPorDni(String dni);
    Page<ClienteDTO> buscarClientesPaginados(String criterio, String valor, Pageable pageable);
    ClienteDTO actualizarContacto(Integer id, ClienteContactoDTO dto);
}
