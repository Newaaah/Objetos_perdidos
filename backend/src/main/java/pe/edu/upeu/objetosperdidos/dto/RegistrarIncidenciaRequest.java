package pe.edu.upeu.objetosperdidos.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

/** Problema ocurrido durante la CUSTODIA (solo personal autorizado). */
public record RegistrarIncidenciaRequest(
        @NotNull @Positive Long idCustodia,
        @NotBlank @Size(min = 15, max = 255) String descripcion) { }
