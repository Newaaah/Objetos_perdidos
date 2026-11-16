package pe.edu.upeu.objetosperdidos.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import pe.edu.upeu.objetosperdidos.entity.Estudiante;
import java.util.Optional;

@Repository
public interface EstudianteRepository extends JpaRepository<Estudiante, Long> {
    Optional<Estudiante> findByCodigoUni(String codigoUni);
    boolean existsByCodigoUni(String codigoUni);
}
