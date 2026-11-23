package pe.edu.upeu.objetosperdidos.services;

import java.util.List;
import pe.edu.upeu.objetosperdidos.dto.CrearReportePerdidaRequest;
import pe.edu.upeu.objetosperdidos.dto.MiPerdidaDTO;
import pe.edu.upeu.objetosperdidos.dto.ReportePerdidaCreadoDTO;

public interface IReportePerdidaService {

    ReportePerdidaCreadoDTO crear(CrearReportePerdidaRequest solicitud, Long idPersona);

    List<MiPerdidaDTO> misPerdidas(Long idPersona);

    // NUEVO: el dueño confirma que ya recupero su objeto
    void marcarRecuperado(Long idReportePerdida, Long idPersona);

    // NUEVO: reportes en busqueda de una persona (lo usa el personal al entregar)
    List<MiPerdidaDTO> abiertasDePersona(Long idPersona);
}