package pe.idat.BackEndConecta.dto;

import lombok.Data;
import java.time.LocalDate;

@Data
public class PromocionDTO {
    private Integer id;
    private String nombre;
    private String descripcion;
    private LocalDate fechaInicio;
    private LocalDate fechaFin;
}
