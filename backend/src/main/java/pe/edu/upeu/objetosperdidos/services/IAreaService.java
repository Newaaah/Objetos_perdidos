package pe.edu.upeu.objetosperdidos.services;

import pe.edu.upeu.objetosperdidos.dto.AreaDTO;

import java.util.List;
import java.util.Optional;

public interface IAreaService {

    List<AreaDTO> listarTodo();

    Optional<AreaDTO> buscarPorId(Long id);

    AreaDTO crear(AreaDTO area);

    Optional<AreaDTO> actualizar(Long id, AreaDTO area);

    boolean eliminar(Long id);
}
