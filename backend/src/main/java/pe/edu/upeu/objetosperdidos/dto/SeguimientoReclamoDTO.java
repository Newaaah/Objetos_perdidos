package pe.edu.upeu.objetosperdidos.dto;

public record SeguimientoReclamoDTO(Long idReclamacion, Long idObjeto, String categoria, String descripcion, String estadoReclamacion, java.time.LocalDateTime fechaSolicitud, java.time.LocalDateTime fechaResolucion, String situacionRevision) {}
