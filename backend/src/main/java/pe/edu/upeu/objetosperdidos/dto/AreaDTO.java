package pe.edu.upeu.objetosperdidos.dto;

import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record AreaDTO(

        @Parameter(hidden = true)
        @Schema(accessMode = Schema.AccessMode.READ_ONLY)
        Long idArea,

        @NotBlank(message = "El nombre del area es obligatorio")
        @Size(max = 50, message = "El nombre no puede superar 50 caracteres")
        @Schema(example = "Servicios Generales")
        String nombre

) {
}
