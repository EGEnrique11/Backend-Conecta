package pe.idat.BackEndConecta.dto;

import lombok.Data;

@Data
public class ServicioDTO {
    private Integer id;
    private String nombre;
    private String descripcion;
    private Boolean activo;
}
