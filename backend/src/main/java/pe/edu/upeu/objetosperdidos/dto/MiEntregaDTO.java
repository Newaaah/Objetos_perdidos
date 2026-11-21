package pe.edu.upeu.objetosperdidos.dto;

import java.time.LocalDateTime;

/** No incluye evidencia fotografica, observaciones ni identificadores de terceros. */
public record MiEntregaDTO(
        Long idEntrega, Long idCustodia, Long idReportePerdida,
        String nombreObjeto, LocalDateTime fechaEntrega) { }
