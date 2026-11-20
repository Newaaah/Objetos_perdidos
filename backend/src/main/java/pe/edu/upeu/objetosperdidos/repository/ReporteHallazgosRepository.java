package pe.edu.upeu.objetosperdidos.repository;

import jakarta.persistence.LockModeType;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import pe.edu.upeu.objetosperdidos.entity.EstadoReporteHallazgo;
import pe.edu.upeu.objetosperdidos.entity.ReporteHallazgos;

@Repository
public interface ReporteHallazgosRepository
        extends JpaRepository<ReporteHallazgos, Long> {

    List<ReporteHallazgos> findByPersonaIdPersona(Long idPersona);

    List<ReporteHallazgos> findByEstado(EstadoReporteHallazgo estado);

    List<ReporteHallazgos> findByUbicacionDestinoIdUbicacionAndEstado(
            Long idUbicacion, EstadoReporteHallazgo estado);

    long countByPersonaIdPersona(Long idPersona);

    long countByPersonaIdPersonaAndEstado(
            Long idPersona, EstadoReporteHallazgo estado);

    // Carga las relaciones necesarias para la bandeja de revision.
    @EntityGraph(attributePaths = {
            "persona", "objeto", "objeto.categoria",
            "ubicacionEncontrado", "ubicacionDestino"
    })
    List<ReporteHallazgos> findByEstadoOrderByFechaReporteAscIdHallazgoAsc(
            EstadoReporteHallazgo estado);

    // Bloqueo para evitar que dos revisores decidan simultaneamente.
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT h FROM ReporteHallazgos h WHERE h.idHallazgo = :id")
    Optional<ReporteHallazgos> bloquearParaRevision(@Param("id") Long id);

    // Se usa el mismo tipo de bloqueo de la revision para serializar
    // recepciones del mismo hallazgo y prevenir registros duplicados.
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT h FROM ReporteHallazgos h WHERE h.idHallazgo = :id")
    Optional<ReporteHallazgos> bloquearParaRecepcion(@Param("id") Long id);

    // Excluye los hallazgos aprobados que ya tienen una custodia.
    @Query("""
        SELECT h FROM ReporteHallazgos h
        JOIN FETCH h.objeto
        JOIN FETCH h.ubicacionDestino
        WHERE h.estado = :estado
          AND NOT EXISTS (
              SELECT c.idCustodia FROM ObjetosCustodiados c
              WHERE c.hallazgo = h
          )
        ORDER BY h.fechaReporte ASC, h.idHallazgo ASC
        """)
    List<ReporteHallazgos> buscarSinCustodiaPorEstado(
            @Param("estado") EstadoReporteHallazgo estado);

    @Query(value = """
        SELECT H.ESTADO, COUNT(*)
          FROM UPEU_DATOS.REPORTE_HALLAZGOS H
         GROUP BY H.ESTADO
         ORDER BY COUNT(*) DESC, H.ESTADO
        """, nativeQuery = true)
    List<Object[]> reporteHallazgosPorEstado();

    @Query(value = """
        SELECT H.ID_HALLAZGO, O.ID_OBJETO, O.NOMBRE,
               C.NOMBRE, H.ESTADO, H.FECHA_REPORTE, U.NOMBRE
          FROM UPEU_DATOS.REPORTE_HALLAZGOS H
          JOIN UPEU_DATOS.OBJETO O ON O.ID_OBJETO = H.ID_OBJETO
          JOIN UPEU_DATOS.CATEGORIA C ON C.ID_CATEGORIA = O.ID_CATEGORIA
          JOIN UPEU_DATOS.UBICACION U
            ON U.ID_UBICACION = H.ID_UBICACION_DESTINO
         WHERE H.ID_PERSONA = :idPersona
         ORDER BY H.FECHA_REPORTE DESC, H.ID_HALLAZGO DESC
        """, nativeQuery = true)
    List<Object[]> listarHallazgosDePersona(@Param("idPersona") Long idPersona);

    // Un hallazgo aprobado aun no equivale a una custodia recibida.
    @Query(value = """
        SELECT H.ID_HALLAZGO, O.NOMBRE, U.NOMBRE, H.FECHA_REPORTE
          FROM UPEU_DATOS.REPORTE_HALLAZGOS H
          JOIN UPEU_DATOS.OBJETO O ON O.ID_OBJETO = H.ID_OBJETO
          JOIN UPEU_DATOS.UBICACION U
            ON U.ID_UBICACION = H.ID_UBICACION_DESTINO
         WHERE H.ESTADO = 'APROBADO'
           AND H.ID_UBICACION_DESTINO = :idUbicacion
           AND NOT EXISTS (
               SELECT 1 FROM UPEU_DATOS.OBJETOS_CUSTODIADOS OC
                WHERE OC.ID_HALLAZGO = H.ID_HALLAZGO
           )
         ORDER BY H.FECHA_REPORTE ASC
        """, nativeQuery = true)
    List<Object[]> listarPendientesRecepcion(@Param("idUbicacion") Long idUbicacion);
}
