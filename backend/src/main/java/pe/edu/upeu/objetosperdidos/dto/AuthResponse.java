package pe.edu.upeu.objetosperdidos.dto;

import io.swagger.v3.oas.annotations.media.Schema;

public record AuthResponse(

        @Schema(
                description = "Token JWT generado al iniciar sesion"
        )
        String token,

        @Schema(
                description = "Tipo de token",
                example = "Bearer"
        )
        String tokenType,

        @Schema(
                description = "Identificador de la persona autenticada",
                example = "1"
        )
        Long idPersona,

        @Schema(
                description = "Rol del usuario autenticado",
                example = "ESTUDIANTE"
        )
        String rol

) {
}
