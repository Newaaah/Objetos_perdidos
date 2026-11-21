package pe.edu.upeu.objetosperdidos.dto;

/**
 * Vista mínima para el estudiante: sin marca, color, descripción privada,
 * fotografía, identidad de otras personas ni puntuación por característica.
 */
public record SugerenciaCoincidenciaDTO(
        String origen,
        Long idReferencia,
        String categoria,
        String nivelCoincidencia
) { }
