package pe.edu.upeu.objetosperdidos.dto;

public record MiPerdidaDTO(Long idReportePerdida, Long idObjeto, String nombreObjeto, String categoria, String estado, java.time.LocalDate fechaPerdida, java.time.LocalDateTime fechaReporte, String ubicacionPerdida) {}
