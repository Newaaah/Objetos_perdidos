package pe.edu.upeu.objetosperdidos.dto;

import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

/** La verificacion y la entrega son presenciales, no automaticas. */
public record RegistrarEntregaRequest(
        @NotNull(message = "El ID de custodia es obligatorio")
        @Positive(message = "El ID de custodia debe ser positivo")
        Long idCustodia,

        @NotNull(message = "La persona que recibe es obligatoria")
        @Positive(message = "El ID de la persona debe ser positivo")
        Long idPersonaRecibe,

        @NotBlank(message = "La observacion de verificacion es obligatoria")
        @Size(max = 255, message = "La observacion no debe superar 255 caracteres")
        String observacionVerificacion,

        @NotNull(message = "Debes confirmar la verificacion presencial")
        @AssertTrue(message = "Debes confirmar la identidad y las pruebas de propiedad presencialmente")
        Boolean confirmoVerificacionPresencial,

        @NotNull(message = "Debes confirmar la entrega fisica")
        @AssertTrue(message = "Debes confirmar que el objeto se entrego fisicamente")
        Boolean confirmoEntregaFisica,

        // Opcional: solo al vincular explicitamente una coincidencia CONFIRMADA.
        @Positive(message = "El ID de coincidencia debe ser positivo")
        Long idCoincidencia,

        // Opcional: asociacion manual de una perdida sin coincidencia formal,
        // SOLO despues de que el personal verifique propiedad y entrega presencial.
        @Positive(message = "El ID del reporte debe ser positivo")
        Long idReportePerdida
) {
}
