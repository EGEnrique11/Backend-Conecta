package pe.idat.BackEndConecta.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import pe.idat.BackEndConecta.entity.enums.TipoEfectoPromocion;

import java.math.BigDecimal;

@Data
public class EfectoPromocionRequestDTO {
    //@NotNull(message = "El ID de la promoción es requerido")
    //private Integer promocionId;

    @NotNull(message = "El tipo de efecto es requerido")
    private TipoEfectoPromocion tipoEfecto;

    @NotNull(message = "El valor del efecto es requerido")
    @DecimalMin(value = "0.0", inclusive = true, message = "El valor debe ser positivo")
    private BigDecimal valor;

    @NotNull(message = "La duración en meses es requerida")
    @Min(value = 1, message = "La duración debe ser de al menos 1 mes")
    private Integer duracionMeses;
}
