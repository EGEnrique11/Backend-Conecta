package pe.idat.BackEndConecta.dto;

import lombok.Data;
import java.time.LocalTime;

@Data
public class TurnoRequestDTO {
    private LocalTime horaInicio;
    private LocalTime horaFin;
}
