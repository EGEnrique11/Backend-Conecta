package pe.idat.BackEndConecta.dto;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;

@Data
@Builder
@lombok.NoArgsConstructor
@lombok.AllArgsConstructor
public class InstalacionPendienteDTO {
    private Integer id;
    private Integer contratoId;
    private String nombreCliente;
    private String direccionCompleta;
    private LocalDate fechaProgramada;
    private String franjaHoraria;
    private String estado;
    private String tecnicoNombre;
    private String celularCliente;
    private String documentoCliente;
}
