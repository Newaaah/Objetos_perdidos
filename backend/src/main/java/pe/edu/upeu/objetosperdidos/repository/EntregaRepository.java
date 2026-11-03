package pe.edu.upeu.objetosperdidos.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import pe.edu.upeu.objetosperdidos.entity.Entrega;

@Repository
public interface EntregaRepository
        extends JpaRepository<Entrega, Long> {

}

