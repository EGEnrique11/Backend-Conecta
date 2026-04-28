package pe.idat.BackEndConecta.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "ciclo_pago")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CicloPago {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(nullable = false, length = 50)
    private String nombre;

    @Column(name = "dia_emision", columnDefinition = "TINYINT", nullable = false)
    private Integer diaEmision;

    @Column(name = "dia_notificacion", columnDefinition = "TINYINT", nullable = false)
    private Integer diaNotificacion;

    @Column(name = "dia_vencimiento", columnDefinition = "TINYINT", nullable = false)
    private Integer diaVencimiento;

    @Column(name = "dia_corte", columnDefinition = "TINYINT", nullable = false)
    private Integer diaCorte;
}
