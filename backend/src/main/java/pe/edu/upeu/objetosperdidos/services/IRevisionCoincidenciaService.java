package pe.edu.upeu.objetosperdidos.services;

import java.util.List;
import pe.edu.upeu.objetosperdidos.dto.CoincidenciaRevisadaDTO;
import pe.edu.upeu.objetosperdidos.dto.EvaluacionCoincidenciaDTO;
import pe.edu.upeu.objetosperdidos.dto.RevisarCoincidenciaRequest;

public interface IRevisionCoincidenciaService {
    EvaluacionCoincidenciaDTO evaluar(Long idReportePerdida, Long idHallazgo);
    CoincidenciaRevisadaDTO registrar(RevisarCoincidenciaRequest datos, Long idPersonaRevisor);
    CoincidenciaRevisadaDTO consultar(Long idCoincidencia);
    List<CoincidenciaRevisadaDTO> porReporte(Long idReportePerdida);
}
