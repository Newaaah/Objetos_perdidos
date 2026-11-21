package pe.edu.upeu.objetosperdidos.dto;

import java.time.LocalDateTime;
import pe.edu.upeu.objetosperdidos.entity.TipoNotificacion;

/** La bandeja nunca entrega identificadores de personas ajenas ni datos confidenciales. */
public record NotificacionDTO(
        Long idNotificacion,
        TipoNotificacion tipo,
        String titulo,
        String mensaje,
        LocalDateTime fechaCreacion,
        boolean leida,
        Long idReportePerdida,
        Long idHallazgo) { }
