package pe.edu.upeu.objetosperdidos.repository;


import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import pe.edu.upeu.objetosperdidos.entity.ObjetosCustodiados;
import java.util.List;
import java.util.Optional;

@Repository
public interface ObjetosCustodiadosRepository extends JpaRepository<ObjetosCustodiados, Long> {
    Optional<ObjetosCustodiados> findByHallazgoIdHallazgo(Long idHallazgo);
    List<ObjetosCustodiados> findByUbicacionActualIdUbicacion(Long idUbicacion);

    // No existe ESTADO en OBJETOS_CUSTODIADOS. Se deduce por la ENTREGA.
    @Query(value = """
        SELECT CASE WHEN E.ID_ENTREGA IS NULL THEN 'EN_CUSTODIA'
                    ELSE 'ENTREGADO' END AS SITUACION,
               COUNT(*) AS TOTAL
          FROM OBJETOS_CUSTODIADOS OC
          LEFT JOIN ENTREGA E ON E.ID_CUSTODIA = OC.ID_CUSTODIA
         GROUP BY CASE WHEN E.ID_ENTREGA IS NULL THEN 'EN_CUSTODIA'
                       ELSE 'ENTREGADO' END
         ORDER BY SITUACION
        """, nativeQuery = true)
    List<Object[]> reporteCustodiasPorSituacion();

    // REPORTE 2: denominador = custodias reales, no fichas OBJETO.
    // Incluye categorías que aún no han tenido custodias: porcentaje 0.
    @Query(value = """
        SELECT C.ID_CATEGORIA, C.NOMBRE,
               COUNT(OC.ID_CUSTODIA) AS TOTAL_CUSTODIAS,
               COUNT(E.ID_ENTREGA) AS TOTAL_ENTREGADAS,
               CASE WHEN COUNT(OC.ID_CUSTODIA) = 0 THEN 0
                    ELSE ROUND(COUNT(E.ID_ENTREGA) * 100.0 / COUNT(OC.ID_CUSTODIA), 2)
               END AS PORCENTAJE
          FROM CATEGORIA C
          LEFT JOIN OBJETO O ON O.ID_CATEGORIA = C.ID_CATEGORIA
          LEFT JOIN OBJETOS_CUSTODIADOS OC ON OC.ID_OBJETO = O.ID_OBJETO
          LEFT JOIN ENTREGA E ON E.ID_CUSTODIA = OC.ID_CUSTODIA
         GROUP BY C.ID_CATEGORIA, C.NOMBRE
         ORDER BY PORCENTAJE DESC, C.NOMBRE
        """, nativeQuery = true)
    List<Object[]> reportePorcentajeEntregasPorCategoria();

    @Query(value = """
        SELECT OC.ID_CUSTODIA, O.NOMBRE, C.NOMBRE, U.NOMBRE, OC.FECHA_REGISTRO
          FROM OBJETOS_CUSTODIADOS OC
          JOIN OBJETO O ON O.ID_OBJETO = OC.ID_OBJETO
          JOIN CATEGORIA C ON C.ID_CATEGORIA = O.ID_CATEGORIA
          JOIN UBICACION U ON U.ID_UBICACION = OC.ID_UBICACION_ACTUAL
         WHERE OC.ID_UBICACION_ACTUAL = :idUbicacion
           AND NOT EXISTS (
               SELECT 1 FROM ENTREGA E WHERE E.ID_CUSTODIA = OC.ID_CUSTODIA
           )
         ORDER BY OC.FECHA_REGISTRO DESC
        """, nativeQuery = true)
    List<Object[]> listarCustodiasDisponiblesEnUbicacion(@Param("idUbicacion") Long idUbicacion);
}
