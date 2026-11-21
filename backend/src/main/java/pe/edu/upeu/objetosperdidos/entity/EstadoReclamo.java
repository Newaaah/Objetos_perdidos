package pe.edu.upeu.objetosperdidos.entity;

/**
 * RECLAMO:    PENDIENTE -> EN_REVISION -> APROBADO | RECHAZADO
 * INCIDENCIA: PENDIENTE -> EN_REVISION -> RESUELTO
 */
public enum EstadoReclamo {
    PENDIENTE,
    EN_REVISION,
    APROBADO,
    RECHAZADO,
    RESUELTO;

    public boolean esFinal() {
        return this == APROBADO || this == RECHAZADO || this == RESUELTO;
    }
}
