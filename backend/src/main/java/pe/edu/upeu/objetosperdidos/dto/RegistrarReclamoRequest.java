package pe.edu.upeu.objetosperdidos.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

/**
 * Reclamo sobre una ENTREGA ya realizada, no sobre una custodia pendiente.
 * La evidencia (foto opcional) se envia como archivo en la parte "evidencia".
 */
public record RegistrarReclamoRequest(
        @NotNull @Positive Long idEntrega,
        @NotBlank @Size(min = 15, max = 255) String descripcion) { }
