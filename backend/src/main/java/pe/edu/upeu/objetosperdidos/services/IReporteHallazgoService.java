package pe.edu.upeu.objetosperdidos.services;

import java.util.List;
import org.springframework.web.multipart.MultipartFile;
import pe.edu.upeu.objetosperdidos.dto.CrearReporteHallazgoRequest;
import pe.edu.upeu.objetosperdidos.dto.MiHallazgoDTO;
import pe.edu.upeu.objetosperdidos.dto.ReporteHallazgoCreadoDTO;

public interface IReporteHallazgoService {

    ReporteHallazgoCreadoDTO crear(
            CrearReporteHallazgoRequest datos,
            MultipartFile imagen,
            Long idPersona
    );

    List<MiHallazgoDTO> misHallazgos(Long idPersona);
}
