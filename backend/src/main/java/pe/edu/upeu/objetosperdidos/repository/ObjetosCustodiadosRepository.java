
package pe.edu.upeu.objetosperdidos.repository;

import java.util.List;
import java.util.Optional;

import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import pe.edu.upeu.objetosperdidos.entity.ObjetosCustodiados;

@Repository
public interface ObjetosCustodiadosRepository
        extends JpaRepository<ObjetosCustodiados, Long> {

    // Evita dos entregas concurrentes de una misma custodia.
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT c FROM ObjetosCustodiados c WHERE c.idCustodia = :idCustodia")
    Optional<ObjetosCustodiados> bloquearParaEntrega(
            @Param("idCustodia") Long idCustodia);

    // Buscar una custodia a partir de un reporte de hallazgo.
    Optional<ObjetosCustodiados> findByHallazgoIdHallazgo(
            Long idHallazgo
    );

    // Buscar custodias registradas en una ubicacion.
    List<ObjetosCustodiados> findByUbicacionActualIdUbicacion(
            Long idUbicacion
    );

    /*
     * REPORTE 1:
     * Cantidad de objetos que permanecen en custodia
     * y objetos que ya fueron entregados.
     *
     * OBJETOS_CUSTODIADOS no tiene columna ESTADO.
     * La situacion se determina por la existencia
     * de un registro en ENTREGA.
     */
    @Query(value = """
        SELECT
            CASE
                WHEN E.ID_ENTREGA IS NULL
                    THEN 'EN_CUSTODIA'
                ELSE 'ENTREGADO'
            END AS SITUACION,
            COUNT(*) AS TOTAL
        FROM UPEU_DATOS.OBJETOS_CUSTODIADOS OC
        LEFT JOIN UPEU_DATOS.ENTREGA E
            ON E.ID_CUSTODIA = OC.ID_CUSTODIA
        GROUP BY
            CASE
                WHEN E.ID_ENTREGA IS NULL
                    THEN 'EN_CUSTODIA'
                ELSE 'ENTREGADO'
            END
        ORDER BY SITUACION
        """, nativeQuery = true)
    List<Object[]> reporteCustodiasPorSituacion();

    /*
     * REPORTE 2:
     * Porcentaje de custodias entregadas por categoria.
     *
     * El denominador utiliza las custodias reales,
     * no todas las fichas de objetos.
     *
     * Tambien incluye categorias que todavia
     * no tienen custodias, con porcentaje cero.
     */
    @Query(value = """
        SELECT
            C.ID_CATEGORIA,
            C.NOMBRE,
            COUNT(OC.ID_CUSTODIA) AS TOTAL_CUSTODIAS,
            COUNT(E.ID_ENTREGA) AS TOTAL_ENTREGADAS,
            CASE
                WHEN COUNT(OC.ID_CUSTODIA) = 0
                    THEN 0
                ELSE ROUND(
                    COUNT(E.ID_ENTREGA) * 100.0
                    / COUNT(OC.ID_CUSTODIA),
                    2
                )
            END AS PORCENTAJE
        FROM UPEU_DATOS.CATEGORIA C
        LEFT JOIN UPEU_DATOS.OBJETO O
            ON O.ID_CATEGORIA = C.ID_CATEGORIA
        LEFT JOIN UPEU_DATOS.OBJETOS_CUSTODIADOS OC
            ON OC.ID_OBJETO = O.ID_OBJETO
        LEFT JOIN UPEU_DATOS.ENTREGA E
            ON E.ID_CUSTODIA = OC.ID_CUSTODIA
        GROUP BY
            C.ID_CATEGORIA,
            C.NOMBRE
        ORDER BY
            PORCENTAJE DESC,
            C.NOMBRE
        """, nativeQuery = true)
    List<Object[]> reportePorcentajeEntregasPorCategoria();

    /*
     * CONSULTA OPERATIVA:
     * Listar objetos disponibles en custodia
     * dentro de una ubicacion determinada.
     *
     * Solo devuelve custodias que todavia
     * no tienen una entrega registrada.
     */
    @Query(value = """
        SELECT
            OC.ID_CUSTODIA,
            O.NOMBRE,
            C.NOMBRE,
            U.NOMBRE,
            OC.FECHA_REGISTRO
        FROM UPEU_DATOS.OBJETOS_CUSTODIADOS OC
        JOIN UPEU_DATOS.OBJETO O
            ON O.ID_OBJETO = OC.ID_OBJETO
        JOIN UPEU_DATOS.CATEGORIA C
            ON C.ID_CATEGORIA = O.ID_CATEGORIA
        JOIN UPEU_DATOS.UBICACION U
            ON U.ID_UBICACION = OC.ID_UBICACION_ACTUAL
        WHERE OC.ID_UBICACION_ACTUAL = :idUbicacion
          AND NOT EXISTS (
              SELECT 1
              FROM UPEU_DATOS.ENTREGA E
              WHERE E.ID_CUSTODIA = OC.ID_CUSTODIA
          )
        ORDER BY
            OC.FECHA_REGISTRO DESC
        """, nativeQuery = true)
    List<Object[]> listarCustodiasDisponiblesEnUbicacion(
            @Param("idUbicacion") Long idUbicacion
    );

    /*
     * CATALOGO PUBLICO:
     * Custodias que todavia no tienen entrega registrada.
     * Filtro de categoria opcional (null = todas).
     */
    @Query("""
        SELECT c FROM ObjetosCustodiados c
        JOIN FETCH c.objeto o
        JOIN FETCH o.categoria cat
        LEFT JOIN FETCH c.ubicacionActual u
        WHERE NOT EXISTS (
            SELECT 1 FROM Entrega e WHERE e.custodia = c
        )
          AND (:idCategoria IS NULL OR cat.idCategoria = :idCategoria)
        ORDER BY c.fechaRegistro DESC, c.idCustodia DESC
        """)
    List<ObjetosCustodiados> listarDisponiblesPublico(
            @Param("idCategoria") Long idCategoria
    );
}
