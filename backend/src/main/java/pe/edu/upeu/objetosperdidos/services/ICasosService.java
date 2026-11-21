package pe.edu.upeu.objetosperdidos.services;

import java.util.List;
import org.springframework.web.multipart.MultipartFile;
import pe.edu.upeu.objetosperdidos.dto.CasoRegistradoDTO;
import pe.edu.upeu.objetosperdidos.dto.RegistrarIncidenciaRequest;
import pe.edu.upeu.objetosperdidos.dto.RegistrarReclamoRequest;
import pe.edu.upeu.objetosperdidos.dto.RevisarCasoRequest;
import pe.edu.upeu.objetosperdidos.entity.EstadoReclamo;
import pe.edu.upeu.objetosperdidos.entity.TipoReclamo;
import pe.edu.upeu.objetosperdidos.security.UsuarioPrincipal;

/** Reclamos (estudiantes) e incidencias (personal) comparten la tabla RECLAMOS. */
public interface ICasosService {

    CasoRegistradoDTO reclamar(RegistrarReclamoRequest solicitud, MultipartFile evidencia,
                               UsuarioPrincipal usuario);

    CasoRegistradoDTO incidencia(RegistrarIncidenciaRequest solicitud, MultipartFile evidencia,
                                 Long idPersonal);

    List<CasoRegistradoDTO> misReclamos(Long idPersona);

    CasoRegistradoDTO consultarReclamo(Long idCaso, UsuarioPrincipal usuario);

    CasoRegistradoDTO consultarIncidencia(Long idCaso, Long idPersonal);

    IFileStorageService.Imagen leerEvidencia(Long idCaso, TipoReclamo tipo, UsuarioPrincipal usuario);

    List<CasoRegistradoDTO> pendientes(TipoReclamo tipo, Long idPersonal);

    List<CasoRegistradoDTO> listar(TipoReclamo tipo, EstadoReclamo estado, Long idPersonal);

    CasoRegistradoDTO revisar(Long idCaso, TipoReclamo tipo, RevisarCasoRequest solicitud,
                              Long idPersonal);
}
