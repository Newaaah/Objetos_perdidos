package pe.edu.upeu.objetosperdidos.dto;

public record MiHallazgoDTO(Long idHallazgo, Long idObjeto, String nombreObjeto, String categoria, String estado, java.time.LocalDateTime fechaReporte, String ubicacionDestino) {}
