package pe.idat.BackEndConecta.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "notificacion")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Notificacion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cliente_id", nullable = false)
    private Cliente cliente;

    @Column(nullable = false, length = 20)
    private String tipo;

    @Column(length = 20)
    @Builder.Default
    private String estado = "PENDIENTE";

    @Column(name = "fecha_envio")
    private LocalDateTime fechaEnvio;

    @Column(columnDefinition = "TEXT", nullable = false)
    private String mensaje;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;
}
