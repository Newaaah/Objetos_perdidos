package pe.edu.upeu.objetosperdidos.services;

import java.util.List;
import pe.edu.upeu.objetosperdidos.dto.ConteoNoLeidasDTO;
import pe.edu.upeu.objetosperdidos.dto.NotificacionDTO;

/** Bandeja de notificaciones de la persona autenticada. */
public interface INotificacionService {

    List<NotificacionDTO> mias(Long idPersona, boolean soloNoLeidas);

    ConteoNoLeidasDTO pendientes(Long idPersona);

    NotificacionDTO marcarLeida(Long idNotificacion, Long idPersona);
}
