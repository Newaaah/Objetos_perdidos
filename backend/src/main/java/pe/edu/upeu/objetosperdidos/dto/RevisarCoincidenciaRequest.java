package pe.edu.upeu.objetosperdidos.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import pe.edu.upeu.objetosperdidos.entity.EstadoCoincidencia;

/** Decision humana: los identificadores de revisor y fecha no se aceptan del cliente. */
public record RevisarCoincidenciaRequest(
        @NotNull @Positive Long idReportePerdida,
        @NotNull @Positive Long idHallazgo,
        @NotNull EstadoCoincidencia decision,
        @NotBlank @Size(max = 255) String observacion
) { }
