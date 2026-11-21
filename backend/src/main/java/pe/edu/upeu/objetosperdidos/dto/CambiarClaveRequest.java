package pe.edu.upeu.objetosperdidos.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CambiarClaveRequest(
        @NotBlank String claveActual,
        @NotBlank @Size(min = 12, max = 72) String nuevaClave) { }
