package pe.idat.BackEndConecta.dto;

import lombok.Data;
import pe.idat.BackEndConecta.entity.enums.TipoEfectoPromocion;

import java.math.BigDecimal;

@Data
public class EfectoPromocionDTO {
    private Integer id;
    private Integer promocionId;
    private TipoEfectoPromocion tipoEfecto;
    private BigDecimal valor;
    private Integer duracionMeses;
}
