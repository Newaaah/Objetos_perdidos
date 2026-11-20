package pe.edu.upeu.objetosperdidos.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record ResolverHallazgoRequest(
        @NotNull(message = "La decision es obligatoria")
        @Schema(description = "APROBADO o RECHAZADO", example = "APROBADO")
        DecisionHallazgo decision,

        @Size(max = 255, message = "La observacion no puede superar 255 caracteres")
        @Schema(description = "Obligatoria cuando se rechaza el hallazgo",
                example = "Descripcion y fotografia revisadas")
        String observacion
) {
}
