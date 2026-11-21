package pe.edu.upeu.objetosperdidos.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import pe.edu.upeu.objetosperdidos.entity.EstadoReclamo;

public record RevisarCasoRequest(
        @NotNull EstadoReclamo estado,
        @Size(max = 500) String resolucion) { }
