package pe.idat.BackEndConecta.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class AsignarTecnicoDTO {
    
    @NotNull(message = "El tecnicoId es requerido")
    private Integer tecnicoId;

    @NotBlank(message = "El bloque asignado es requerido")
    private String bloqueAsignado;
}
