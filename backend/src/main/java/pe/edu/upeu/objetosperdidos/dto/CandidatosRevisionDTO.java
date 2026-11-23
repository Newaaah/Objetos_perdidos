package pe.edu.upeu.objetosperdidos.dto;

import java.util.List;

/** Listas para elegir la pareja a evaluar (solo PERSONAL/ADMIN). */
public record CandidatosRevisionDTO(
        List<MiPerdidaDTO> perdidas,     // reportes de perdida REPORTADO
        List<MiHallazgoDTO> hallazgos    // hallazgos APROBADO aun no entregados
) {
}