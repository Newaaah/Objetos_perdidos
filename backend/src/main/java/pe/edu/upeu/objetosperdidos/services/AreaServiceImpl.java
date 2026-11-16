package pe.edu.upeu.objetosperdidos.services;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pe.edu.upeu.objetosperdidos.dto.AreaDTO;
import pe.edu.upeu.objetosperdidos.entity.Area;
import pe.edu.upeu.objetosperdidos.exception.DuplicateResourceException;
import pe.edu.upeu.objetosperdidos.exception.ResourceInUseException;
import pe.edu.upeu.objetosperdidos.repository.AreaRepository;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;

@Service
public class AreaServiceImpl implements IAreaService {

    private final AreaRepository areaRepository;

    public AreaServiceImpl(AreaRepository areaRepository) {
        this.areaRepository = areaRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public List<AreaDTO> listarTodo() {
        return areaRepository.findAll().stream()
                .sorted(Comparator.comparing(Area::getIdArea))
                .map(this::convertToDTO)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<AreaDTO> buscarPorId(Long id) {
        return areaRepository.findById(id).map(this::convertToDTO);
    }

    @Override
    @Transactional
    public AreaDTO crear(AreaDTO dto) {
        String nombre = normalizarNombre(dto.nombre());
        validarNombreDisponible(nombre, null);

        Area area = new Area();
        area.setNombre(nombre);
        return convertToDTO(areaRepository.saveAndFlush(area));
    }

    @Override
    @Transactional
    public Optional<AreaDTO> actualizar(Long id, AreaDTO dto) {
        return areaRepository.findById(id).map(area -> {
            String nombre = normalizarNombre(dto.nombre());
            validarNombreDisponible(nombre, id);

            area.setNombre(nombre);
            return convertToDTO(areaRepository.saveAndFlush(area));
        });
    }

    @Override
    @Transactional
    public boolean eliminar(Long id) {
        return areaRepository.findById(id).map(area -> {
            try {
                // No existe deleteAndFlush en JpaRepository.
                // El flush comprueba las claves foraneas en Oracle.
                areaRepository.delete(area);
                areaRepository.flush();
            } catch (DataIntegrityViolationException ex) {
                throw new ResourceInUseException(
                        "No se puede eliminar el area porque tiene personal asociado");
            }
            return true;
        }).orElse(false);
    }

    private void validarNombreDisponible(String nombre, Long idActual) {
        areaRepository.findByNombreIgnoreCase(nombre).ifPresent(existente -> {
            if (idActual == null || !existente.getIdArea().equals(idActual)) {
                throw new DuplicateResourceException(
                        "Ya existe un area con el nombre: " + nombre);
            }
        });
    }

    private String normalizarNombre(String nombre) {
        if (nombre == null || nombre.isBlank()) {
            throw new IllegalArgumentException("El nombre del area es obligatorio");
        }
        return nombre.strip().replaceAll("\\s+", " ");
    }

    private AreaDTO convertToDTO(Area area) {
        return new AreaDTO(area.getIdArea(), area.getNombre());
    }
}
