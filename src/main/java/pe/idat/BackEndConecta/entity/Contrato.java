package pe.idat.BackEndConecta.entity;

import jakarta.persistence.*;
import lombok.*;
import pe.idat.BackEndConecta.entity.enums.EstadoContrato;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "contrato")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Contrato {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cliente_id", nullable = false)
    private Cliente cliente;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "direccion_id", nullable = false)
    private Direccion direccion;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "plan_id", nullable = false)
    private Plan plan;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "promocion_id")
    private Promocion promocion;

    @Column(name = "fecha_fin_promocion")
    private LocalDate fechaFinPromocion;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ciclo_pago_id", nullable = false)
    private CicloPago cicloPago;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "empleado_registro_id", nullable = false)
    private Empleado empleadoRegistro;

    @Column(name = "fecha_contrato", nullable = false)
    private LocalDate fechaContrato;

    @Column(name = "fecha_activacion")
    private LocalDate fechaActivacion;

    @Column(name = "costo_instalacion", precision = 10, scale = 2)
    @Builder.Default
    private BigDecimal costoInstalacion = BigDecimal.ZERO;

    @Enumerated(EnumType.STRING)
    @Column(length = 20)
    @Builder.Default
    private EstadoContrato estado = EstadoContrato.PENDIENTE;

    @Column(name = "texto_contrato", columnDefinition = "TEXT")
    private String textoContrato;

    @Column(name = "created_at", insertable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", insertable = false, updatable = false)
    private LocalDateTime updatedAt;
}
