package pe.idat.BackEndConecta.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import pe.idat.BackEndConecta.entity.Notificacion;

public interface NotificacionRepository extends JpaRepository<Notificacion, Integer> {
}
