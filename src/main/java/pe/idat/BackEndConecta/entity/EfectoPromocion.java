package pe.idat.BackEndConecta.entity;

import jakarta.persistence.*;
import lombok.*;
import pe.idat.BackEndConecta.entity.enums.TipoEfectoPromocion;

import java.math.BigDecimal;

@Entity
@Table(name = "efecto_promocion")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EfectoPromocion {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "promocion_id", nullable = false)
    private Promocion promocion;

    @Enumerated(EnumType.STRING)
    @Column(name = "tipo_efecto", nullable = false)
    private TipoEfectoPromocion tipoEfecto;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal valor;

    @Column(name = "duracion_meses", nullable = false)
    private Integer duracionMeses;
}
