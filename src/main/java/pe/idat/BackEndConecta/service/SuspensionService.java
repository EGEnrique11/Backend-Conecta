package pe.idat.BackEndConecta.service;

import java.time.LocalDate;
import java.util.Map;

public interface SuspensionService {
    Map<String, Object> procesarSuspensionesPorMora(LocalDate fechaEjecucion);
}
