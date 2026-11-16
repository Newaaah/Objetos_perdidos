package pe.edu.upeu.objetosperdidos.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import pe.edu.upeu.objetosperdidos.entity.PersonalUbicacion;
import java.util.List;

@Repository
public interface PersonalUbicacionRepository extends JpaRepository<PersonalUbicacion, Long> {
    List<PersonalUbicacion> findByUbicacionIdUbicacionAndFechaFinIsNull(Long idUbicacion);
    List<PersonalUbicacion> findByPersonaIdPersonaAndFechaFinIsNull(Long idPersona);
    boolean existsByPersonaIdPersonaAndUbicacionIdUbicacionAndFechaFinIsNull(Long idPersona, Long idUbicacion);
}
