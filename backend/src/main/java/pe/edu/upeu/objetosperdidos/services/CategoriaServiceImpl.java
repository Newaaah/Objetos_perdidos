package pe.edu.upeu.objetosperdidos.services;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pe.edu.upeu.objetosperdidos.dto.CategoriaDTO;
import pe.edu.upeu.objetosperdidos.entity.Categoria;
import pe.edu.upeu.objetosperdidos.exception.DuplicateResourceException;
import pe.edu.upeu.objetosperdidos.exception.ResourceInUseException;
import pe.edu.upeu.objetosperdidos.repository.CategoriaRepository;
import pe.edu.upeu.objetosperdidos.repository.ObjetoRepository;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;

@Service
public class CategoriaServiceImpl implements ICategoriaService {

    private final CategoriaRepository categoriaRepository;
    private final ObjetoRepository objetoRepository;

    public CategoriaServiceImpl(CategoriaRepository categoriaRepository,
                                ObjetoRepository objetoRepository) {
        this.categoriaRepository = categoriaRepository;
        this.objetoRepository = objetoRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public List<CategoriaDTO> listarTodo() {
        return categoriaRepository.findAll().stream()
                .sorted(Comparator.comparing(Categoria::getIdCategoria))
                .map(this::convertToDTO)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<CategoriaDTO> buscarPorId(Long id) {
        return categoriaRepository.findById(id).map(this::convertToDTO);
    }

    @Override
    @Transactional
    public CategoriaDTO crear(CategoriaDTO dto) {
        String nombreNormalizado = normalizarNombre(dto.nombre());
        validarNombreDisponible(nombreNormalizado, null);

        Categoria categoria = new Categoria();
        categoria.setNombre(nombreNormalizado);

        return convertToDTO(categoriaRepository.saveAndFlush(categoria));
    }

    @Override
    @Transactional
    public Optional<CategoriaDTO> actualizar(Long id, CategoriaDTO dto) {
        return categoriaRepository.findById(id).map(categoria -> {
            String nombreNormalizado = normalizarNombre(dto.nombre());
            validarNombreDisponible(nombreNormalizado, id);

            categoria.setNombre(nombreNormalizado);
            return convertToDTO(categoriaRepository.saveAndFlush(categoria));
        });
    }

    @Override
    @Transactional
    public boolean eliminar(Long id) {
        return categoriaRepository.findById(id).map(categoria -> {
            if (objetoRepository.existsByCategoria_IdCategoria(id)) {
                throw new ResourceInUseException(
                        "No se puede eliminar la categoria porque existen objetos asociados");
            }
            categoriaRepository.delete(categoria);
            categoriaRepository.flush();
            return true;
        }).orElse(false);
    }

    private void validarNombreDisponible(String nombre, Long idActual) {
        categoriaRepository.findByNombreIgnoreCase(nombre).ifPresent(existente -> {
            if (idActual == null || !existente.getIdCategoria().equals(idActual)) {
                throw new DuplicateResourceException(
                        "Ya existe una categoria con el nombre: " + nombre);
            }
        });
    }

    private String normalizarNombre(String nombre) {
        // @Valid verifica NotBlank; dejamos este control como defensa adicional.
        if (nombre == null || nombre.isBlank()) {
            throw new IllegalArgumentException("El nombre de la categoria es obligatorio");
        }
        return nombre.strip().replaceAll("\\s+", " ");
    }

    private CategoriaDTO convertToDTO(Categoria categoria) {
        return new CategoriaDTO(categoria.getIdCategoria(), categoria.getNombre());
    }
}
