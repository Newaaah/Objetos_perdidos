package pe.edu.upeu.objetosperdidos.dto;

import java.time.LocalDateTime;

/**
 * Objeto que sigue bajo custodia de la UPeU (vista publica, sin login).
 * No incluye la descripcion detallada: esos detalles los verifica
 * el personal en persona para evitar reclamos falsos.
 */
public record ObjetoPublicoDTO(
        Long idCustodia,
        String nombre,
        String categoria,
        String marca,
        String color,
        String ubicacionActual,
        LocalDateTime fechaRegistro,
        String imagenUrl
) {
}
