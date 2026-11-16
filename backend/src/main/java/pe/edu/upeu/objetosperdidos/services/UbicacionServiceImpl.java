
package pe.edu.upeu.objetosperdidos.services;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pe.edu.upeu.objetosperdidos.dto.UbicacionDTO;
import pe.edu.upeu.objetosperdidos.entity.Ubicacion;
import pe.edu.upeu.objetosperdidos.exception.DuplicateResourceException;
import pe.edu.upeu.objetosperdidos.exception.ResourceInUseException;
import pe.edu.upeu.objetosperdidos.repository.UbicacionRepository;

import java.util.List;
import java.util.Optional;

@Service
public class UbicacionServiceImpl implements IUbicacionService {

    private final UbicacionRepository ubicacionRepository;

    public UbicacionServiceImpl(UbicacionRepository ubicacionRepository) {
        this.ubicacionRepository = ubicacionRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public List<UbicacionDTO> listarTodo() {
        return ubicacionRepository.findAll().stream()
                .map(this::convertToDTO)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<UbicacionDTO> buscarPorId(Long id) {
        return ubicacionRepository.findById(id)
                .map(this::convertToDTO);
    }

    @Override
    @Transactional
    public UbicacionDTO crear(UbicacionDTO dto) {
        String nombre = normalizarNombre(dto.nombre());
        validarNombreDisponible(nombre, null);

        Ubicacion ubicacion = new Ubicacion();
        ubicacion.setNombre(nombre);
        ubicacion.setDetalle(normalizarDetalle(dto.detalle()));

        return convertToDTO(
                ubicacionRepository.saveAndFlush(ubicacion)
        );
    }

    @Override
    @Transactional
    public Optional<UbicacionDTO> actualizar(
            Long id, UbicacionDTO dto) {

        return ubicacionRepository.findById(id).map(ubicacion -> {

            String nombre = normalizarNombre(dto.nombre());

            // Proteger los nombres de los tres puntos autorizados.
            if (esPuntoRecepcion(ubicacion.getNombre())
                    && !ubicacion.getNombre().equals(nombre)) {

                throw new ResourceInUseException(
                        "No se puede cambiar el nombre de Garita, "
                                + "Mesa de Partes o Limpieza"
                );
            }

            validarNombreDisponible(nombre, id);

            ubicacion.setNombre(nombre);
            ubicacion.setDetalle(normalizarDetalle(dto.detalle()));

            return convertToDTO(
                    ubicacionRepository.saveAndFlush(ubicacion)
            );
        });
    }

    @Override
    @Transactional
    public boolean eliminar(Long id) {

        return ubicacionRepository.findById(id).map(ubicacion -> {

            if (esPuntoRecepcion(ubicacion.getNombre())) {
                throw new ResourceInUseException(
                        "No se puede eliminar un punto autorizado "
                                + "de recepcion"
                );
            }

            // Las claves foraneas de Oracle impiden eliminar
            // ubicaciones utilizadas por otros registros.
            ubicacionRepository.delete(ubicacion);
            ubicacionRepository.flush();

            return true;

        }).orElse(false);
    }

    private void validarNombreDisponible(
            String nombre, Long idActual) {

        ubicacionRepository.findByNombreIgnoreCase(nombre)
                .ifPresent(existente -> {

                    if (idActual == null
                            || !existente.getIdUbicacion()
                            .equals(idActual)) {

                        throw new DuplicateResourceException(
                                "Ya existe una ubicacion con el nombre: "
                                        + nombre
                        );
                    }
                });
    }

    private String normalizarNombre(String nombre) {

        if (nombre == null || nombre.isBlank()) {
            throw new IllegalArgumentException(
                    "El nombre de la ubicacion es obligatorio"
            );
        }

        return nombre.strip().replaceAll("\\s+", " ");
    }

    private String normalizarDetalle(String detalle) {

        if (detalle == null || detalle.isBlank()) {
            return null;
        }

        return detalle.strip();
    }

    // Solo estos tres lugares reciben objetos oficialmente.
    private boolean esPuntoRecepcion(String nombre) {

        if (nombre == null) {
            return false;
        }

        String normalizado = nombre.strip()
                .replaceAll("\\s+", " ");

        return normalizado.equalsIgnoreCase("Garita")
                || normalizado.equalsIgnoreCase("Mesa de Partes")
                || normalizado.equalsIgnoreCase("Limpieza");
    }

    private UbicacionDTO convertToDTO(Ubicacion ubicacion) {

        return new UbicacionDTO(
                ubicacion.getIdUbicacion(),
                ubicacion.getNombre(),
                ubicacion.getDetalle()
        );
    }
}
