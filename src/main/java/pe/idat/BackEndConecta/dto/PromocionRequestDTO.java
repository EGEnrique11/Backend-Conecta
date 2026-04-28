package pe.idat.BackEndConecta.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import java.time.LocalDate;

@Data
public class PromocionRequestDTO {
    @NotBlank(message = "El nombre de la promoción es requerido")
    private String nombre;

    private String descripcion;
    private LocalDate fechaInicio;
    private LocalDate fechaFin;
}
