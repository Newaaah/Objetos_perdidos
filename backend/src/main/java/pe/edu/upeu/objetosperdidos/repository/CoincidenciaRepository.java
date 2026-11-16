package pe.edu.upeu.objetosperdidos.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import pe.edu.upeu.objetosperdidos.entity.Coincidencia;
import java.util.List;
import java.util.Optional;
import pe.edu.upeu.objetosperdidos.entity.EstadoCoincidencia;

@Repository
public interface CoincidenciaRepository extends JpaRepository<Coincidencia, Long> {
    List<Coincidencia> findByReportePerdidaIdReportePerdida(Long idReportePerdida);
    List<Coincidencia> findByHallazgoIdHallazgo(Long idHallazgo);
    Optional<Coincidencia> findByReportePerdidaIdReportePerdidaAndHallazgoIdHallazgo(Long idReportePerdida, Long idHallazgo);
    boolean existsByHallazgoIdHallazgoAndEstado(Long idHallazgo, EstadoCoincidencia estado);
    boolean existsByReportePerdidaIdReportePerdidaAndEstado(Long idReportePerdida, EstadoCoincidencia estado);
}
