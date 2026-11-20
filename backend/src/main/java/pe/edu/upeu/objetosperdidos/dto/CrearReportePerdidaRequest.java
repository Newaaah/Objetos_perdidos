package pe.edu.upeu.objetosperdidos.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

public record CrearReportePerdidaRequest(
        @NotNull(message = "La categoria es obligatoria")
        @Positive(message = "El ID de categoria debe ser positivo")
        @Schema(description = "ID de una categoria existente", example = "3")
        Long idCategoria,

        @NotBlank(message = "El nombre del objeto es obligatorio")
        @Size(max = 100, message = "El nombre no puede superar 100 caracteres")
        @Schema(example = "Guitarra acustica de prueba")
        String nombre,

        @Size(max = 100, message = "La marca no puede superar 100 caracteres")
        @Schema(example = "Yamaha")
        String marca,

        @Size(max = 50, message = "El color no puede superar 50 caracteres")
        @Schema(example = "Negro")
        String color,

        @NotBlank(message = "La descripcion es obligatoria")
        @Size(max = 255, message = "La descripcion no puede superar 255 caracteres")
        @Schema(example = "Objeto de prueba, dejado cerca del polideportivo")
        String descripcion,

        @Positive(message = "El ID de ubicacion debe ser positivo")
        @Schema(description = "Ubicacion aproximada; puede omitirse", example = "2")
        Long idUbicacionPerdida,

        @Schema(description = "Fecha estimada de perdida; puede omitirse", example = "2026-11-20")
        LocalDate fechaPerdida
) {
}
