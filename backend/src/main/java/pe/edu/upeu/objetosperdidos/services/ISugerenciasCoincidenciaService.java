package pe.edu.upeu.objetosperdidos.services;

import pe.edu.upeu.objetosperdidos.dto.ResultadoSugerenciasDTO;

/** Posibles coincidencias para un reporte de perdida del propio estudiante. */
public interface ISugerenciasCoincidenciaService {

    ResultadoSugerenciasDTO sugerir(Long idReporte, Long idPersona);
}
