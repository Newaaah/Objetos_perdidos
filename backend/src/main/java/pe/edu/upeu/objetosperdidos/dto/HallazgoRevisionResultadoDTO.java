package pe.edu.upeu.objetosperdidos.dto;

import java.time.LocalDateTime;

public record HallazgoRevisionResultadoDTO(
        Long idHallazgo,
        String estado,
        Long idPersonaRevisor,
        LocalDateTime fechaRevision,
        String observacion
) {
}
