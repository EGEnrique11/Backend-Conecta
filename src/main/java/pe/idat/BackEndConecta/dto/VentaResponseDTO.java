package pe.idat.BackEndConecta.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class VentaResponseDTO {
    private Integer contratoId;
    private Integer instalacionId;
    private String mensaje;
}
