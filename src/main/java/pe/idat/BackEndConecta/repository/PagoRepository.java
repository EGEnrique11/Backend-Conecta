package pe.idat.BackEndConecta.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import pe.idat.BackEndConecta.entity.Pago;

public interface PagoRepository extends JpaRepository<Pago, Integer> {
}
