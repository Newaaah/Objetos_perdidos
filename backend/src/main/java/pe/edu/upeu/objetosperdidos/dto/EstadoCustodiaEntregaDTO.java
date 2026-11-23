package pe.edu.upeu.objetosperdidos.dto;

public record EstadoCustodiaEntregaDTO(
        Long idCustodia,
        Long idObjeto,
        String nombreObjeto,
        boolean disponibleParaEntrega,
        Long idEntrega,
        // NUEVO: coincidencia CONFIRMADA del hallazgo de esta custodia (si existe)
        Long idCoincidenciaConfirmada,
        Long idReporteConfirmado,
        String nombreReporteConfirmado,
        Long idPersonaReporteConfirmado
) { }