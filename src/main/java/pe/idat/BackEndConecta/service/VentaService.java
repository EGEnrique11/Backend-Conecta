package pe.idat.BackEndConecta.service;

import pe.idat.BackEndConecta.dto.VentaCompletaRequestDTO;
import pe.idat.BackEndConecta.dto.VentaResponseDTO;

public interface VentaService {
    VentaResponseDTO generarVenta(VentaCompletaRequestDTO dto);
}
