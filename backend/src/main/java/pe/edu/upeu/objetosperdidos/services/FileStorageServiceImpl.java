package pe.edu.upeu.objetosperdidos.services;

import jakarta.annotation.PostConstruct;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.List;
import java.util.Locale;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.springframework.web.multipart.MultipartFile;
import pe.edu.upeu.objetosperdidos.exception.ResourceNotFoundException;

@Service
public class FileStorageServiceImpl implements IFileStorageService {

    private static final Logger log = LoggerFactory.getLogger(FileStorageServiceImpl.class);
    private static final long LIMITE = 5L * 1024 * 1024; // 5 MB
    private static final List<String> CARPETAS = List.of(HALLAZGOS, CUSTODIAS, ENTREGAS, RECLAMOS);
    // Solo se aceptan nombres generados por este servicio: carpeta/uuid.ext
    private static final String PATRON =
            "(hallazgos|custodias|entregas|reclamos)/[0-9a-fA-F-]{36}\\.(jpg|png|webp)";

    private final Path raiz;

    public FileStorageServiceImpl(@Value("${storage.location:images}") String storageLocation) {
        this.raiz = Paths.get(storageLocation).toAbsolutePath().normalize();
    }

    @Override
    @PostConstruct
    public void init() {
        try {
            for (String carpeta : CARPETAS) {
                Files.createDirectories(raiz.resolve(carpeta));
            }
        } catch (IOException e) {
            throw new UncheckedIOException("No se pudo inicializar la carpeta de imagenes", e);
        }
    }

    @Override
    public String store(MultipartFile file, String carpeta) {
        if (!CARPETAS.contains(carpeta)) {
            throw new IllegalArgumentException("Carpeta de imagenes invalida");
        }
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("Debes adjuntar una imagen");
        }
        if (file.getSize() > LIMITE) {
            throw new IllegalArgumentException("La imagen no debe superar los 5 MB");
        }
        // La extension sale del tipo de archivo, nunca del nombre que manda el usuario.
        String tipo = file.getContentType();
        String extension = switch (tipo == null ? "" : tipo.toLowerCase(Locale.ROOT)) {
            case "image/jpeg" -> ".jpg";
            case "image/png" -> ".png";
            case "image/webp" -> ".webp";
            default -> throw new IllegalArgumentException("Solo se permiten imagenes JPG, PNG o WebP");
        };
        byte[] contenido;
        try {
            contenido = file.getBytes();
        } catch (IOException e) {
            throw new UncheckedIOException("No se pudo leer la imagen", e);
        }
        if (contenido.length == 0 || !esImagenReal(contenido, extension)) {
            throw new IllegalArgumentException("El archivo no es una imagen JPG, PNG o WebP valida");
        }
        String referencia = carpeta + "/" + UUID.randomUUID() + extension;
        Path destino = raiz.resolve(referencia);
        try {
            Files.createDirectories(destino.getParent());
            Files.write(destino, contenido, StandardOpenOption.CREATE_NEW);
            return referencia;
        } catch (IOException e) {
            delete(referencia);
            throw new UncheckedIOException("Error al guardar la imagen", e);
        }
    }

    @Override
    public String storeEnTransaccion(MultipartFile file, String carpeta) {
        if (!TransactionSynchronizationManager.isSynchronizationActive()) {
            throw new IllegalStateException("No hay una transaccion activa");
        }
        String referencia = store(file, carpeta);
        // Si el INSERT en Oracle falla, se borra el archivo para no dejar basura.
        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
            @Override
            public void afterCompletion(int estado) {
                if (estado != TransactionSynchronization.STATUS_COMMITTED) {
                    delete(referencia);
                }
            }
        });
        return referencia;
    }

    @Override
    public Imagen load(String referencia) {
        Path archivo = rutaSegura(referencia);
        if (archivo == null) {
            throw new ResourceNotFoundException("Imagen no encontrada");
        }
        try {
            if (!Files.isRegularFile(archivo) || Files.size(archivo) > LIMITE) {
                throw new ResourceNotFoundException("Imagen no encontrada");
            }
            MediaType media = referencia.endsWith(".jpg") ? MediaType.IMAGE_JPEG
                    : referencia.endsWith(".png") ? MediaType.IMAGE_PNG
                    : MediaType.parseMediaType("image/webp");
            return new Imagen(Files.readAllBytes(archivo), media);
        } catch (NoSuchFileException e) {
            throw new ResourceNotFoundException("Imagen no encontrada");
        } catch (IOException e) {
            throw new UncheckedIOException("No se pudo leer la imagen", e);
        }
    }

    @Override
    public void delete(String referencia) {
        Path archivo = rutaSegura(referencia);
        if (archivo == null) return;
        try {
            Files.deleteIfExists(archivo);
        } catch (IOException e) {
            log.error("No se pudo borrar el archivo: {}", referencia, e);
        }
    }

    /** Evita leer o borrar archivos fuera de la carpeta de imagenes. */
    private Path rutaSegura(String referencia) {
        if (referencia == null || !referencia.matches(PATRON)) return null;
        Path ruta = raiz.resolve(referencia).normalize();
        return ruta.startsWith(raiz) ? ruta : null;
    }

    /** Revisa los primeros bytes: un .exe renombrado a .jpg no pasa. */
    private static boolean esImagenReal(byte[] b, String extension) {
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
