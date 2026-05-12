package pe.idat.BackEndConecta.dto;

import lombok.Data;
import java.time.LocalTime;

@Data
public class BloqueHorarioRequestDTO {
    private Boolean esRefrigerio = false;
    private LocalTime horaInicio;
    private LocalTime horaFin;
}
