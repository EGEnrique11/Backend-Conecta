package pe.idat.BackEndConecta.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class ClienteContactoDTO {
    @NotBlank(message = "El correo es obligatorio")
    @Email(message = "Formato de correo inválido")
    private String correo;
    @NotBlank(message = "El celular es obligatorio")
    private String celular;
}
