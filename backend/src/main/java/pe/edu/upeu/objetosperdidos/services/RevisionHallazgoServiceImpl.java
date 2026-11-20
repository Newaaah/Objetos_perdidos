package pe.edu.upeu.objetosperdidos.services;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pe.edu.upeu.objetosperdidos.dto.DecisionHallazgo;
import pe.edu.upeu.objetosperdidos.dto.HallazgoPendienteRevisionDTO;
import pe.edu.upeu.objetosperdidos.dto.HallazgoRevisionResultadoDTO;
import pe.edu.upeu.objetosperdidos.dto.ResolverHallazgoRequest;
import pe.edu.upeu.objetosperdidos.entity.EstadoPersonal;
import pe.edu.upeu.objetosperdidos.entity.EstadoReporteHallazgo;
import pe.edu.upeu.objetosperdidos.entity.Objeto;
import pe.edu.upeu.objetosperdidos.entity.Personal;
import pe.edu.upeu.objetosperdidos.entity.ReporteHallazgos;
import pe.edu.upeu.objetosperdidos.exception.ResourceInUseException;
import pe.edu.upeu.objetosperdidos.exception.ResourceNotFoundException;
import pe.edu.upeu.objetosperdidos.repository.PersonalRepository;
import pe.edu.upeu.objetosperdidos.repository.ReporteHallazgosRepository;

@Service
public class RevisionHallazgoServiceImpl implements IRevisionHallazgoService {

    private final ReporteHallazgosRepository hallazgos;
    private final PersonalRepository personal;

    public RevisionHallazgoServiceImpl(
            ReporteHallazgosRepository hallazgos,
            PersonalRepository personal) {
        this.hallazgos = hallazgos;
        this.personal = personal;
    }

    @Override
    @Transactional(readOnly = true)
    public List<HallazgoPendienteRevisionDTO> pendientes() {
        return hallazgos.findByEstadoOrderByFechaReporteAscIdHallazgoAsc(
                EstadoReporteHallazgo.PENDIENTE)
                .stream()
                .map(this::aPendiente)
                .toList();
    }

    @Override
    @Transactional
    public HallazgoRevisionResultadoDTO resolver(
            Long idHallazgo,
            ResolverHallazgoRequest solicitud,
            Long idPersonaRevisor) {

        if (idHallazgo == null || idHallazgo <= 0) {
            throw new IllegalArgumentException("El ID de hallazgo debe ser positivo");
        }
        if (solicitud == null || solicitud.decision() == null) {
            throw new IllegalArgumentException("Debes indicar APROBADO o RECHAZADO");
        }

        String observacion = solicitud.observacion() == null
                ? null : solicitud.observacion().strip();
        if (observacion != null && observacion.isEmpty()) {
            observacion = null;
        }
        if (observacion != null && observacion.length() > 255) {
            throw new IllegalArgumentException("La observacion supera los 255 caracteres");
        }
        if (solicitud.decision() == DecisionHallazgo.RECHAZADO
                && observacion == null) {
            throw new IllegalArgumentException(
                    "Debes indicar una observacion al rechazar un hallazgo");
        }

        // ID obtenido del JWT por el controlador, nunca del cuerpo de la peticion.
        Personal revisor = personal.findById(idPersonaRevisor)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "El usuario revisor no posee un registro PERSONAL"));
        if (revisor.getEstado() != EstadoPersonal.ACTIVO) {
            throw new ResourceInUseException(
                    "El revisor no tiene una cuenta de personal activa");
        }

        ReporteHallazgos hallazgo = hallazgos.bloquearParaRevision(idHallazgo)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Hallazgo no encontrado con ID: " + idHallazgo));

        if (hallazgo.getEstado() != EstadoReporteHallazgo.PENDIENTE) {
            throw new ResourceInUseException(
                    "El hallazgo ya fue revisado; estado actual: "
                            + hallazgo.getEstado());
        }

        EstadoReporteHallazgo nuevoEstado = solicitud.decision()
                == DecisionHallazgo.APROBADO
                ? EstadoReporteHallazgo.APROBADO
                : EstadoReporteHallazgo.RECHAZADO;

        hallazgo.setEstado(nuevoEstado);
        hallazgo.setPersonaRevisor(revisor);
        hallazgo.setFechaRevision(LocalDateTime.now(
                ZoneId.of("America/Lima")).truncatedTo(ChronoUnit.SECONDS));
        hallazgo.setObservacion(observacion);
        hallazgos.saveAndFlush(hallazgo);

        // La aprobacion del reporte NO crea un OBJETOS_CUSTODIADOS.
        return new HallazgoRevisionResultadoDTO(
                hallazgo.getIdHallazgo(),
                hallazgo.getEstado().name(),
                revisor.getIdPersona(),
                hallazgo.getFechaRevision(),
                hallazgo.getObservacion());
    }

    private HallazgoPendienteRevisionDTO aPendiente(ReporteHallazgos h) {
        Objeto objeto = h.getObjeto();
        return new HallazgoPendienteRevisionDTO(
                h.getIdHallazgo(),
                objeto.getIdObjeto(),
                h.getPersona().getIdPersona(),
                objeto.getNombre(),
                objeto.getCategoria().getNombre(),
                objeto.getMarca(),
                objeto.getColor(),
                objeto.getDescripcion(),
                h.getUbicacionEncontrado().getNombre(),
                h.getUbicacionDestino().getNombre(),
                h.getFechaReporte(),
                h.getEstado().name(),
                h.getImagenRef());
    }
}
