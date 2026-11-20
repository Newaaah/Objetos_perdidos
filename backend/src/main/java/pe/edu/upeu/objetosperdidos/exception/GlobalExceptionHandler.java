
package pe.edu.upeu.objetosperdidos.exception;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import org.springframework.web.multipart.MultipartException;
import org.springframework.web.multipart.support.MissingServletRequestPartException;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log =
            LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<Object> handleResourceNotFound(
            ResourceNotFoundException ex) {

        return error(HttpStatus.NOT_FOUND, ex.getMessage());
    }

    @ExceptionHandler(DuplicateResourceException.class)
    public ResponseEntity<Object> handleDuplicateResource(
            DuplicateResourceException ex) {

        return error(HttpStatus.CONFLICT, ex.getMessage());
    }

    @ExceptionHandler(ResourceInUseException.class)
    public ResponseEntity<Object> handleResourceInUse(
            ResourceInUseException ex) {

        return error(HttpStatus.CONFLICT, ex.getMessage());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Object> handleValidationExceptions(
            MethodArgumentNotValidException ex) {

        Map<String, Object> body = cuerpo(HttpStatus.BAD_REQUEST);
        Map<String, String> errors = new LinkedHashMap<>();

        ex.getBindingResult().getFieldErrors().forEach(
                fieldError -> errors.put(
                        fieldError.getField(),
                        fieldError.getDefaultMessage()
                )
        );

        body.put("errors", errors);

        return new ResponseEntity<>(body, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Object> handleInvalidArgument(
            IllegalArgumentException ex) {

        return error(HttpStatus.BAD_REQUEST, ex.getMessage());
    }

    @ExceptionHandler(MissingServletRequestPartException.class)
    public ResponseEntity<Object> handleMissingPart(
            MissingServletRequestPartException ex) {

        return error(
                HttpStatus.BAD_REQUEST,
                "Falta una parte obligatoria del formulario: "
                        + ex.getRequestPartName()
        );
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<Object> handleUnreadablePart(
            HttpMessageNotReadableException ex) {

        return error(
                HttpStatus.BAD_REQUEST,
                "El JSON de los datos enviados no es valido"
        );
    }

    @ExceptionHandler(HttpMediaTypeNotSupportedException.class)
    public ResponseEntity<Object> handleUnsupportedType(
            HttpMediaTypeNotSupportedException ex) {

        return error(
                HttpStatus.UNSUPPORTED_MEDIA_TYPE,
                "Formato de solicitud no admitido"
        );
    }

    @ExceptionHandler(MaxUploadSizeExceededException.class)
    public ResponseEntity<Object> handleTooLarge(
            MaxUploadSizeExceededException ex) {

        return error(
                HttpStatus.PAYLOAD_TOO_LARGE,
                "La imagen no debe superar los 5 MB"
        );
    }

    @ExceptionHandler(MultipartException.class)
    public ResponseEntity<Object> handleMultipart(
            MultipartException ex) {

        return error(
                HttpStatus.BAD_REQUEST,
                "No se pudo procesar el formulario con la imagen"
        );
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<Object> handleIntegrityViolation(
            DataIntegrityViolationException ex) {

        log.error("Error de integridad al guardar datos", ex);

        return error(
                HttpStatus.CONFLICT,
                "La operacion incumple una restriccion "
                        + "de integridad de la base de datos"
        );
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Object> handleGlobalException(
            Exception ex) {

        log.error("Error interno durante la operacion", ex);

        return error(
                HttpStatus.INTERNAL_SERVER_ERROR,
                "Ocurrio un error interno en el servidor"
        );
    }

    private ResponseEntity<Object> error(
            HttpStatus status,
            String message) {

        Map<String, Object> body = cuerpo(status);
        body.put("message", message);

        return new ResponseEntity<>(body, status);
    }

    private Map<String, Object> cuerpo(HttpStatus status) {

        Map<String, Object> body = new LinkedHashMap<>();

        body.put("timestamp", LocalDateTime.now());
        body.put("status", status.value());
        body.put("error", status.getReasonPhrase());

        return body;
    }
}
