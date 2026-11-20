package pe.edu.upeu.objetosperdidos.dto;

import java.time.LocalDateTime;

/** Datos que el personal necesita para revisar un hallazgo pendiente. */
public record HallazgoPendienteRevisionDTO(
        Long idHallazgo,
        Long idObjeto,
        Long idPersonaReporta,
        String nombreObjeto,
        String categoria,
        String marca,
        String color,
        String descripcion,
        String ubicacionEncontrado,
        String ubicacionDestino,
        LocalDateTime fechaReporte,
        String estado,
        String imagenRef
) {
}
