package pe.idat.BackEndConecta.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "pago")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Pago {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "recibo_id", nullable = false)
    private Recibo recibo;

    @Column(name = "fecha_pago", nullable = false)
    private LocalDateTime fechaPago;

    @Column(name = "monto_pagado", precision = 10, scale = 2, nullable = false)
    private BigDecimal montoPagado;

    @Column(name = "metodo_pago", length = 30, nullable = false)
    private String metodoPago;

    @Column(name = "nro_operacion", length = 50)
    private String nroOperacion;

    @Column(columnDefinition = "TEXT")
    private String observaciones;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "empleado_registro_id", nullable = false)
    private Empleado empleadoRegistro;

    @Column(name = "created_at", insertable = false, updatable = false)
    private LocalDateTime createdAt;
}
