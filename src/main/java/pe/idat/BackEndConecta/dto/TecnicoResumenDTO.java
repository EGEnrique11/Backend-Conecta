package pe.idat.BackEndConecta.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TecnicoResumenDTO {
    private Integer id;
    private String nombreCompleto;
    private String documento;
    private String celular;
    private Integer turnoId;
    private String turnoNombre;
}
