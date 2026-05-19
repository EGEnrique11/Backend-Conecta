package pe.idat.BackEndConecta.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import lombok.Data;

@Data
public class ContratoResumenDTO {
    private Integer id;
    private String estado;
    private BigDecimal tarifaMensual;
    private LocalDateTime fechaAlta;
    private String planNombre;
    private String planVelocidad;
    private Integer diaCierre;
    private String vendedorNombres;
}
