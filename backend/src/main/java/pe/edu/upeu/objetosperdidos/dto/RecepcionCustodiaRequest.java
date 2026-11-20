package pe.edu.upeu.objetosperdidos.dto;

import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

/**
 * Recepcion fisica de un objeto cuyo hallazgo ya fue aprobado.
 * La identidad del personal se obtiene del JWT, no de este DTO.
 */
public record RecepcionCustodiaRequest(
        @NotNull(message = "El ID del hallazgo es obligatorio")
        @Positive(message = "El ID del hallazgo debe ser positivo")
        Long idHallazgo,

        @NotNull(message = "La ubicacion actual es obligatoria")
        @Positive(message = "El ID de ubicacion actual debe ser positivo")
        Long idUbicacionActual,

        // Opcional: solamente si se sabe quien entrego fisicamente el objeto.
        @Positive(message = "El ID de quien entrega debe ser positivo")
        Long idPersonaEntrega,

        @Size(max = 255, message = "La observacion no debe superar 255 caracteres")
        String observacionRecepcion,

        @NotNull(message = "Debes confirmar la recepcion fisica")
        @AssertTrue(message = "Debes confirmar que el objeto fue recibido fisicamente")
        Boolean confirmoRecepcionFisica
) {
}
