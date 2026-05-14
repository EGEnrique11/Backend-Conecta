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

    List<InstalacionPendienteDTO> obtenerAgendaTecnico(Integer tecnicoId, LocalDate fecha);

    List<InstalacionPendienteDTO> buscarInstalaciones(String criterio, String valor);
}
