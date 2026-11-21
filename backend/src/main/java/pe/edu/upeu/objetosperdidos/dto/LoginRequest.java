package pe.edu.upeu.objetosperdidos.dto;

import jakarta.validation.constraints.NotBlank;

import io.swagger.v3.oas.annotations.media.Schema;

public record LoginRequest(

        @NotBlank(message = "Ingrese su codigo universitario o DNI")
        @Schema(
                description = "Codigo universitario o DNI del usuario",
                example = "202312345"
        )
        String ingreso,

        @Schema(
                description = "Contraseña obligatoria para PERSONAL y ADMIN. " +
                        "Estudiante: solo si configuro una contraseña"
        )
        String password

) {
}
