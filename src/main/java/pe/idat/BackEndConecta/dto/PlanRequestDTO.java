package pe.idat.BackEndConecta.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class PlanRequestDTO {
    @NotNull(message = "El ID del servicio es requerido")
    private Integer servicioId;

    @NotBlank(message = "El nombre del plan es requerido")
    private String nombre;

    private Integer velocidadBaseMbps;

    @NotNull(message = "El precio es requerido")
    @DecimalMin(value = "0.0", inclusive = true, message = "El precio debe ser un número positivo")
    private BigDecimal precio;

    private String detalle;
}
