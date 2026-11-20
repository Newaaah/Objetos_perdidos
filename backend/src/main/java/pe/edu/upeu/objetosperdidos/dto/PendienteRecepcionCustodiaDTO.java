package pe.edu.upeu.objetosperdidos.dto;

import java.time.LocalDateTime;

public record PendienteRecepcionCustodiaDTO(
        Long idHallazgo,
        Long idObjeto,
        String nombreObjeto,
        Long idUbicacionDestino,
        String ubicacionDestino,
        LocalDateTime fechaReporte
) {
}
