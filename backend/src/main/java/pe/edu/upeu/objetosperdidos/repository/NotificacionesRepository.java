package pe.edu.upeu.objetosperdidos.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import pe.edu.upeu.objetosperdidos.entity.Notificaciones;
import java.util.List;

@Repository
public interface NotificacionesRepository extends JpaRepository<Notificaciones, Long> {
    List<Notificaciones> findByPersonaIdPersonaOrderByFechaCreacionDescIdNotificacionDesc(Long idPersona);
    List<Notificaciones> findByPersonaIdPersonaAndLeidaOrderByFechaCreacionDescIdNotificacionDesc(Long idPersona, Integer leida);
    boolean existsByPersonaIdPersonaAndTipoAndReportePerdidaIdReportePerdida(
            Long idPersona, pe.edu.upeu.objetosperdidos.entity.TipoNotificacion tipo, Long idReporte);
    boolean existsByPersonaIdPersonaAndTipoAndHallazgoIdHallazgo(
            Long idPersona, pe.edu.upeu.objetosperdidos.entity.TipoNotificacion tipo, Long idHallazgo);
    boolean existsByPersonaIdPersonaAndTipoAndCustodiaIdCustodia(
            Long idPersona, pe.edu.upeu.objetosperdidos.entity.TipoNotificacion tipo, Long idCustodia);
    List<Notificaciones> findByPersonaIdPersonaAndLeidaOrderByFechaCreacionDesc(Long idPersona, Integer leida);
    long countByPersonaIdPersonaAndLeida(Long idPersona, Integer leida);
}
