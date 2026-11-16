package pe.edu.upeu.objetosperdidos.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import pe.edu.upeu.objetosperdidos.entity.Personal;
import java.util.List;
import pe.edu.upeu.objetosperdidos.entity.EstadoPersonal;

@Repository
public interface PersonalRepository extends JpaRepository<Personal, Long> {
    List<Personal> findByEstado(EstadoPersonal estado);
    List<Personal> findByAreaIdArea(Long idArea);
}
