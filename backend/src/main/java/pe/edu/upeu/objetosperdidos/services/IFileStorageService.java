package pe.edu.upeu.objetosperdidos.services;

import org.springframework.http.MediaType;
import org.springframework.web.multipart.MultipartFile;

/**
 * Manejo de imagenes, igual que el IFileStorageService del proyecto guia,
 * pero separando las fotos en subcarpetas dentro de storage.location.
 * Las imagenes NO se publican como recursos estaticos: se leen desde los
 * controladores para poder controlar quien las ve.
 */
public interface IFileStorageService {

    String HALLAZGOS = "hallazgos";
    String CUSTODIAS = "custodias";
    String ENTREGAS = "entregas";
    String RECLAMOS = "reclamos";

    record Imagen(byte[] contenido, MediaType mediaType) { }

    void init(); // Crea las carpetas si no existen

    String store(MultipartFile file, String carpeta); // Guarda y devuelve "carpeta/uuid.ext"

    /** Guarda la imagen y la borra si la transaccion actual se revierte. */
    String storeEnTransaccion(MultipartFile file, String carpeta);

    Imagen load(String referencia); // Lee una imagen guardada

    void delete(String referencia); // Borra el archivo (si existe)
}
