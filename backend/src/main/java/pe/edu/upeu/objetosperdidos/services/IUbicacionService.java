package pe.edu.upeu.objetosperdidos.services;

import pe.edu.upeu.objetosperdidos.dto.UbicacionDTO;

import java.util.List;
import java.util.Optional;

public interface IUbicacionService {

    List<UbicacionDTO> listarTodo();

    Optional<UbicacionDTO> buscarPorId(Long id);

    UbicacionDTO crear(UbicacionDTO ubicacion);

    Optional<UbicacionDTO> actualizar(Long id, UbicacionDTO ubicacion);

    boolean eliminar(Long id);
}
