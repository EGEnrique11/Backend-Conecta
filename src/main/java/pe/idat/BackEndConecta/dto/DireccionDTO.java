package pe.idat.BackEndConecta.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;
import pe.idat.BackEndConecta.entity.enums.*;

import java.math.BigDecimal;

@Data
public class DireccionDTO {
    private Integer id;

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
