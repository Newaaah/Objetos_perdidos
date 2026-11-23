package pe.edu.upeu.objetosperdidos.repository;

import jakarta.persistence.LockModeType;
import java.util.Optional;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import pe.edu.upeu.objetosperdidos.entity.EstadoReportePerdida;
import pe.edu.upeu.objetosperdidos.entity.ReportePerdida;

import java.util.List;

@Repository
public interface ReportePerdidaRepository
        extends JpaRepository<ReportePerdida, Long> {

    // Evita cambios concurrentes de estado durante la entrega fisica.
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT p FROM ReportePerdida p WHERE p.idReportePerdida = :idReportePerdida")
    Optional<ReportePerdida> bloquearParaEntrega(
            @Param("idReportePerdida") Long idReportePerdida);

    @Query("""
           SELECT p FROM ReportePerdida p
           JOIN FETCH p.objeto o
           JOIN FETCH o.categoria
           WHERE p.estado = :estado AND o.categoria.idCategoria = :categoria
           ORDER BY p.idReportePerdida DESC
           """)
    List<ReportePerdida> buscarActivasPorCategoria(
            @Param("estado") EstadoReportePerdida estado,
            @Param("categoria") Long categoria,
            Pageable pageable);

    List<ReportePerdida> findByPersonaIdPersona(Long idPersona);

    List<ReportePerdida> findByEstado(
            EstadoReportePerdida estado
    );

    long countByPersonaIdPersona(Long idPersona);

    long countByPersonaIdPersonaAndEstado(
            Long idPersona,
            EstadoReportePerdida estado
    );

    // REPORTE 1: cantidad de reportes de perdida por estado.
    @Query(value = """
            SELECT P.ESTADO, COUNT(*)
            FROM UPEU_DATOS.REPORTE_PERDIDA P
            GROUP BY P.ESTADO
            ORDER BY COUNT(*) DESC, P.ESTADO
            """, nativeQuery = true)
    List<Object[]> reportePerdidasPorEstado();

    // NUEVO
    // Cantidad de reportes de perdida por zona del campus.
    // Las aulas y SS.HH. se agrupan por pabellon; laboratorios, malocas,
    // estacionamientos y talleres se agrupan en una sola zona.
    // Los demas lugares (Auditorio, CRAI, Comedor...) salen con su nombre.
    @Query(value = """
            SELECT Z.ZONA, COUNT(*)
            FROM (
                SELECT
                    CASE
                        WHEN U.ID_UBICACION IS NULL THEN 'Sin indicar'
                        WHEN UPPER(U.NOMBRE) LIKE 'PABELLON %' THEN 'Pabellon ' || SUBSTR(U.NOMBRE, 10, 1)
                        WHEN UPPER(U.NOMBRE) LIKE 'LABORATORIO%' OR UPPER(U.NOMBRE) = 'REDES' THEN 'Laboratorios'
                        WHEN UPPER(U.NOMBRE) LIKE 'MALOCA%' THEN 'Malocas'
                        WHEN UPPER(U.NOMBRE) LIKE 'ESTACIONAMIENTO%' THEN 'Estacionamientos'
                        WHEN UPPER(U.NOMBRE) LIKE 'TALLER%' THEN 'Talleres'
                        ELSE U.NOMBRE
                    END AS ZONA
                FROM UPEU_DATOS.REPORTE_PERDIDA P
                LEFT JOIN UPEU_DATOS.UBICACION U
                    ON U.ID_UBICACION = P.ID_UBICACION_PERDIDA
            ) Z
            GROUP BY Z.ZONA
            ORDER BY COUNT(*) DESC, Z.ZONA
            """, nativeQuery = true)
    List<Object[]> reportePerdidasPorZona();

    // Cantidad de reportes de perdida por mes.
    @Query(value = """
            SELECT
                TO_CHAR(P.FECHA_REPORTE, 'YYYY-MM'),
                COUNT(*)
            FROM UPEU_DATOS.REPORTE_PERDIDA P
            GROUP BY TO_CHAR(P.FECHA_REPORTE, 'YYYY-MM')
            ORDER BY TO_CHAR(P.FECHA_REPORTE, 'YYYY-MM')
            """, nativeQuery = true)
    List<Object[]> reportePerdidasPorMes();

    // Historial de perdidas de una persona.
    // Devuelve columnas simples para evitar cargar
    // entidades JPA con relaciones LAZY.
    @Query(value = """
            SELECT
                P.ID_REPORTE_PERDIDA,
                O.ID_OBJETO,
                O.NOMBRE,
                C.NOMBRE,
                P.ESTADO,
                P.FECHA_PERDIDA,
                P.FECHA_REPORTE,
                U.NOMBRE
            FROM UPEU_DATOS.REPORTE_PERDIDA P
            JOIN UPEU_DATOS.OBJETO O
                ON O.ID_OBJETO = P.ID_OBJETO
            JOIN UPEU_DATOS.CATEGORIA C
                ON C.ID_CATEGORIA = O.ID_CATEGORIA
            LEFT JOIN UPEU_DATOS.UBICACION U
                ON U.ID_UBICACION = P.ID_UBICACION_PERDIDA
            WHERE P.ID_PERSONA = :idPersona
            ORDER BY
                P.FECHA_REPORTE DESC,
                P.ID_REPORTE_PERDIDA DESC
            """, nativeQuery = true)
    List<Object[]> listarPerdidasDePersona(
            @Param("idPersona") Long idPersona
    );
}