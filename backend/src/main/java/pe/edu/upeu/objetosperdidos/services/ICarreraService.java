package pe.edu.upeu.objetosperdidos.services;

import pe.edu.upeu.objetosperdidos.dto.CarreraDTO;

import java.util.List;
import java.util.Optional;

public interface ICarreraService {

    List<CarreraDTO> listarTodo();

    Optional<CarreraDTO> buscarPorId(Long id);

    CarreraDTO crear(CarreraDTO carrera);

    Optional<CarreraDTO> actualizar(Long id, CarreraDTO carrera);

    boolean eliminar(Long id);
}
