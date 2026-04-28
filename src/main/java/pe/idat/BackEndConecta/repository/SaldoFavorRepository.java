package pe.idat.BackEndConecta.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import pe.idat.BackEndConecta.entity.SaldoFavor;

import java.util.List;

public interface SaldoFavorRepository extends JpaRepository<SaldoFavor, Integer> {
    List<SaldoFavor> findByContratoIdAndEstado(Integer contratoId, String estado);
}
