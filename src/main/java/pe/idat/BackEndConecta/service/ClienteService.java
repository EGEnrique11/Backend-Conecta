package pe.idat.BackEndConecta.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import pe.idat.BackEndConecta.dto.ClienteDTO;
import pe.idat.BackEndConecta.dto.ClienteRegistrationDTO;
import pe.idat.BackEndConecta.dto.ClienteUpdateDTO;

public interface ClienteService {
    ClienteRegistrationDTO registrarCliente(ClienteRegistrationDTO dto);
    Page<ClienteDTO> buscarClientes(String documento, String celular, Integer id, String nombres, String apellidoPaterno, Pageable pageable);
    ClienteDTO actualizarCliente(Integer id, ClienteUpdateDTO dto);
    void eliminarCliente(Integer id);
    java.util.Optional<ClienteDTO> buscarPorDni(String dni);
    Page<ClienteDTO> buscarClientesPaginados(String criterio, String valor, Pageable pageable);
}
