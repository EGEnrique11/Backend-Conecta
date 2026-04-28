package pe.idat.BackEndConecta.service;

import pe.idat.BackEndConecta.dto.InstalacionObservacionDTO;
import pe.idat.BackEndConecta.dto.InstalacionReprogramarDTO;
import java.util.Map;

public interface InstalacionService {
    Map<String, String> completarInstalacion(Integer instalacionId, InstalacionObservacionDTO dto);
    Map<String, String> cancelarInstalacion(Integer instalacionId, InstalacionObservacionDTO dto);
    Map<String, String> reprogramarInstalacion(Integer instalacionId, InstalacionReprogramarDTO dto);
}
