package pe.idat.BackEndConecta.service;

import pe.idat.BackEndConecta.dto.AsignarTecnicoDTO;
import pe.idat.BackEndConecta.dto.InstalacionPendienteDTO;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

public interface DespachoService {
    List<InstalacionPendienteDTO> obtenerPendientesPorFechaYFranja(LocalDate fecha, String franja);
    List<InstalacionPendienteDTO> obtenerAsignadasPorFecha(LocalDate fecha);
    Map<String, String> asignarTecnicoABloque(Integer instalacionId, AsignarTecnicoDTO dto);
    Map<String, String> actualizarEstado(Integer instalacionId, pe.idat.BackEndConecta.entity.enums.EstadoInstalacion estado);
    List<InstalacionPendienteDTO> obtenerAgendaTecnico(Integer mes, Integer anio, String username);
}
