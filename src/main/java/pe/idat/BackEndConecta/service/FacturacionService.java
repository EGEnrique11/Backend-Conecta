package pe.idat.BackEndConecta.service;

import java.time.LocalDate;
import java.util.Map;

public interface FacturacionService {
    Map<String, Object> generarRecibosPorCiclo(Integer cicloId, LocalDate fechaEjecucion);
}
