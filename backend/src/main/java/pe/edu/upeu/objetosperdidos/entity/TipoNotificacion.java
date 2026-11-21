package pe.edu.upeu.objetosperdidos.entity;

/** Tipos de notificacion. Deben coincidir con el CHECK de NOTIFICACIONES.TIPO. */
public enum TipoNotificacion {
    HALLAZGO_APROBADO,
    HALLAZGO_RECHAZADO,
    POSIBLE_COINCIDENCIA,
    OBJETO_EN_CUSTODIA,
    ENTREGA_REGISTRADA,
    HALLAZGO_PENDIENTE_RECEPCION,
    RECLAMO_REGISTRADO,
    RECLAMO_RESUELTO
}
