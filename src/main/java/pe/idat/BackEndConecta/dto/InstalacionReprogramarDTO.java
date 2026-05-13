package pe.idat.BackEndConecta.dto;

import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDate;

@Data
public class InstalacionReprogramarDTO {
    @NotNull(message = "La nuevaFecha es requerida")
    @FutureOrPresent(message = "La fecha no puede ser en el pasado")
    private LocalDate nuevaFecha;

    private String motivo;
}
