package pe.edu.upeu.objetosperdidos.services;

import pe.edu.upeu.objetosperdidos.dto.CategoriaDTO;

import java.util.List;
import java.util.Optional;

public interface ICategoriaService {
    List<CategoriaDTO> listarTodo();
    Optional<CategoriaDTO> buscarPorId(Long id);
    CategoriaDTO crear(CategoriaDTO categoria);
    Optional<CategoriaDTO> actualizar(Long id, CategoriaDTO categoria);
    boolean eliminar(Long id);
}
