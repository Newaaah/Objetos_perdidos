package pe.edu.upeu.objetosperdidos.dto;

import java.util.List;

public record ResultadoSugerenciasDTO(
        Long idReportePerdida,
        String estadoReporte,
        List<SugerenciaCoincidenciaDTO> sugerencias,
        String aviso
) { }
