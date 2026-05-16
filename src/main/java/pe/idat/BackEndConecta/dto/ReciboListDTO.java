package pe.idat.BackEndConecta.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReciboListDTO {
    private Integer id;
    private Integer contratoId;
    private LocalDate fechaEmision;
    private LocalDate fechaVencimiento;
    private LocalDate periodoInicio;
    private LocalDate periodoFin;
    private BigDecimal montoTotal;
    private String estadoPago;
    
    // Payment specific details
    private LocalDateTime fechaPago;
    private String metodoPago;
}
