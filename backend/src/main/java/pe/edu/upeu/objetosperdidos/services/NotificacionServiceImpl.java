package pe.edu.upeu.objetosperdidos.services;

import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pe.edu.upeu.objetosperdidos.dto.ConteoNoLeidasDTO;
import pe.edu.upeu.objetosperdidos.dto.NotificacionDTO;
import pe.edu.upeu.objetosperdidos.entity.Notificaciones;
import pe.edu.upeu.objetosperdidos.exception.ResourceNotFoundException;
import pe.edu.upeu.objetosperdidos.repository.NotificacionesRepository;

@Service
public class NotificacionServiceImpl implements INotificacionService {
    private final NotificacionesRepository repo;
    public NotificacionServiceImpl(NotificacionesRepository repo) { this.repo = repo; }

    @Override
    @Transactional(readOnly = true)
    public List<NotificacionDTO> mias(Long idPersona, boolean soloNoLeidas) {
        validar(idPersona);
        List<Notificaciones> listado = soloNoLeidas
                ? repo.findByPersonaIdPersonaAndLeidaOrderByFechaCreacionDescIdNotificacionDesc(idPersona, 0)
                : repo.findByPersonaIdPersonaOrderByFechaCreacionDescIdNotificacionDesc(idPersona);
        return listado.stream().map(NotificacionServiceImpl::aDto).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public ConteoNoLeidasDTO pendientes(Long idPersona) {
        validar(idPersona);
        return new ConteoNoLeidasDTO(repo.countByPersonaIdPersonaAndLeida(idPersona, 0));
    }

    @Override
    @Transactional
    public NotificacionDTO marcarLeida(Long idNotificacion, Long idPersona) {
        validar(idPersona); validar(idNotificacion);
        Notificaciones n = repo.findById(idNotificacion)
                .orElseThrow(() -> new ResourceNotFoundException("Notificacion no encontrada"));
        if (!idPersona.equals(n.getPersona().getIdPersona())) {
            throw new ResourceNotFoundException("Notificacion no encontrada");
        }
        if (!Integer.valueOf(1).equals(n.getLeida())) {
            n.setLeida(1);
            repo.saveAndFlush(n);
        }
        return aDto(n);
    }

    private static NotificacionDTO aDto(Notificaciones n) {
        return new NotificacionDTO(n.getIdNotificacion(), n.getTipo(), n.getTitulo(), n.getMensaje(),
                n.getFechaCreacion(), Integer.valueOf(1).equals(n.getLeida()),
                n.getReportePerdida() == null ? null : n.getReportePerdida().getIdReportePerdida(),
                n.getHallazgo() == null ? null : n.getHallazgo().getIdHallazgo());
    }
    private static void validar(Long id) {
        if (id == null || id <= 0) throw new IllegalArgumentException("Identificador invalido");
    }
}
