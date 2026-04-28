package pe.idat.BackEndConecta.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "historial_suspension")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class HistorialSuspension {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "contrato_id", nullable = false)
    private Contrato contrato;

    @Column(name = "fecha_suspension", nullable = false)
    private LocalDateTime fechaSuspension;

    @Column(name = "fecha_reactivacion")
    private LocalDateTime fechaReactivacion;

    @Column(name = "dias_suspendidos")
    @Builder.Default
    private Integer diasSuspendidos = 0;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "aplicado_en_recibo_id")
    private Recibo aplicadoEnRecibo;
}
