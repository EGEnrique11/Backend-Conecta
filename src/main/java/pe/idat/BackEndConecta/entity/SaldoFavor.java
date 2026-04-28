package pe.idat.BackEndConecta.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "saldo_favor")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SaldoFavor {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "contrato_id", nullable = false)
    private Contrato contrato;

    @Column(name = "monto_original", precision = 10, scale = 2, nullable = false)
    private BigDecimal montoOriginal;

    @Column(name = "monto_disponible", precision = 10, scale = 2, nullable = false)
    private BigDecimal montoDisponible;

    @Column(nullable = false, length = 50)
    private String origen;

    @Column(length = 20)
    @Builder.Default
    private String estado = "ACTIVO";

    @Column(name = "fecha_generacion", insertable = false, updatable = false)
    private LocalDateTime fechaGeneracion;
}
