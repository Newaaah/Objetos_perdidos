package pe.edu.upeu.objetosperdidos.repository;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Repository;
import pe.edu.upeu.objetosperdidos.entity.EstadoReporteHallazgo;
import pe.edu.upeu.objetosperdidos.entity.ObjetosCustodiados;
import pe.edu.upeu.objetosperdidos.entity.ReporteHallazgos;
import pe.edu.upeu.objetosperdidos.entity.ReportePerdida;

/**
 * Solo lecturas para encontrar candidatos a coincidencia.
 * No escribe en COINCIDENCIA ni crea decisiones de personal.
 * Consultas orientadas a conjuntos pequeños/medianos; medir rendimiento
 * antes de operar con grandes volúmenes de registros.
 */
@Repository
public class CandidatosCoincidenciaRepository {

    @PersistenceContext
    private EntityManager em;

    public Optional<ReportePerdida> buscarPerdidaPropia(Long idReporte, Long idPersona) {
        return em.createQuery("""
                SELECT p FROM ReportePerdida p
                JOIN FETCH p.objeto o
                JOIN FETCH o.categoria
                LEFT JOIN FETCH p.ubicacionPerdida
                WHERE p.idReportePerdida = :idReporte
                  AND p.persona.idPersona = :idPersona
                """, ReportePerdida.class)
                .setParameter("idReporte", idReporte)
                .setParameter("idPersona", idPersona)
                .getResultStream()
                .findFirst();
    }

    /** Hallazgos aprobados que aún no ingresaron como custodia. */
    public List<ReporteHallazgos> hallazgosSinCustodia() {
        return em.createQuery("""
                SELECT h FROM ReporteHallazgos h
                JOIN FETCH h.objeto o
                JOIN FETCH o.categoria
                JOIN FETCH h.ubicacionEncontrado
                WHERE h.estado = :aprobado
                  AND NOT EXISTS (
                      SELECT c.idCustodia FROM ObjetosCustodiados c
                      WHERE c.hallazgo = h
                  )
                ORDER BY h.idHallazgo DESC
                """, ReporteHallazgos.class)
                .setParameter("aprobado", EstadoReporteHallazgo.APROBADO)
                .getResultList();
    }

    /**
     * Custodias todavía sin entrega. Una custodia vinculada a un hallazgo
     * se considera solo si ese hallazgo está aprobado.
     * Incluye custodias directas (hallazgo = NULL).
     */
    public List<ObjetosCustodiados> custodiasDisponibles() {
        return em.createQuery("""
                SELECT c FROM ObjetosCustodiados c
                JOIN FETCH c.objeto o
                JOIN FETCH o.categoria
                LEFT JOIN FETCH c.hallazgo h
                LEFT JOIN FETCH c.ubicacionEncontrado
                LEFT JOIN FETCH h.ubicacionEncontrado
                WHERE NOT EXISTS (
                    SELECT e.idEntrega FROM Entrega e WHERE e.custodia = c
                )
                  AND (c.hallazgo IS NULL OR h.estado = :aprobado)
                ORDER BY c.idCustodia DESC
                """, ObjetosCustodiados.class)
                .setParameter("aprobado", EstadoReporteHallazgo.APROBADO)
                .getResultList();
    }
}
