package pe.idat.BackEndConecta.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class LoginRequestDTO {
    @NotBlank(message = "El username es obligatorio")
    private String username;
    
    @NotBlank(message = "El password es obligatorio")
    private String password;
}
