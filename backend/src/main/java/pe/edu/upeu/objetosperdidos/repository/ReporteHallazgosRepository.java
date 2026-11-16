package pe.edu.upeu.objetosperdidos.repository;


import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import pe.edu.upeu.objetosperdidos.entity.EstadoReporteHallazgo;
import pe.edu.upeu.objetosperdidos.entity.ReporteHallazgos;
import java.util.List;

@Repository
public interface ReporteHallazgosRepository extends JpaRepository<ReporteHallazgos, Long> {
    List<ReporteHallazgos> findByPersonaIdPersona(Long idPersona);
    List<ReporteHallazgos> findByEstado(EstadoReporteHallazgo estado);
    List<ReporteHallazgos> findByUbicacionDestinoIdUbicacionAndEstado(Long idUbicacion, EstadoReporteHallazgo estado);
    long countByPersonaIdPersona(Long idPersona);
    long countByPersonaIdPersonaAndEstado(Long idPersona, EstadoReporteHallazgo estado);

    @Query(value = """
        SELECT H.ESTADO, COUNT(*)
          FROM REPORTE_HALLAZGOS H
         GROUP BY H.ESTADO
         ORDER BY COUNT(*) DESC, H.ESTADO
        """, nativeQuery = true)
    List<Object[]> reporteHallazgosPorEstado();

    @Query(value = """
        SELECT H.ID_HALLAZGO, O.ID_OBJETO, O.NOMBRE, C.NOMBRE,
               H.ESTADO, H.FECHA_REPORTE, U.NOMBRE
          FROM REPORTE_HALLAZGOS H
          JOIN OBJETO O ON O.ID_OBJETO = H.ID_OBJETO
          JOIN CATEGORIA C ON C.ID_CATEGORIA = O.ID_CATEGORIA
          JOIN UBICACION U ON U.ID_UBICACION = H.ID_UBICACION_DESTINO
         WHERE H.ID_PERSONA = :idPersona
         ORDER BY H.FECHA_REPORTE DESC, H.ID_HALLAZGO DESC
        """, nativeQuery = true)
    List<Object[]> listarHallazgosDePersona(@Param("idPersona") Long idPersona);

    // Solo los aprobados que NO ingresaron a custodia. Los cancelados no aparecen.
    @Query(value = """
        SELECT H.ID_HALLAZGO, O.NOMBRE, U.NOMBRE, H.FECHA_REPORTE
          FROM REPORTE_HALLAZGOS H
          JOIN OBJETO O ON O.ID_OBJETO = H.ID_OBJETO
          JOIN UBICACION U ON U.ID_UBICACION = H.ID_UBICACION_DESTINO
         WHERE H.ESTADO = 'APROBADO'
           AND H.ID_UBICACION_DESTINO = :idUbicacion
           AND NOT EXISTS (
               SELECT 1 FROM OBJETOS_CUSTODIADOS OC
                WHERE OC.ID_HALLAZGO = H.ID_HALLAZGO
           )
         ORDER BY H.FECHA_REPORTE ASC
        """, nativeQuery = true)
    List<Object[]> listarPendientesRecepcion(@Param("idUbicacion") Long idUbicacion);
}
