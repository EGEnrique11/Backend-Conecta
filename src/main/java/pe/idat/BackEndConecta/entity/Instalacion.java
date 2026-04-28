package pe.idat.BackEndConecta.entity;

import jakarta.persistence.*;
import lombok.*;
import pe.idat.BackEndConecta.entity.enums.EstadoInstalacion;

import java.time.LocalDate;

@Entity
@Table(name = "instalacion")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Instalacion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "contrato_id", nullable = false)
    private Contrato contrato;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tecnico_id")
    private Empleado tecnico;

    @Column(name = "fecha_programada", nullable = false)
    private LocalDate fechaProgramada;

    @Column(name = "franja_horaria", nullable = false, length = 20)
    private String franjaHoraria;

    @Column(name = "bloque_asignado", length = 50)
    private String bloqueAsignado;

    @Enumerated(EnumType.STRING)
    @Column(length = 20)
    @Builder.Default
    private EstadoInstalacion estado = EstadoInstalacion.PENDIENTE;

    @Column(columnDefinition = "TEXT")
    private String observaciones;
}
