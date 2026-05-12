package pe.idat.BackEndConecta.service;

import pe.idat.BackEndConecta.dto.EmpleadoRegistroDTO;

public interface EmpleadoService {
    String registrarEmpleado(EmpleadoRegistroDTO dto);
    java.util.List<java.util.Map<String, Object>> obtenerTecnicos();
    org.springframework.data.domain.Page<pe.idat.BackEndConecta.dto.EmpleadoListDTO> obtenerEmpleadosPaginados(
            int page, int size, String rol, pe.idat.BackEndConecta.entity.enums.TipoDocumento tipoDocumento, String criterio, String valor);
    java.util.List<pe.idat.BackEndConecta.dto.TecnicoResumenDTO> buscarTecnicosResumen(String term);
    org.springframework.data.domain.Page<pe.idat.BackEndConecta.dto.TecnicoResumenDTO> obtenerTecnicosPaginados(int page, int size, String term);
}
