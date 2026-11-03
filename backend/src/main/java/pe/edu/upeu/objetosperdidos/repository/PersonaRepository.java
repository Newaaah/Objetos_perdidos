package pe.edu.upeu.objetosperdidos.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import pe.edu.upeu.objetosperdidos.entity.Persona;

@Repository
public interface PersonaRepository
        extends JpaRepository<Persona, Long> {

}

