package pe.edu.upeu.objetosperdidos.dto;

public record EstadoCustodiaEntregaDTO(
        Long idCustodia,
        Long idObjeto,
        String nombreObjeto,
        boolean disponibleParaEntrega,
        Long idEntrega
) { }
