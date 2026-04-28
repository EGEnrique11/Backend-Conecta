package pe.idat.BackEndConecta.entity;

import jakarta.persistence.*;
import lombok.*;
import pe.idat.BackEndConecta.entity.enums.TipoUrbanizacion;
import pe.idat.BackEndConecta.entity.enums.TipoVia;
import pe.idat.BackEndConecta.entity.enums.TipoVivienda;

import java.math.BigDecimal;

@Entity
@Table(name = "direccion")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Direccion {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cliente_id", nullable = false)
    private Cliente cliente;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "distrito_id", nullable = false)
    private Distrito distrito;

    @Enumerated(EnumType.STRING)
    @Column(name = "tipo_via", nullable = false)
    private TipoVia tipoVia;

    @Column(name = "nombre_via", length = 100)
    private String nombreVia;

    @Column(length = 20)
    private String numero;

    @Enumerated(EnumType.STRING)
    @Column(name = "tipo_urbanizacion")
    private TipoUrbanizacion tipoUrbanizacion;

    @Column(name = "nombre_urbanizacion", length = 100)
    private String nombreUrbanizacion;

    @Column(length = 20)
    private String manzana;

    @Column(length = 20)
    private String lote;

    @Column(length = 20)
    private String piso;

    @Column(length = 20)
    private String interior;

    @Enumerated(EnumType.STRING)
    @Column(name = "tipo_vivienda")
    private TipoVivienda tipoVivienda;

    @Column(name = "direccion_completa", nullable = false, length = 255)
    private String direccionCompleta;

    private String referencia;

    @Column(precision = 10, scale = 7)
    private BigDecimal latitud;

    @Column(precision = 10, scale = 7)
    private BigDecimal longitud;

    @Column(name = "is_principal")
    @Builder.Default
    private Boolean isPrincipal = false;

    @Builder.Default
    private Boolean activo = true;
}
