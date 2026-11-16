package pe.edu.upeu.objetosperdidos.dto;

import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CategoriaDTO(
        @Parameter(hidden = true)
        @Schema(accessMode = Schema.AccessMode.READ_ONLY)
        Long idCategoria,

        @NotBlank(message = "El nombre de la categoria es obligatorio")
        @Size(max = 50, message = "El nombre no puede superar 50 caracteres")
        @Schema(example = "Accesorios")
        String nombre
) {
}
