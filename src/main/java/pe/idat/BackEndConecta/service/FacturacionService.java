package pe.idat.BackEndConecta.service;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import pe.idat.BackEndConecta.dto.ReciboListDTO;
import pe.idat.BackEndConecta.entity.enums.EstadoPago;

public interface FacturacionService {
    Map<String, Object> generarRecibosPorCiclo(Integer cicloId, LocalDate fechaEjecucion);

    Map<String, Object> verificarDeudaPendiente(Integer clienteId);

    Page<ReciboListDTO> obtenerRecibosPaginados(Integer contratoId, List<EstadoPago> estados, Pageable pageable);
}
