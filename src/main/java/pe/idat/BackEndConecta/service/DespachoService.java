package pe.idat.BackEndConecta.service;

import pe.idat.BackEndConecta.dto.AsignarTecnicoDTO;
import pe.idat.BackEndConecta.dto.InstalacionPendienteDTO;
import pe.idat.BackEndConecta.dto.InstalacionReprogramarDTO;
import pe.idat.BackEndConecta.entity.enums.EstadoInstalacion;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

public interface DespachoService {
    List<InstalacionPendienteDTO> obtenerPendientesPorFechaYFranja(LocalDate fecha, String franja);

    List<InstalacionPendienteDTO> obtenerAsignadasPorFecha(LocalDate fecha);

    Map<String, String> asignarTecnicoABloque(Integer instalacionId, AsignarTecnicoDTO dto);

    Map<String, String> actualizarEstado(Integer instalacionId, EstadoInstalacion estado);

    List<InstalacionPendienteDTO> obtenerAgendaTecnico(Integer mes, Integer anio, String username);

    List<InstalacionPendienteDTO> buscarInstalaciones(String term);

    Map<String, String> reprogramarInstalacion(Integer instalacionId, InstalacionReprogramarDTO dto);
}
