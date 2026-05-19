package pe.idat.BackEndConecta.service.impl;

import java.util.List;

import org.springframework.stereotype.Service;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import pe.idat.BackEndConecta.dto.ContratoResumenDTO;
import pe.idat.BackEndConecta.entity.Contrato;
import pe.idat.BackEndConecta.mapper.ContratoMapper;
import pe.idat.BackEndConecta.repository.ContratoRepository;
import pe.idat.BackEndConecta.service.ContratoService;

@Service
@RequiredArgsConstructor
public class ContratoServiceImpl implements ContratoService{
    private final ContratoRepository contratoRepository;
    private final ContratoMapper contratoMapper;
    @Override
    @Transactional
    public List<ContratoResumenDTO> buscarPorClienteId(Integer clienteId) {
        List<Contrato> contratos = contratoRepository.findByClienteId(clienteId);
        return contratoMapper.toResumenDTOList(contratos);
    }
    
}
