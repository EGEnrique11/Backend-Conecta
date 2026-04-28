package pe.idat.BackEndConecta.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

import java.time.LocalDate;

@Data
public class VentaCompletaRequestDTO {
    private Integer clienteId; 
    private Integer direccionId; 
    
    @Valid 
    private ClienteDTO datosCliente; 
    
    @Valid 
    private DireccionDTO datosDireccion; 
    
    @NotNull(message = "El planId es requerido") 
    private Integer planId;
    
    private Integer promocionId;
    
    @NotNull(message = "La fechaProgramada es requerida") 
    @FutureOrPresent(message = "La fecha no puede ser en el pasado") 
    private LocalDate fechaProgramada;
    
    @NotNull(message = "La franja horaria es requerida") 
    @Pattern(regexp = "^(MAÑANA|TARDE)$", message = "La franja horaria debe ser MAÑANA o TARDE") 
    private String franjaHoraria;
    
    private Boolean necesitaRouter;
}
