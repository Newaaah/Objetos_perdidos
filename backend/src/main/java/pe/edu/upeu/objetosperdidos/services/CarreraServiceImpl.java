package pe.edu.upeu.objetosperdidos.services;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pe.edu.upeu.objetosperdidos.dto.CarreraDTO;
import pe.edu.upeu.objetosperdidos.entity.Carrera;
import pe.edu.upeu.objetosperdidos.exception.DuplicateResourceException;
import pe.edu.upeu.objetosperdidos.exception.ResourceInUseException;
import pe.edu.upeu.objetosperdidos.repository.CarreraRepository;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;

@Service
public class CarreraServiceImpl implements ICarreraService {

    private final CarreraRepository carreraRepository;

    public CarreraServiceImpl(CarreraRepository carreraRepository) {
        this.carreraRepository = carreraRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public List<CarreraDTO> listarTodo() {
        return carreraRepository.findAll().stream()
                .sorted(Comparator.comparing(Carrera::getIdCarrera))
                .map(this::convertToDTO)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<CarreraDTO> buscarPorId(Long id) {
        return carreraRepository.findById(id).map(this::convertToDTO);
    }

    @Override
    @Transactional
    public CarreraDTO crear(CarreraDTO dto) {
        String nombre = normalizarNombre(dto.nombre());
        validarNombreDisponible(nombre, null);

        Carrera carrera = new Carrera();
        carrera.setNombre(nombre);
        return convertToDTO(carreraRepository.saveAndFlush(carrera));
    }

    @Override
    @Transactional
    public Optional<CarreraDTO> actualizar(Long id, CarreraDTO dto) {
        return carreraRepository.findById(id).map(carrera -> {
            String nombre = normalizarNombre(dto.nombre());
            validarNombreDisponible(nombre, id);

            carrera.setNombre(nombre);
            return convertToDTO(carreraRepository.saveAndFlush(carrera));
        });
    }

    @Override
    @Transactional
    public boolean eliminar(Long id) {
        return carreraRepository.findById(id).map(carrera -> {
            try {
                // JpaRepository ofrece delete() y flush(), no deleteAndFlush().
                // Oracle comprobara si hay estudiantes asociados.
                carreraRepository.delete(carrera);
                carreraRepository.flush();
            } catch (DataIntegrityViolationException ex) {
                throw new ResourceInUseException(
                        "No se puede eliminar la carrera porque tiene estudiantes asociados");
            }
            return true;
        }).orElse(false);
    }

    private void validarNombreDisponible(String nombre, Long idActual) {
        carreraRepository.findByNombreIgnoreCase(nombre).ifPresent(existente -> {
            if (idActual == null || !existente.getIdCarrera().equals(idActual)) {
                throw new DuplicateResourceException(
                        "Ya existe una carrera con el nombre: " + nombre);
            }
        });
    }

    private String normalizarNombre(String nombre) {
        if (nombre == null || nombre.isBlank()) {
            throw new IllegalArgumentException("El nombre de la carrera es obligatorio");
        }
        return nombre.strip().replaceAll("\\s+", " ");
    }

    private CarreraDTO convertToDTO(Carrera carrera) {
        return new CarreraDTO(carrera.getIdCarrera(), carrera.getNombre());
    }
}
