package pe.edu.upeu.objetosperdidos.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import pe.edu.upeu.objetosperdidos.entity.Incidencia;

@Repository
public interface IncidenciaRepository
        extends JpaRepository<Incidencia, Long> {

}

