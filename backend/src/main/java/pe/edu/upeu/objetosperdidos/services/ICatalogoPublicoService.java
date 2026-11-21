package pe.edu.upeu.objetosperdidos.services;

import java.util.List;
import pe.edu.upeu.objetosperdidos.dto.ObjetoEntregadoPublicoDTO;
import pe.edu.upeu.objetosperdidos.dto.ObjetoPublicoDTO;

public interface ICatalogoPublicoService {

    List<ObjetoPublicoDTO> disponibles(Long idCategoria);

    List<ObjetoEntregadoPublicoDTO> entregados(Long idCategoria, Integer dias);

    IFileStorageService.Imagen imagen(Long idCustodia);
}
