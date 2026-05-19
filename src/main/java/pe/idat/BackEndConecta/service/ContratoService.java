package pe.idat.BackEndConecta.service;

import java.util.List;

import pe.idat.BackEndConecta.dto.ContratoResumenDTO;

public interface ContratoService {
    List<ContratoResumenDTO> buscarPorClienteId(Integer clienteId);
}