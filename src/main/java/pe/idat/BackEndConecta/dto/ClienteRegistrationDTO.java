package pe.idat.BackEndConecta.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import pe.idat.BackEndConecta.entity.enums.*;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
public class ClienteRegistrationDTO {
    @NotNull(message = "El tipo de documento es requerido")
    private TipoDocumento tipoDocumento;

    @NotBlank(message = "El documento es requerido")
    private String documento;

    @NotBlank(message = "Los nombres son requeridos")
    private String nombres;

    @NotBlank(message = "El apellido paterno es requerido")
    private String apellidoPaterno;

    private String apellidoMaterno;

    @Email(message = "Debe ser un correo válido")
    private String correo;

    private String celular;

    private LocalDate fechaNacimiento;

    // Direccion fields
    @NotNull(message = "El ID del distrito es requerido")
    private Integer distritoId;

    @NotNull(message = "El tipo de vía es requerido")
    private TipoVia tipoVia;

    private String nombreVia;
    private String numero;
    private TipoUrbanizacion tipoUrbanizacion;
    private String nombreUrbanizacion;
    private String manzana;
    private String lote;
    private String piso;
    private String interior;
    private TipoVivienda tipoVivienda;
    private String referencia;
    private BigDecimal latitud;
    private BigDecimal longitud;

    private Boolean isPrincipal = true;
}
