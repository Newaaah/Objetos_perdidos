package pe.edu.upeu.objetosperdidos.dto;

import java.time.LocalDateTime;

/**
 * Objeto ya entregado (vista publica, sin login).
 * Sirve para que el verdadero dueno reconozca su objeto y reclame.
 * NUNCA incluye quien lo recibio ni la foto de evidencia de la entrega.
 */
public record ObjetoEntregadoPublicoDTO(
        Long idEntrega,
        Long idCustodia,
        String nombre,
        String categoria,
        String marca,
        String color,
        LocalDateTime fechaEntrega,
        String imagenUrl
) {
}
