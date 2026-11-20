package pe.edu.upeu.objetosperdidos.dto;

import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

/** Recepcion directa: no existe un reporte previo de hallazgo. */
public record RecepcionDirectaCustodiaRequest(
        @NotNull(message = "La categoria es obligatoria")
        @Positive(message = "El ID de categoria debe ser positivo")
        Long idCategoria,

        @NotBlank(message = "El nombre del objeto es obligatorio")
        @Size(max = 100, message = "El nombre no puede superar 100 caracteres")
        String nombre,

        @Size(max = 100, message = "La marca no puede superar 100 caracteres")
        String marca,

        @Size(max = 50, message = "El color no puede superar 50 caracteres")
        String color,

        @NotBlank(message = "La descripcion es obligatoria")
        @Size(max = 255, message = "La descripcion no puede superar 255 caracteres")
        String descripcion,

        @Positive(message = "El ID de ubicacion encontrada debe ser positivo")
        Long idUbicacionEncontrado,

        @NotNull(message = "La ubicacion de custodia es obligatoria")
        @Positive(message = "El ID de ubicacion actual debe ser positivo")
        Long idUbicacionActual,

        @Positive(message = "El ID de la persona que entrega debe ser positivo")
        Long idPersonaEntrega,

        @Size(max = 255, message = "La observacion no debe superar 255 caracteres")
        String observacionRecepcion,

        @NotNull(message = "Debes confirmar la recepcion fisica")
        @AssertTrue(message = "Debes confirmar que el objeto fue recibido fisicamente")
        Boolean confirmoRecepcionFisica
) {
}
