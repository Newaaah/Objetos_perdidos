package pe.edu.upeu.objetosperdidos.dto;

import java.time.LocalDateTime;

public record CoincidenciaRevisadaDTO(
        Long idCoincidencia,
        Long idReportePerdida,
        Long idHallazgo,
        String estado,
        Integer puntaje,
        Long idPersonalRevisor,
        LocalDateTime fechaRevision,
        String observacion
) { }
