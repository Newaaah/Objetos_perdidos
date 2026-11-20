package pe.edu.upeu.objetosperdidos.services;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.Locale;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import pe.edu.upeu.objetosperdidos.exception.ResourceNotFoundException;

/** Carpeta privada: images/custodias, no publicada por un resource handler. */
@Service
public class AlmacenamientoImagenCustodiaService {
    private static final Logger log = LoggerFactory.getLogger(
            AlmacenamientoImagenCustodiaService.class);
    private static final long LIMITE = 5L * 1024 * 1024;
    private final Path directorio;

    public record Imagen(byte[] contenido, MediaType mediaType) { }

    public AlmacenamientoImagenCustodiaService(
            @Value("${storage.location:images}") String rutaBase) {
        this.directorio = Paths.get(rutaBase).toAbsolutePath().normalize()
                .resolve("custodias").normalize();
    }

    /** Nunca usa el nombre de archivo facilitado por el usuario. */
    public String guardar(MultipartFile imagen) {
        if (imagen == null || imagen.isEmpty()) {
            throw new IllegalArgumentException("La fotografia de recepcion es obligatoria");
        }
        if (imagen.getSize() > LIMITE) {
            throw new IllegalArgumentException("La imagen no debe superar los 5 MB");
        }
        String tipo = imagen.getContentType();
        String extension = switch (tipo == null ? "" : tipo.toLowerCase(Locale.ROOT)) {
            case "image/jpeg" -> ".jpg";
            case "image/png" -> ".png";
            case "image/webp" -> ".webp";
            default -> throw new IllegalArgumentException(
                    "Solo se permiten imagenes JPG, PNG o WebP");
        };
        byte[] contenido;
        try {
            contenido = imagen.getBytes();
        } catch (IOException ex) {
            throw new UncheckedIOException("No se pudo leer la fotografia", ex);
        }
        if (contenido.length == 0 || contenido.length > LIMITE
                || !firmaValida(contenido, extension)) {
            throw new IllegalArgumentException(
                    "El archivo no es una imagen JPG, PNG o WebP valida");
        }

        Path destino = directorio.resolve(UUID.randomUUID() + extension);
        try {
            Files.createDirectories(directorio);
            Files.write(destino, contenido, StandardOpenOption.CREATE_NEW);
            return "custodias/" + destino.getFileName();
        } catch (IOException ex) {
            try {
                Files.deleteIfExists(destino);
            } catch (IOException cleanupEx) {
                log.warn("No se pudo limpiar una imagen incompleta", cleanupEx);
            }
            throw new UncheckedIOException("No se pudo almacenar la fotografia", ex);
        }
    }

    public Imagen leer(String referencia) {
        if (referencia == null || !referencia.matches(
                "custodias/[0-9a-fA-F-]{36}\\.(jpg|png|webp)")) {
            throw new ResourceNotFoundException("Fotografia de custodia no encontrada");
        }
        String archivo = referencia.substring("custodias/".length());
        Path ruta = directorio.resolve(archivo).normalize();
        if (!ruta.startsWith(directorio)) {
            throw new ResourceNotFoundException("Referencia de fotografia invalida");
        }
        try {
            Path directorioReal = directorio.toRealPath();
            Path rutaReal = ruta.toRealPath();
            if (!rutaReal.startsWith(directorioReal) || !Files.isRegularFile(rutaReal)
                    || Files.size(rutaReal) > LIMITE) {
                throw new ResourceNotFoundException("Fotografia de custodia no encontrada");
            }
            String extension = archivo.substring(archivo.lastIndexOf('.') + 1)
                    .toLowerCase(Locale.ROOT);
            MediaType media = switch (extension) {
                case "jpg" -> MediaType.IMAGE_JPEG;
                case "png" -> MediaType.IMAGE_PNG;
                case "webp" -> MediaType.parseMediaType("image/webp");
                default -> throw new ResourceNotFoundException("Imagen no encontrada");
            };
            return new Imagen(Files.readAllBytes(rutaReal), media);
        } catch (java.nio.file.NoSuchFileException ex) {
            throw new ResourceNotFoundException("Fotografia de custodia no encontrada");
        } catch (IOException ex) {
            throw new UncheckedIOException("No se pudo leer la fotografia", ex);
        }
    }

    /** Solo elimina referencias que tienen el patron de nombres generado aqui. */
    public void eliminarTrasRollback(String referencia) {
        if (referencia == null || !referencia.matches(
                "custodias/[0-9a-fA-F-]{36}\\.(jpg|png|webp)")) {
            return;
        }
        Path ruta = directorio.resolve(referencia.substring("custodias/".length()))
                .normalize();
        if (!ruta.startsWith(directorio)) return;
        try {
            Files.deleteIfExists(ruta);
        } catch (IOException ex) {
            log.error("No se pudo eliminar la imagen de una transaccion revertida", ex);
        }
    }

    private static boolean firmaValida(byte[] b, String extension) {
        if (".jpg".equals(extension)) {
            return b.length >= 3 && (b[0] & 0xff) == 0xff
                    && (b[1] & 0xff) == 0xd8 && (b[2] & 0xff) == 0xff;
        }
        if (".png".equals(extension)) {
            int[] firma = {137, 80, 78, 71, 13, 10, 26, 10};
            if (b.length < firma.length) return false;
            for (int i = 0; i < firma.length; i++) {
                if ((b[i] & 0xff) != firma[i]) return false;
            }
            return true;
        }
        return ".webp".equals(extension) && b.length >= 12
                && b[0] == 'R' && b[1] == 'I' && b[2] == 'F' && b[3] == 'F'
                && b[8] == 'W' && b[9] == 'E' && b[10] == 'B' && b[11] == 'P';
    }
}
