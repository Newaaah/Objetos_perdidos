package pe.edu.upeu.objetosperdidos.dto;

import java.time.LocalDate;
import java.time.LocalDateTime;
import pe.edu.upeu.objetosperdidos.entity.EstadoReportePerdida;

public record ReportePerdidaCreadoDTO(
        Long idReportePerdida,
        Long idObjeto,
        Long idPersona,
        String nombreObjeto,
        Long idCategoria,
        Long idUbicacionPerdida,
        LocalDate fechaPerdida,
        LocalDateTime fechaReporte,
        EstadoReportePerdida estado
) {
}
