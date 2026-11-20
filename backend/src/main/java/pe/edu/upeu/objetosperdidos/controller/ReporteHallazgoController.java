
package pe.edu.upeu.objetosperdidos.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validator;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import pe.edu.upeu.objetosperdidos.dto.CrearReporteHallazgoRequest;
import pe.edu.upeu.objetosperdidos.dto.MiHallazgoDTO;
import pe.edu.upeu.objetosperdidos.dto.ReporteHallazgoCreadoDTO;
import pe.edu.upeu.objetosperdidos.security.UsuarioPrincipal;
import pe.edu.upeu.objetosperdidos.services.IReporteHallazgoService;

@RestController
@RequestMapping("/api/v1/hallazgos")
@Tag(name = "Reportes de hallazgo",
        description = "Registro de objetos encontrados y consulta de mis reportes")
public class ReporteHallazgoController {

    private final IReporteHallazgoService servicio;
    private final ObjectMapper objectMapper;
    private final Validator validator;

    public ReporteHallazgoController(
            IReporteHallazgoService servicio,
            ObjectMapper objectMapper,
            Validator validator) {
        this.servicio = servicio;
        this.objectMapper = objectMapper;
        this.validator = validator;
    }

    @Operation(
            summary = "Registrar un hallazgo con imagen (no confirma custodia)",
            description = "Formulario multipart. Escribe el JSON en 'datos' "
                    + "y selecciona una imagen JPG, PNG o WebP en 'imagen'.")
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ReporteHallazgoCreadoDTO> crear(
            @Parameter(
                    description = "JSON con los datos del hallazgo, sin imagen",
                    example = "{\"idCategoria\":2,\"nombre\":\"Mochila hallada de prueba\","
                            + "\"marca\":\"Sin marca\",\"color\":\"Azul\","
                            + "\"descripcion\":\"Objeto encontrado cerca del CRAI\","
                            + "\"idUbicacionEncontrado\":1,\"idUbicacionDestino\":2}")
            @RequestPart("datos") String datosJson,
            @RequestPart("imagen") MultipartFile imagen,
            @AuthenticationPrincipal UsuarioPrincipal usuario) {

        CrearReporteHallazgoRequest datos = convertirYValidar(datosJson);

        ReporteHallazgoCreadoDTO resultado =
                servicio.crear(datos, imagen, usuario.idPersona());

        return ResponseEntity.status(HttpStatus.CREATED).body(resultado);
    }

    @Operation(summary = "Consultar solamente mis reportes de hallazgos")
    @GetMapping("/mios")
    public ResponseEntity<List<MiHallazgoDTO>> misHallazgos(
            @AuthenticationPrincipal UsuarioPrincipal usuario) {
        return ResponseEntity.ok(servicio.misHallazgos(usuario.idPersona()));
    }

    private CrearReporteHallazgoRequest convertirYValidar(String datosJson) {
        if (datosJson == null || datosJson.isBlank()) {
            throw new IllegalArgumentException(
                    "Los datos del hallazgo son obligatorios");
        }

        CrearReporteHallazgoRequest datos;

        try {
            datos = objectMapper.readValue(
                    datosJson, CrearReporteHallazgoRequest.class);
        } catch (JsonProcessingException ex) {
            throw new IllegalArgumentException(
                    "El campo datos debe contener un JSON valido", ex);
        }

        if (datos == null) {
            throw new IllegalArgumentException(
                    "Los datos del hallazgo son obligatorios");
        }

        Set<ConstraintViolation<CrearReporteHallazgoRequest>> errores =
                validator.validate(datos);

        if (!errores.isEmpty()) {
            String detalle = errores.stream()
                    .map(error -> error.getPropertyPath()
                            + ": " + error.getMessage())
                    .sorted()
                    .collect(Collectors.joining("; "));

            throw new IllegalArgumentException(
                    "Datos invalidos: " + detalle);
        }

        return datos;
    }
}
