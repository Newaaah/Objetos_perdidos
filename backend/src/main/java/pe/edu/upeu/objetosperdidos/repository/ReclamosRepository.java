
package pe.edu.upeu.objetosperdidos.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import pe.edu.upeu.objetosperdidos.entity.EstadoReclamo;
import pe.edu.upeu.objetosperdidos.entity.Reclamos;
import pe.edu.upeu.objetosperdidos.entity.TipoReclamo;

@Repository
public interface ReclamosRepository
        extends JpaRepository<Reclamos, Long> {

    List<Reclamos> findByPersonaReclamaIdPersona(
            Long idPersona
    );

    List<Reclamos> findByCustodiaIdCustodia(
            Long idCustodia
    );

    List<Reclamos> findByEstado(
            EstadoReclamo estado
    );

    long countByPersonaReclamaIdPersonaAndTipoReclamo(
            Long idPersona,
            TipoReclamo tipoReclamo
    );

    /*
     * REPORTE ESTADISTICO:
     * Cantidad de reclamos por estado.
     * No incluye incidencias.
     */
    @Query(value = """
        SELECT
            R.ESTADO,
            COUNT(*) AS TOTAL
        FROM UPEU_DATOS.RECLAMOS R
        WHERE R.TIPO_RECLAMO = 'RECLAMO'
        GROUP BY R.ESTADO
        ORDER BY COUNT(*) DESC, R.ESTADO
        """, nativeQuery = true)
    List<Object[]> reporteReclamosPorEstado();

    /*
     * VISTA ORACLE:
     * Seguimiento de reclamos.
     *
     * Propietario verificado: UPEU_DATOS.
     */
    @Query(value = """
        SELECT
            ID_RECLAMACION,
            ID_OBJETO,
            CATEGORIA,
            DESCRIPCION,
            ESTADO_RECLAMACION,
            FECHA_SOLICITUD,
            FECHA_RESOLUCION,
            SITUACION_REVISION
        FROM UPEU_DATOS.VW_SEGUIMIENTO_RECLAMOS
        ORDER BY
            FECHA_SOLICITUD DESC,
            ID_RECLAMACION DESC
        """, nativeQuery = true)
    List<Object[]> consultarVistaSeguimientoReclamos();
}
