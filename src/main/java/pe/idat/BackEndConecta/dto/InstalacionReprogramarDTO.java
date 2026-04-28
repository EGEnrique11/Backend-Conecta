package pe.idat.BackEndConecta.dto;

import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDate;

@Data
public class InstalacionReprogramarDTO {
    @NotNull(message = "La fechaProgramada es requerida")
    @FutureOrPresent(message = "La fecha no puede ser en el pasado")
    private LocalDate fechaProgramada;

    @NotNull(message = "La franja horaria es requerida")
    @jakarta.validation.constraints.Pattern(regexp = "^(MAÑANA|TARDE)$", message = "La franja horaria debe ser MAÑANA o TARDE")
    private String franjaHoraria;

    private String observaciones;
}
