package pe.idat.BackEndConecta.dto;

import lombok.Builder;
import lombok.Data;

import java.util.Set;

@Data
@Builder
public class EmpleadoListDTO {
    private Integer id;
    private String documento;
    private pe.idat.BackEndConecta.entity.enums.TipoDocumento tipoDocumento;
    private String nombresCompletos;
    private String username;
    private String celular;
    private String correo;
    private Set<String> roles;
}
