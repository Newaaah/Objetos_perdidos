
package pe.edu.upeu.objetosperdidos.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import pe.edu.upeu.objetosperdidos.entity.Entrega;

@Repository
public interface EntregaRepository
        extends JpaRepository<Entrega, Long> {

    // Historial privado para que quien recibio un objeto pueda reclamar su entrega.
    List<Entrega> findByPersonaIdPersonaOrderByFechaEntregaDescIdEntregaDesc(Long idPersona);

    /*
     * Buscar una entrega asociada a una custodia.
     */
    Optional<Entrega> findByCustodiaIdCustodia(
            Long idCustodia
    );

    /*
     * Buscar una entrega asociada a un reporte de perdida.
     */
    Optional<Entrega> findByReportePerdidaIdReportePerdida(
            Long idReportePerdida
    );

    /*
     * Verificar si una custodia ya tiene una entrega registrada.
     */
    boolean existsByCustodiaIdCustodia(
            Long idCustodia
    );

    /*
     * REPORTE ESTADISTICO:
     * Cantidad de entregas realizadas por mes.
     *
     * Se utiliza UPEU_DATOS porque APP_UPEU
     * accede a tablas de otro esquema.
     */
    @Query(value = """
        SELECT
            TO_CHAR(E.FECHA_ENTREGA, 'YYYY-MM') AS MES,
            COUNT(*) AS TOTAL
        FROM UPEU_DATOS.ENTREGA E
        GROUP BY
            TO_CHAR(E.FECHA_ENTREGA, 'YYYY-MM')
        ORDER BY
            MES
        """, nativeQuery = true)
    List<Object[]> reporteEntregasPorMes();

    /*
     * CATALOGO PUBLICO:
     * Entregas desde una fecha, para que un dueno pueda
     * reconocer su objeto y presentar un reclamo.
     */
    @Query("""
        SELECT e FROM Entrega e
        JOIN FETCH e.custodia c
        JOIN FETCH c.objeto o
        JOIN FETCH o.categoria cat
        WHERE e.fechaEntrega >= :desde
          AND (:idCategoria IS NULL OR cat.idCategoria = :idCategoria)
        ORDER BY e.fechaEntrega DESC, e.idEntrega DESC
        """)
    List<Entrega> listarEntregadasPublico(
            @Param("desde") java.time.LocalDateTime desde,
            @Param("idCategoria") Long idCategoria
    );
}
