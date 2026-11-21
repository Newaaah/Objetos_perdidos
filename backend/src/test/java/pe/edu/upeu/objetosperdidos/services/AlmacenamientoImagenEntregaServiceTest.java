package pe.edu.upeu.objetosperdidos.services;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Base64;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;

import pe.edu.upeu.objetosperdidos.exception.ResourceNotFoundException;

import static org.junit.jupiter.api.Assertions.*;

/** Pruebas aisladas: todas las imagenes se guardan en una carpeta temporal. */
class AlmacenamientoImagenEntregaServiceTest {

    @TempDir
    Path carpetaTemporal;

    // Una imagen PNG de prueba, de un solo pixel.
    private static final byte[] PNG = Base64.getDecoder().decode(
            "iVBORw0KGgoAAAANSUhEUgAAAAEAAAABCAYAAAAfFcSJ" +
            "AAAADUlEQVQIHWP4z8DwHwAFgAI/ScL3sAAAAABJRU5ErkJggg==");

    private AlmacenamientoImagenEntregaService servicio() {
        return new AlmacenamientoImagenEntregaService(carpetaTemporal.toString());
    }

    @Test
    void guardaYRecuperaUnaImagenValida() throws Exception {
        var servicio = servicio();
        var archivo = new MockMultipartFile(
                "fotografia", "evidencia.png", "image/png", PNG);

        String referencia = servicio.guardar(archivo);

        assertTrue(referencia.matches("entregas/[0-9a-f-]{36}\\.png"));
        assertTrue(Files.isRegularFile(
                carpetaTemporal.resolve(referencia)));
        var recuperada = servicio.leer(referencia);
        assertArrayEquals(PNG, recuperada.contenido());
        assertEquals(MediaType.IMAGE_PNG, recuperada.mediaType());
    }

    @Test
    void rechazaArchivoTxtSinGuardarNada() {
        var servicio = servicio();
        var archivo = new MockMultipartFile(
                "fotografia", "prueba.txt", "text/plain", "PRUEBA".getBytes());

        var error = assertThrows(IllegalArgumentException.class,
                () -> servicio.guardar(archivo));

        assertTrue(error.getMessage().contains("Solo se permiten imagenes"));
        assertFalse(Files.exists(carpetaTemporal.resolve("entregas")));
    }

    @Test
    void rechazaMasDeCincoMegabytesSinGuardarNada() {
        var servicio = servicio();
        byte[] contenido = new byte[5 * 1024 * 1024 + 1];
        var archivo = new MockMultipartFile(
                "fotografia", "gigante.png", "image/png", contenido);

        var error = assertThrows(IllegalArgumentException.class,
                () -> servicio.guardar(archivo));

        assertTrue(error.getMessage().contains("5 MB"));
        assertFalse(Files.exists(carpetaTemporal.resolve("entregas")));
    }

    @Test
    void rechazaPngFalsoAunqueDeclareMimePng() {
        var servicio = servicio();
        var archivo = new MockMultipartFile(
                "fotografia", "falso.png", "image/png", "NO ES PNG".getBytes());

        var error = assertThrows(IllegalArgumentException.class,
                () -> servicio.guardar(archivo));

        assertTrue(error.getMessage().contains("no es una imagen"));
        assertFalse(Files.exists(carpetaTemporal.resolve("entregas")));
    }

    @Test
    void rechazaFotografiaVacia() {
        var servicio = servicio();
        var archivo = new MockMultipartFile(
                "fotografia", "vacia.png", "image/png", new byte[0]);

        assertThrows(IllegalArgumentException.class,
                () -> servicio.guardar(archivo));
        assertFalse(Files.exists(carpetaTemporal.resolve("entregas")));
    }

    @Test
    void eliminaEvidenciaRegistradaCuandoSeSolicitaLimpiezaDeRollback() throws Exception {
        var servicio = servicio();
        String referencia = servicio.guardar(new MockMultipartFile(
                "fotografia", "evidencia.png", "image/png", PNG));
        Path ruta = carpetaTemporal.resolve(referencia);
        assertTrue(Files.isRegularFile(ruta));

        servicio.eliminarTrasRollback(referencia);

        assertFalse(Files.exists(ruta));
    }

    @Test
    void rechazaReferenciasDeRutaNoAutorizadas() {
        var servicio = servicio();

        assertThrows(ResourceNotFoundException.class,
                () -> servicio.leer("entregas/../../secreto.txt"));
    }
}
