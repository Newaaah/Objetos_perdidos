package pe.edu.upeu.objetosperdidos.repository;


import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import pe.edu.upeu.objetosperdidos.entity.EstadoReportePerdida;
import pe.edu.upeu.objetosperdidos.entity.ReportePerdida;
import java.util.List;

@Repository
public interface ReportePerdidaRepository extends JpaRepository<ReportePerdida, Long> {
    List<ReportePerdida> findByPersonaIdPersona(Long idPersona);
    List<ReportePerdida> findByEstado(EstadoReportePerdida estado);
    long countByPersonaIdPersona(Long idPersona);
    long countByPersonaIdPersonaAndEstado(Long idPersona, EstadoReportePerdida estado);

    // REPORTE 1: el estado pertenece a REPORTE_PERDIDA, no a OBJETO.
    @Query(value = """
        SELECT P.ESTADO, COUNT(*)
          FROM REPORTE_PERDIDA P
         GROUP BY P.ESTADO
         ORDER BY COUNT(*) DESC, P.ESTADO
        """, nativeQuery = true)
    List<Object[]> reportePerdidasPorEstado();

    @Query(value = """
        SELECT TO_CHAR(P.FECHA_REPORTE, 'YYYY-MM'), COUNT(*)
          FROM REPORTE_PERDIDA P
         GROUP BY TO_CHAR(P.FECHA_REPORTE, 'YYYY-MM')
         ORDER BY TO_CHAR(P.FECHA_REPORTE, 'YYYY-MM')
        """, nativeQuery = true)
    List<Object[]> reportePerdidasPorMes();

    // Historial propio: devuelve columnas simples, no entidades JPA con relaciones LAZY.
    @Query(value = """
        SELECT P.ID_REPORTE_PERDIDA, O.ID_OBJETO, O.NOMBRE, C.NOMBRE,
               P.ESTADO, P.FECHA_PERDIDA, P.FECHA_REPORTE, U.NOMBRE
          FROM REPORTE_PERDIDA P
          JOIN OBJETO O ON O.ID_OBJETO = P.ID_OBJETO
          JOIN CATEGORIA C ON C.ID_CATEGORIA = O.ID_CATEGORIA
          LEFT JOIN UBICACION U ON U.ID_UBICACION = P.ID_UBICACION_PERDIDA
         WHERE P.ID_PERSONA = :idPersona
         ORDER BY P.FECHA_REPORTE DESC, P.ID_REPORTE_PERDIDA DESC
        """, nativeQuery = true)
    List<Object[]> listarPerdidasDePersona(@Param("idPersona") Long idPersona);
}
