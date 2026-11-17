package pe.edu.upeu.objetosperdidos.dto;

import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CarreraDTO(

        @Parameter(hidden = true)
        @Schema(accessMode = Schema.AccessMode.READ_ONLY)
        Long idCarrera,

        @NotBlank(message = "El nombre de la carrera es obligatorio")
        @Size(max = 50, message = "El nombre no puede superar 50 caracteres")
        @Schema(example = "Ingenieria de Sistemas")
        String nombre

) {
}
