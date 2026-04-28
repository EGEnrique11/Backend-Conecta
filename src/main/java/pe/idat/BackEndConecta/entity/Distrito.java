package pe.idat.BackEndConecta.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "distrito")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Distrito {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(nullable = false, length = 100)
    private String nombre;

    @com.fasterxml.jackson.annotation.JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "provincia_id", nullable = false)
    private Provincia provincia;
}
