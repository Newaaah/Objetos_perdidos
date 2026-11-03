package pe.edu.upeu.objetosperdidos.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import pe.edu.upeu.objetosperdidos.entity.Reclamos;

@Repository
public interface ReclamosRepository
        extends JpaRepository<Reclamos, Long> {

}
