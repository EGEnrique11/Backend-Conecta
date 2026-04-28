package pe.idat.BackEndConecta.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class ClienteUpdateDTO {
    // Solo permitimos modificar nombres y datos de contacto

    @NotBlank(message = "Los nombres son requeridos")
    private String nombres;

    @NotBlank(message = "El apellido paterno es requerido")
    private String apellidoPaterno;

    private String apellidoMaterno;

    @Email(message = "Debe ser un correo válido")
    private String correo;

    private String celular;
}
