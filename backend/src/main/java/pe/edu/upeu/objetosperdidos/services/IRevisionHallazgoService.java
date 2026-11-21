package pe.edu.upeu.objetosperdidos.services;

import java.util.List;
import pe.edu.upeu.objetosperdidos.dto.HallazgoPendienteRevisionDTO;
import pe.edu.upeu.objetosperdidos.dto.HallazgoRevisionResultadoDTO;
import pe.edu.upeu.objetosperdidos.dto.ResolverHallazgoRequest;

public interface IRevisionHallazgoService {
    List<HallazgoPendienteRevisionDTO> pendientes();
    HallazgoRevisionResultadoDTO resolver(
            Long idHallazgo, ResolverHallazgoRequest solicitud, Long idPersonaRevisor);

    IFileStorageService.Imagen leerImagen(Long idHallazgo);
}
