package pe.edu.upeu.objetosperdidos.dto;

public record PersonaResumenDTO(
        Long idPersona,
        String nombres,
        String apellidos,
        String documentoIdentidad,
        String codigoUni,
        String carrera
) {
}