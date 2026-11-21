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

/** Almacenamiento privado de evidencias de reclamos e incidencias: images/reclamos. */
@Service
public class AlmacenamientoImagenReclamoService {

    private static final Logger log = LoggerFactory.getLogger(
            AlmacenamientoImagenReclamoService.class);
    private static final long LIMITE = 5L * 1024 * 1024;
    private final Path directorio;

    public record Imagen(byte[] contenido, MediaType mediaType) { }

    public AlmacenamientoImagenReclamoService(
            @Value("${storage.location:images}") String rutaBase) {
        this.directorio = Paths.get(rutaBase).toAbsolutePath()
                .normalize().resolve("reclamos").normalize();
    }

    public String guardar(MultipartFile imagen) {
        if (imagen == null || imagen.isEmpty()) {
            throw new IllegalArgumentException(
                    "La evidencia esta vacia");
        }
        if (imagen.getSize() > LIMITE) {
            throw new IllegalArgumentException(
                    "La imagen no debe superar los 5 MB");
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
            throw new UncheckedIOException("No se pudo leer la evidencia", ex);
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
            return "reclamos/" + destino.getFileName();
        } catch (IOException ex) {
            try {
                Files.deleteIfExists(destino);
            } catch (IOException limpieza) {
                log.warn("No se pudo limpiar evidencia incompleta", limpieza);
            }
            throw new UncheckedIOException("No se pudo guardar la evidencia", ex);
        }
    }

    public Imagen leer(String referencia) {
        if (referencia == null || !referencia.matches(
                "reclamos/[0-9a-fA-F-]{36}\\.(jpg|png|webp)")) {
            throw new ResourceNotFoundException("Evidencia no encontrada");
        }
        String nombre = referencia.substring("reclamos/".length());
        Path ruta = directorio.resolve(nombre).normalize();
        if (!ruta.startsWith(directorio)) {
            throw new ResourceNotFoundException("Referencia de imagen invalida");
        }
        try {
            Path realDirectorio = directorio.toRealPath();
            Path realArchivo = ruta.toRealPath();
            if (!realArchivo.startsWith(realDirectorio)
                    || !Files.isRegularFile(realArchivo)
                    || Files.size(realArchivo) > LIMITE) {
                throw new ResourceNotFoundException("Evidencia no encontrada");
            }
            MediaType media = nombre.endsWith(".jpg") ? MediaType.IMAGE_JPEG
                    : nombre.endsWith(".png") ? MediaType.IMAGE_PNG
                    : MediaType.parseMediaType("image/webp");
            return new Imagen(Files.readAllBytes(realArchivo), media);
        } catch (java.nio.file.NoSuchFileException ex) {
            throw new ResourceNotFoundException("Evidencia no encontrada");
        } catch (IOException ex) {
            throw new UncheckedIOException("No se pudo leer la evidencia", ex);
        }
    }

    /** Eliminar solo nombres creados por este servicio si Oracle revierte. */
    public void eliminarTrasRollback(String referencia) {
        if (referencia == null || !referencia.matches(
                "reclamos/[0-9a-fA-F-]{36}\\.(jpg|png|webp)")) return;
        Path ruta = directorio.resolve(referencia.substring("reclamos/".length()))
                .normalize();
        if (!ruta.startsWith(directorio)) return;
        try {
            Files.deleteIfExists(ruta);
        } catch (IOException ex) {
            log.error("No se pudo limpiar una evidencia de transaccion revertida", ex);
        }
    }

    private static boolean firmaValida(byte[] b, String extension) {
        if (".jpg".equals(extension)) {
            return b.length >= 3 && (b[0] & 255) == 255
                    && (b[1] & 255) == 216 && (b[2] & 255) == 255;
        }
        if (".png".equals(extension)) {
            int[] cabecera = {137, 80, 78, 71, 13, 10, 26, 10};
            if (b.length < cabecera.length) return false;
            for (int i = 0; i < cabecera.length; i++) {
                if ((b[i] & 255) != cabecera[i]) return false;
            }
            return true;
        }
        return ".webp".equals(extension) && b.length >= 12
                && b[0] == 'R' && b[1] == 'I' && b[2] == 'F' && b[3] == 'F'
                && b[8] == 'W' && b[9] == 'E' && b[10] == 'B' && b[11] == 'P';
    }
}
