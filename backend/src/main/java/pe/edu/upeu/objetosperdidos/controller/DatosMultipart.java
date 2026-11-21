package pe.edu.upeu.objetosperdidos.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validator;
import java.util.Set;
import java.util.stream.Collectors;
import org.springframework.stereotype.Component;

/** Convierte y valida la parte "datos" (JSON) de una peticion multipart. */
@Component
public class DatosMultipart {

    private final ObjectMapper mapper;
    private final Validator validator;

    public DatosMultipart(ObjectMapper mapper, Validator validator) {
        this.mapper = mapper;
        this.validator = validator;
    }

    public <T> T leer(String json, Class<T> clase) {
        if (json == null || json.isBlank()) {
            throw new IllegalArgumentException("Los datos son obligatorios");
        }
        T valor;
        try {
            valor = mapper.readValue(json, clase);
        } catch (JsonProcessingException ex) {
            throw new IllegalArgumentException("El campo datos debe contener un JSON valido", ex);
        }
        if (valor == null) {
            throw new IllegalArgumentException("Los datos son obligatorios");
        }
        Set<ConstraintViolation<T>> errores = validator.validate(valor);
        if (!errores.isEmpty()) {
            String detalle = errores.stream()
                    .map(e -> e.getPropertyPath() + ": " + e.getMessage())
                    .sorted().collect(Collectors.joining("; "));
            throw new IllegalArgumentException("Datos invalidos: " + detalle);
        }
        return valor;
    }
}
