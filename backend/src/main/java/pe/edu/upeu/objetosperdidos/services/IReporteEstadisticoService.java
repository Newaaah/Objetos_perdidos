package pe.edu.upeu.objetosperdidos.services;

import java.util.List;
import pe.edu.upeu.objetosperdidos.dto.*;

public interface IReporteEstadisticoService {
    List<ConteoEstadoDTO> perdidasPorEstado();
    List<ConteoEstadoDTO> perdidasPorUbicacion();   // NUEVO
    List<ConteoEstadoDTO> hallazgosPorEstado();
    List<ConteoEstadoDTO> custodiasPorSituacion();
    List<PorcentajeEntregasCategoriaDTO> entregasPorCategoria();
    List<ConteoMesDTO> perdidasPorMes();
    List<ConteoMesDTO> entregasPorMes();
    List<ConteoEstadoDTO> reclamosPorEstado();
    ResumenReclamosDTO resumenReclamosDesdeProcedimiento();
    List<SeguimientoReclamoDTO> seguimientoDesdeVista();
    List<PendienteRecepcionDTO> pendientesDeRecepcion(Long idUbicacion);
    List<CustodiaDisponibleDTO> custodiasDisponibles(Long idUbicacion);

    // Métodos privados por identidad: NO se exponen por ID en el controller hasta tener JWT.
    ResumenEstudianteDTO resumenEstudiante(Long idPersona);
    List<MiPerdidaDTO> misPerdidas(Long idPersona);
    List<MiHallazgoDTO> misHallazgos(Long idPersona);
}