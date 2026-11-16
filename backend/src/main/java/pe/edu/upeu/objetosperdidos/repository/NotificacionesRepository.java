package pe.edu.upeu.objetosperdidos.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import pe.edu.upeu.objetosperdidos.entity.Notificaciones;
import java.util.List;

@Repository
public interface NotificacionesRepository extends JpaRepository<Notificaciones, Long> {
    List<Notificaciones> findByPersonaIdPersonaOrderByFechaCreacionDesc(Long idPersona);
    List<Notificaciones> findByPersonaIdPersonaAndLeidaOrderByFechaCreacionDesc(Long idPersona, Integer leida);
    long countByPersonaIdPersonaAndLeida(Long idPersona, Integer leida);
}
