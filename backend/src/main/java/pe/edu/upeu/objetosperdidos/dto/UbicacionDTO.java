package pe.edu.upeu.objetosperdidos.dto;

import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record UbicacionDTO(

        @Parameter(hidden = true)
        @Schema(accessMode = Schema.AccessMode.READ_ONLY)
        Long idUbicacion,

        @NotBlank(message = "El nombre de la ubicacion es obligatorio")
        @Size(max = 100, message = "El nombre no puede superar 100 caracteres")
        @Schema(example = "Garita")
        String nombre,

        @Size(max = 255, message = "El detalle no puede superar 255 caracteres")
        @Schema(example = "Punto autorizado de recepcion de objetos")
        String detalle

) {
}
