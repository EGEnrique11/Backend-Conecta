package pe.idat.BackEndConecta.dto;

import lombok.Data;
import pe.idat.BackEndConecta.entity.enums.EstadoCliente;
import pe.idat.BackEndConecta.entity.enums.TipoDocumento;
import pe.idat.BackEndConecta.entity.enums.TipoPersona;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
public class ClienteDTO {
    private Integer id;
    private TipoPersona tipoPersona;
    private TipoDocumento tipoDocumento;
    private String documento;
    private String nombres;
    private String apellidoPaterno;
    private String apellidoMaterno;
    private String correo;
    private String celular;
    private LocalDate fechaNacimiento;
    private EstadoCliente estado;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
