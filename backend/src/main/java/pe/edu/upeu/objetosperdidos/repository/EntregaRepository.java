package pe.edu.upeu.objetosperdidos.repository;


import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import pe.edu.upeu.objetosperdidos.entity.Entrega;
import java.util.List;
import java.util.Optional;

@Repository
public interface EntregaRepository extends JpaRepository<Entrega, Long> {
    Optional<Entrega> findByCustodiaIdCustodia(Long idCustodia);
    Optional<Entrega> findByReportePerdidaIdReportePerdida(Long idReportePerdida);
    boolean existsByCustodiaIdCustodia(Long idCustodia);

    @Query(value = """
        SELECT TO_CHAR(E.FECHA_ENTREGA, 'YYYY-MM'), COUNT(*)
          FROM ENTREGA E
         GROUP BY TO_CHAR(E.FECHA_ENTREGA, 'YYYY-MM')
         ORDER BY TO_CHAR(E.FECHA_ENTREGA, 'YYYY-MM')
        """, nativeQuery = true)
    List<Object[]> reporteEntregasPorMes();
}
