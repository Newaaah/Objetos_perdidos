package pe.edu.upeu.objetosperdidos.dto;

/** Solo para PERSONAL/ADMIN: incluye atributos que NO deben exponerse al estudiante. */
public record EvaluacionCoincidenciaDTO(
        Long idReportePerdida,
        Long idHallazgo,
        String estadoPerdida,
        String estadoHallazgo,
        String nombrePerdido,
        String marcaPerdido,
        String colorPerdido,
        String descripcionPerdido,
        String nombreHallado,
        String marcaHallado,
        String colorHallado,
        String descripcionHallado,
        int similitud,
        int cobertura,
        int prioridad,
        boolean objetoEntregado,
        String decisionExistente,
        String aviso
) { }
