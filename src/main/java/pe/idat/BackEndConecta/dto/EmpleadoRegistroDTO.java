package pe.idat.BackEndConecta.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import pe.idat.BackEndConecta.entity.enums.TipoDocumento;

import java.util.List;

@Data
public class EmpleadoRegistroDTO {

    @NotNull(message = "El tipo de documento es requerido")
    private TipoDocumento tipoDocumento;

    @NotBlank(message = "El documento es requerido")
    private String documento;

    @NotBlank(message = "El nombre es requerido")
    private String nombres;

    @NotBlank(message = "El apellido paterno es requerido")
    private String apellidoPaterno;

    private String apellidoMaterno;

    @NotBlank(message = "El correo es requerido")
    @Email(message = "Formato de correo inválido")
    private String correo;

    @NotBlank(message = "El username es requerido")
    private String username;

    @NotBlank(message = "El password es requerido")
    private String password;

    @NotEmpty(message = "Debe asignar al menos un rol al empleado")
    private List<String> roles;
}
