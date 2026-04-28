package pe.idat.BackEndConecta.dto;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class PlanDTO {
    private Integer id;
    private Integer servicioId;
    private String nombreServicio;
    private String nombre;
    private Integer velocidadBaseMbps;
    private BigDecimal precio;
    private String detalle;
    private Boolean activo;
}
