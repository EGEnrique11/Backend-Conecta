package pe.idat.BackEndConecta.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class AsignarTecnicoDTO {
    @NotNull(message = "El tecnicoId es requerido")
    private Integer tecnicoId;

    @NotNull(message = "El bloqueId es requerido")
    private Integer bloqueId;
}
