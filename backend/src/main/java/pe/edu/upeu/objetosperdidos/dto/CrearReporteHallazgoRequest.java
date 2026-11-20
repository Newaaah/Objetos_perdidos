package pe.edu.upeu.objetosperdidos.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

@Schema(description = "Datos descriptivos del hallazgo. La imagen se carga aparte.")
public record CrearReporteHallazgoRequest(
        @NotNull(message = "La categoria es obligatoria")
        @Positive(message = "El ID de categoria debe ser positivo")
        @Schema(description = "Categoria existente", example = "2")
        Long idCategoria,

        @NotBlank(message = "El nombre del objeto es obligatorio")
        @Size(max = 100, message = "El nombre no puede superar 100 caracteres")
        @Schema(example = "Mochila hallada de prueba")
        String nombre,

        @Size(max = 100, message = "La marca no puede superar 100 caracteres")
        @Schema(example = "Sin marca")
        String marca,

        @Size(max = 50, message = "El color no puede superar 50 caracteres")
        @Schema(example = "Azul")
        String color,

        @NotBlank(message = "La descripcion es obligatoria")
        @Size(max = 255, message = "La descripcion no puede superar 255 caracteres")
        @Schema(example = "Objeto encontrado cerca del CRAI")
        String descripcion,

        @NotNull(message = "La ubicacion del hallazgo es obligatoria")
        @Positive(message = "El ID de ubicacion encontrada debe ser positivo")
        @Schema(description = "Donde se encontro el objeto", example = "1")
        Long idUbicacionEncontrado,

        @NotNull(message = "La ubicacion de destino es obligatoria")
        @Positive(message = "El ID de ubicacion destino debe ser positivo")
        @Schema(description = "Destino previsto; no confirma recepcion en custodia", example = "2")
        Long idUbicacionDestino
) {
}
