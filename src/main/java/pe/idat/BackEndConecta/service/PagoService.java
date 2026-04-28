package pe.idat.BackEndConecta.service;

import pe.idat.BackEndConecta.dto.PagoRequestDTO;
import java.util.Map;

public interface PagoService {
    Map<String, Object> registrarPago(PagoRequestDTO dto);
}
