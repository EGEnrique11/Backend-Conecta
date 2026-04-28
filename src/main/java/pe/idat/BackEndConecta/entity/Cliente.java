package pe.idat.BackEndConecta.entity;

import jakarta.persistence.*;
import lombok.*;
import pe.idat.BackEndConecta.entity.enums.EstadoCliente;

@Entity
@Table(name = "cliente")
@PrimaryKeyJoinColumn(name = "id")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Cliente extends Persona {

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, columnDefinition = "varchar(20) default 'PRECLIENTE'")
    private EstadoCliente estado = EstadoCliente.PRECLIENTE;

    @PrePersist
    public void prePersist() {
        if (estado == null) {
            estado = EstadoCliente.PRECLIENTE;
        }
    }
}
