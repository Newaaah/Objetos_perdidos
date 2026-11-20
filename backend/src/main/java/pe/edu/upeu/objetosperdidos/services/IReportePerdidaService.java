package pe.edu.upeu.objetosperdidos.services;

import java.util.List;
import pe.edu.upeu.objetosperdidos.dto.CrearReportePerdidaRequest;
import pe.edu.upeu.objetosperdidos.dto.MiPerdidaDTO;
import pe.edu.upeu.objetosperdidos.dto.ReportePerdidaCreadoDTO;

public interface IReportePerdidaService {

    ReportePerdidaCreadoDTO crear(CrearReportePerdidaRequest solicitud, Long idPersona);

    List<MiPerdidaDTO> misPerdidas(Long idPersona);
}
