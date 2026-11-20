package pe.edu.upeu.objetosperdidos.dto;

import java.time.LocalDateTime;
import pe.edu.upeu.objetosperdidos.entity.EstadoReporteHallazgo;

public record ReporteHallazgoCreadoDTO(
        Long idHallazgo,
        Long idObjeto,
        Long idPersona,
        String nombreObjeto,
        Long idCategoria,
        Long idUbicacionEncontrado,
        Long idUbicacionDestino,
        String imagenRef,
        LocalDateTime fechaReporte,
        EstadoReporteHallazgo estado
) {
}
