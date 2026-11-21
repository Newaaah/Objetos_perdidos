package pe.edu.upeu.objetosperdidos.services;

import jakarta.persistence.EntityManager;
import jakarta.persistence.LockModeType;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pe.edu.upeu.objetosperdidos.dto.CoincidenciaRevisadaDTO;
import pe.edu.upeu.objetosperdidos.dto.EvaluacionCoincidenciaDTO;
import pe.edu.upeu.objetosperdidos.dto.RevisarCoincidenciaRequest;
import pe.edu.upeu.objetosperdidos.entity.Coincidencia;
import pe.edu.upeu.objetosperdidos.entity.EstadoCoincidencia;
import pe.edu.upeu.objetosperdidos.entity.EstadoPersonal;
import pe.edu.upeu.objetosperdidos.entity.EstadoReporteHallazgo;
import pe.edu.upeu.objetosperdidos.entity.EstadoReportePerdida;
import pe.edu.upeu.objetosperdidos.entity.Objeto;
import pe.edu.upeu.objetosperdidos.entity.Personal;
import pe.edu.upeu.objetosperdidos.entity.ReporteHallazgos;
import pe.edu.upeu.objetosperdidos.entity.ReportePerdida;
import pe.edu.upeu.objetosperdidos.exception.ResourceInUseException;
import pe.edu.upeu.objetosperdidos.exception.ResourceNotFoundException;
import pe.edu.upeu.objetosperdidos.repository.CoincidenciaRepository;
import pe.edu.upeu.objetosperdidos.repository.PersonalRepository;

/** Registra la revision HUMANA. No crea custodias ni entregas ni cambia el estado de la perdida. */
@Service
public class RevisionCoincidenciaServiceImpl implements IRevisionCoincidenciaService {

    private static final String AVISO = "El puntaje es heuristico y no prueba propiedad; "
            + "confirmar una coincidencia no autoriza una entrega fisica.";

    private final CoincidenciaRepository coincidencias;
    private final PersonalRepository personal;
    private final EntityManager em;
    private final ComparadorObjetosService comparador;
    private final AvisosDeDominioService avisos;

    public RevisionCoincidenciaServiceImpl(
            CoincidenciaRepository coincidencias,
            PersonalRepository personal,
            EntityManager em,
            ComparadorObjetosService comparador,
            AvisosDeDominioService avisos) {
        this.coincidencias = coincidencias;
        this.personal = personal;
        this.em = em;
        this.comparador = comparador;
        this.avisos = avisos;
    }

    @Override
    @Transactional(readOnly = true)
    public EvaluacionCoincidenciaDTO evaluar(Long idReportePerdida, Long idHallazgo) {
        validarId(idReportePerdida, "reporte de perdida");
        validarId(idHallazgo, "hallazgo");
        ReportePerdida perdida = buscarPerdida(idReportePerdida, false);
        ReporteHallazgos hallazgo = buscarHallazgo(idHallazgo, false);
        var calculo = comparador.comparar(perdida, hallazgo);
        Optional<Coincidencia> anterior = coincidencias
                .findByReportePerdidaIdReportePerdidaAndHallazgoIdHallazgo(
                        idReportePerdida, idHallazgo);
        Objeto op = perdida.getObjeto();
        Objeto oh = hallazgo.getObjeto();
        return new EvaluacionCoincidenciaDTO(
                idReportePerdida, idHallazgo,
                perdida.getEstado().name(), hallazgo.getEstado().name(),
                op.getNombre(), op.getMarca(), op.getColor(), op.getDescripcion(),
                oh.getNombre(), oh.getMarca(), oh.getColor(), oh.getDescripcion(),
                calculo.similitud(), calculo.cobertura(), calculo.prioridad(),
                tieneEntrega(idHallazgo),
                anterior.map(c -> c.getEstado().name()).orElse(null), AVISO);
    }

    @Override
    @Transactional
    public CoincidenciaRevisadaDTO registrar(RevisarCoincidenciaRequest datos, Long idPersonaRevisor) {
        if (datos == null || datos.decision() == null) {
            throw new IllegalArgumentException("Debes indicar CONFIRMADA o DESCARTADA");
        }
        validarId(datos.idReportePerdida(), "reporte de perdida");
        validarId(datos.idHallazgo(), "hallazgo");
        validarId(idPersonaRevisor, "personal revisor");
        String observacion = datos.observacion() == null ? "" : datos.observacion().strip();
        if (observacion.isBlank() || observacion.length() > 255) {
            throw new IllegalArgumentException("La observacion es obligatoria (1 a 255 caracteres)");
        }
        Personal revisor = personal.findById(idPersonaRevisor)
                .orElseThrow(() -> new ResourceNotFoundException("No existe personal asociado a la sesion"));
        if (revisor.getEstado() != EstadoPersonal.ACTIVO) {
            throw new ResourceInUseException("El personal revisor no esta activo");
        }

        // Primero se bloquea la perdida y luego el hallazgo: orden estable entre solicitudes.
        // Esto serializa revisiones que comparten uno de estos dos registros.
        ReportePerdida perdida = buscarPerdida(datos.idReportePerdida(), true);
        // Una perdida no activa se rechaza antes de consultar o bloquear un hallazgo.
        if (perdida.getEstado() != EstadoReportePerdida.REPORTADO) {
            throw new ResourceInUseException("Solo se puede revisar una perdida REPORTADO");
        }

        ReporteHallazgos hallazgo = buscarHallazgo(datos.idHallazgo(), true);
        if (hallazgo.getEstado() != EstadoReporteHallazgo.APROBADO) {
            throw new ResourceInUseException("Solo se puede revisar un hallazgo APROBADO");
        }
        if (coincidencias.findByReportePerdidaIdReportePerdidaAndHallazgoIdHallazgo(
                datos.idReportePerdida(), datos.idHallazgo()).isPresent()) {
            throw new ResourceInUseException("Esta pareja perdida-hallazgo ya fue revisada");
        }
        if (tieneEntrega(datos.idHallazgo())) {
            throw new ResourceInUseException("El objeto del hallazgo ya fue entregado");
        }
        if (datos.decision() == EstadoCoincidencia.CONFIRMADA) {
            if (coincidencias.existsByHallazgoIdHallazgoAndEstado(
                    datos.idHallazgo(), EstadoCoincidencia.CONFIRMADA)) {
                throw new ResourceInUseException("Este hallazgo ya tiene una coincidencia CONFIRMADA");
            }
            if (coincidencias.existsByReportePerdidaIdReportePerdidaAndEstado(
                    datos.idReportePerdida(), EstadoCoincidencia.CONFIRMADA)) {
                throw new ResourceInUseException("Este reporte de perdida ya tiene una coincidencia CONFIRMADA");
            }
        }

        Coincidencia coincidencia = new Coincidencia();
        coincidencia.setReportePerdida(perdida);
        coincidencia.setHallazgo(hallazgo);
        coincidencia.setEstado(datos.decision());
        coincidencia.setObservacion(observacion);
        coincidencia.setPersonalRevisor(revisor);
        // PUNTAJE almacena similitud heuristica (0-100), no probabilidad ni certeza.
        coincidencia.setPuntaje(comparador.comparar(perdida, hallazgo).similitud());
        Coincidencia guardada = coincidencias.saveAndFlush(coincidencia);
        avisos.coincidenciaConfirmada(guardada);
        return convertir(guardada);
    }

    @Override
    @Transactional(readOnly = true)
    public CoincidenciaRevisadaDTO consultar(Long idCoincidencia) {
        validarId(idCoincidencia, "coincidencia");
        Coincidencia c = coincidencias.findById(idCoincidencia)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Coincidencia no encontrada con ID: " + idCoincidencia));
        return convertir(c);
    }

    @Override
    @Transactional(readOnly = true)
    public List<CoincidenciaRevisadaDTO> porReporte(Long idReportePerdida) {
        validarId(idReportePerdida, "reporte de perdida");
        buscarPerdida(idReportePerdida, false);
        return coincidencias.findByReportePerdidaIdReportePerdida(idReportePerdida)
                .stream()
                .sorted(Comparator.comparing(Coincidencia::getIdCoincidencia))
                .map(RevisionCoincidenciaServiceImpl::convertir)
                .toList();
    }

    private ReportePerdida buscarPerdida(Long id, boolean bloquear) {
        ReportePerdida p = bloquear
                ? em.find(ReportePerdida.class, id, LockModeType.PESSIMISTIC_WRITE)
                : em.find(ReportePerdida.class, id);
        if (p == null) throw new ResourceNotFoundException("Reporte de perdida no encontrado: " + id);
        return p;
    }

    private ReporteHallazgos buscarHallazgo(Long id, boolean bloquear) {
        ReporteHallazgos h = bloquear
                ? em.find(ReporteHallazgos.class, id, LockModeType.PESSIMISTIC_WRITE)
                : em.find(ReporteHallazgos.class, id);
        if (h == null) throw new ResourceNotFoundException("Hallazgo no encontrado: " + id);
        return h;
    }

    private boolean tieneEntrega(Long idHallazgo) {
        Long total = em.createQuery("""
                SELECT COUNT(e) FROM Entrega e
                JOIN e.custodia c
                WHERE c.hallazgo.idHallazgo = :idHallazgo
                """, Long.class)
                .setParameter("idHallazgo", idHallazgo)
                .getSingleResult();
        return total != null && total > 0L;
    }

    private static CoincidenciaRevisadaDTO convertir(Coincidencia c) {
        return new CoincidenciaRevisadaDTO(
                c.getIdCoincidencia(),
                c.getReportePerdida().getIdReportePerdida(),
                c.getHallazgo().getIdHallazgo(),
                c.getEstado().name(),
                c.getPuntaje(),
                c.getPersonalRevisor().getIdPersona(),
                c.getFechaRevision(),
                c.getObservacion());
    }

    private static void validarId(Long id, String nombre) {
        if (id == null || id <= 0) {
            throw new IllegalArgumentException("El ID de " + nombre + " debe ser positivo");
        }
    }
}
