package pe.idat.BackEndConecta.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class PagoRequestDTO {

    @NotNull(message = "El reciboId es requerido")
    private Integer reciboId;

    @NotNull(message = "El montoPagado es requerido")
    @DecimalMin(value = "0.01", message = "El monto pagado debe ser mayor a 0")
    private BigDecimal montoPagado;

    @NotBlank(message = "El metodoPago es requerido")
    private String metodoPago;

    private String nroOperacion;

    private String observaciones;

    @NotNull(message = "El empleadoRegistroId es requerido")
    private Integer empleadoRegistroId;
}
