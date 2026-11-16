package pe.edu.upeu.objetosperdidos.exception;

import jakarta.servlet.http.HttpServletRequest;

import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import org.springframework.security.core.AuthenticationException;

import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import pe.edu.upeu.objetosperdidos.controller.AuthController;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.Map;

@Order(Ordered.HIGHEST_PRECEDENCE)
@RestControllerAdvice(assignableTypes = AuthController.class)
public class AuthExceptionHandler {

    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<Map<String, Object>>
    manejarErrorAutenticacion(
            AuthenticationException ex,
            HttpServletRequest request) {

        Map<String, Object> respuesta =
                new LinkedHashMap<>();

        respuesta.put(
                "timestamp",
                LocalDateTime.now().toString()
        );

        respuesta.put("status", 401);

        respuesta.put("error", "Unauthorized");

        respuesta.put(
                "message",
                "Credenciales invalidas o cuenta no habilitada"
        );

        respuesta.put(
                "path",
                request.getRequestURI()
        );

        return ResponseEntity
                .status(HttpStatus.UNAUTHORIZED)
                .body(respuesta);
    }
}
