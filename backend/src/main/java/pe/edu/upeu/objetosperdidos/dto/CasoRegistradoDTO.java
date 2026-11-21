package pe.edu.upeu.objetosperdidos.dto;

import java.time.LocalDateTime;
import pe.edu.upeu.objetosperdidos.entity.EstadoReclamo;
import pe.edu.upeu.objetosperdidos.entity.TipoReclamo;

/** Respuesta sin nombres, documentos, fotografias privadas ni datos de terceros. */
public record CasoRegistradoDTO(
        Long idReclamo,
        TipoReclamo tipo,
        EstadoReclamo estado,
        Long idEntrega,
        Long idCustodia,
        Long idHallazgo,
        String descripcion,
        String resolucion,
        boolean tieneEvidencia,
        LocalDateTime fechaRegistro,
        LocalDateTime fechaResolucion) { }
