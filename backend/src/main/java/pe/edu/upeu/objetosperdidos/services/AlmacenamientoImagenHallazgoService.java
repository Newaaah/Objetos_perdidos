package pe.edu.upeu.objetosperdidos.services;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
public class AlmacenamientoImagenHallazgoService {

    private static final Logger log =
            LoggerFactory.getLogger(AlmacenamientoImagenHallazgoService.class);

    private static final long MAX_BYTES = 5L * 1024 * 1024;
    private final Path directorio;

    public AlmacenamientoImagenHallazgoService(
            @Value("${storage.location:images}") String rutaBase) {
        this.directorio = Paths.get(rutaBase)
                .toAbsolutePath()
                .normalize()
                .resolve("hallazgos");
    }

    /**
     * Guarda la imagen y devuelve una referencia relativa a storage.location.
     * No se utiliza el nombre original del archivo suministrado por el cliente.
     */
    public String guardar(MultipartFile imagen) {
        if (imagen == null || imagen.isEmpty()) {
            throw new IllegalArgumentException("Debes seleccionar una imagen del objeto");
        }
        if (imagen.getSize() > MAX_BYTES) {
            throw new IllegalArgumentException("La imagen no debe superar los 5 MB");
        }

        String tipo = imagen.getContentType();
        String extension = switch (tipo == null ? "" : tipo.toLowerCase(java.util.Locale.ROOT)) {
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
            throw new UncheckedIOException("No se pudo leer la imagen", ex);
        }
        if (contenido.length > MAX_BYTES || !tieneFirmaValida(contenido, extension)) {
            throw new IllegalArgumentException(
                    "El archivo no corresponde a una imagen JPG, PNG o WebP valida");
        }

        Path destino = directorio.resolve(UUID.randomUUID() + extension);
        try {
            Files.createDirectories(directorio);
            Files.write(destino, contenido, StandardOpenOption.CREATE_NEW);
            return "hallazgos/" + destino.getFileName();
        } catch (IOException ex) {
            try {
                Files.deleteIfExists(destino);
            } catch (IOException limpieza) {
                log.warn("No se pudo limpiar un archivo de imagen incompleto", limpieza);
            }
            throw new UncheckedIOException("No se pudo almacenar la imagen", ex);
        }
    }

    /** Solo recibe referencias generadas por este servicio. */
    public void eliminarTrasRollback(String referencia) {
        if (referencia == null || !referencia.matches(
                "hallazgos/[0-9a-fA-F-]{36}\\.(jpg|png|webp)")) {
            return;
        }
        Path ruta = directorio.resolve(referencia.substring("hallazgos/".length()))
                .normalize();
        if (!ruta.startsWith(directorio)) {
            return;
        }
        try {
            Files.deleteIfExists(ruta);
        } catch (IOException ex) {
            log.error("No se pudo eliminar la imagen de una transaccion revertida", ex);
        }
    }

    private static boolean tieneFirmaValida(byte[] b, String extension) {
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
