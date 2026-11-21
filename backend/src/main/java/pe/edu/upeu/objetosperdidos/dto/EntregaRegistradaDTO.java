package pe.edu.upeu.objetosperdidos.dto;

import java.time.LocalDateTime;

public record EntregaRegistradaDTO(
        Long idEntrega,
        Long idCustodia,
        Long idObjeto,
        String nombreObjeto,
        Long idPersonaRecibe,
        Long idPersonalEntrega,
        LocalDateTime fechaEntrega,
        String observacionVerificacion,
        String fotoEvidenciaRef,
        Long idCoincidencia,
        Long idReportePerdida
) {
}
