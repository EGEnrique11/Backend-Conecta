package pe.idat.BackEndConecta.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class ServicioRequestDTO {
    @NotBlank(message = "El nombre del servicio es requerido")
    private String nombre;

    private String descripcion;
}
