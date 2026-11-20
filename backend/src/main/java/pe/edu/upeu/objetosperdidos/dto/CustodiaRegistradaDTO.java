package pe.edu.upeu.objetosperdidos.dto;

import java.time.LocalDateTime;

/**
 * idHallazgo es null cuando el objeto fue recibido directamente.
 * imagenRef apunta a la nueva foto de recepcion o, si no hay una, al hallazgo.
 */
public record CustodiaRegistradaDTO(
        Long idCustodia,
        Long idHallazgo,
        Long idObjeto,
        String nombreObjeto,
        Long idPersonalRegistra,
        Long idUbicacionActual,
        String ubicacionActual,
        Long idPersonaEntrega,
        LocalDateTime fechaRegistro,
        String observacionRecepcion,
        String origen,
        String imagenRef
) {
}
