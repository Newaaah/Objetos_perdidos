package pe.edu.upeu.objetosperdidos.services;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Locale;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pe.edu.upeu.objetosperdidos.entity.ReporteHallazgos;
import pe.edu.upeu.objetosperdidos.exception.ResourceNotFoundException;
import pe.edu.upeu.objetosperdidos.repository.ReporteHallazgosRepository;

/** Acceso a fotografias almacenadas fuera de recursos publicos. */
@Service
public class LecturaImagenHallazgoService {

    private static final long LIMITE = 5L * 1024 * 1024;
    private final Path carpeta;
    private final ReporteHallazgosRepository hallazgos;

    public record Imagen(byte[] contenido, MediaType mediaType) { }

    public LecturaImagenHallazgoService(
            @Value("${storage.location:images}") String rutaBase,
            ReporteHallazgosRepository hallazgos) {
        this.carpeta = Paths.get(rutaBase).toAbsolutePath().normalize()
                .resolve("hallazgos").normalize();
        this.hallazgos = hallazgos;
    }

    @Transactional(readOnly = true)
    public Imagen leer(Long idHallazgo) {
        ReporteHallazgos hallazgo = hallazgos.findById(idHallazgo)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Hallazgo no encontrado con ID: " + idHallazgo));
        String referencia = hallazgo.getImagenRef();

        // Nunca se acepta una ruta proporcionada por la solicitud HTTP.
        if (referencia == null || !referencia.matches(
                "hallazgos/[0-9a-fA-F-]{36}\\.(jpg|png|webp)")) {
            throw new ResourceNotFoundException(
                    "Este hallazgo no tiene una fotografia almacenada en el servidor");
        }

        Path candidato = carpeta.resolve(referencia.substring("hallazgos/".length()))
                .normalize();
        if (!candidato.startsWith(carpeta)) {
            throw new ResourceNotFoundException("Referencia de imagen invalida");
        }
        try {
            Path carpetaReal = carpeta.toRealPath();
            Path archivoReal = candidato.toRealPath();
            if (!archivoReal.startsWith(carpetaReal)
                    || !Files.isRegularFile(archivoReal)) {
                throw new ResourceNotFoundException("Imagen no encontrada");
            }
            if (Files.size(archivoReal) > LIMITE) {
                throw new IllegalStateException("Imagen almacenada demasiado grande");
            }

            String extension = referencia.substring(referencia.lastIndexOf('.') + 1)
                    .toLowerCase(Locale.ROOT);
            MediaType tipo = switch (extension) {
                case "jpg" -> MediaType.IMAGE_JPEG;
                case "png" -> MediaType.IMAGE_PNG;
                case "webp" -> MediaType.parseMediaType("image/webp");
                default -> throw new ResourceNotFoundException("Imagen no encontrada");
            };
            return new Imagen(Files.readAllBytes(archivoReal), tipo);
        } catch (java.nio.file.NoSuchFileException ex) {
            throw new ResourceNotFoundException("Imagen no encontrada");
        } catch (IOException ex) {
            throw new IllegalStateException("No se pudo leer la imagen almacenada", ex);
        }
    }
}
