package pe.edu.upeu.objetosperdidos.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Valid;
import jakarta.validation.Validator;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import org.springframework.http.CacheControl;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import pe.edu.upeu.objetosperdidos.dto.CustodiaRegistradaDTO;
import pe.edu.upeu.objetosperdidos.dto.PendienteRecepcionCustodiaDTO;
import pe.edu.upeu.objetosperdidos.dto.RecepcionCustodiaRequest;
import pe.edu.upeu.objetosperdidos.dto.RecepcionDirectaCustodiaRequest;
import pe.edu.upeu.objetosperdidos.security.UsuarioPrincipal;
import pe.edu.upeu.objetosperdidos.services.AlmacenamientoImagenCustodiaService;
import pe.edu.upeu.objetosperdidos.services.IRecepcionCustodiaService;

@RestController
@RequestMapping("/api/v1/custodias/recepcion")
@Tag(name = "Recepcion fisica de custodias", description = "Solo PERSONAL y ADMIN")
public class RecepcionCustodiaController {

    private final IRecepcionCustodiaService servicio;
    private final ObjectMapper mapper;
    private final Validator validator;

    public RecepcionCustodiaController(
            IRecepcionCustodiaService servicio, ObjectMapper mapper,
            Validator validator) {
        this.servicio = servicio;
        this.mapper = mapper;
        this.validator = validator;
    }

    @Operation(summary = "Hallazgos aprobados que todavia no tienen custodia")
    @GetMapping("/pendientes")
    public ResponseEntity<List<PendienteRecepcionCustodiaDTO>> pendientes() {
        return ResponseEntity.ok(servicio.pendientes());
    }

    @Operation(summary = "Recepcion vinculada a hallazgo con imagen previa valida (JSON)",
            description = "Se mantiene el endpoint anterior. Solo funciona si la imagen original se puede leer.")
    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<CustodiaRegistradaDTO> recibir(
            @Valid @RequestBody RecepcionCustodiaRequest datos,
            @AuthenticationPrincipal UsuarioPrincipal usuario) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(servicio.recibir(datos, usuario.idPersona()));
    }

    @Operation(summary = "Recepcion con hallazgo aprobado (foto opcional)",
            description = "La foto original se reutiliza si existe; en otro caso, adjunta una nueva.")
    @PostMapping(value = "/con-hallazgo", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<CustodiaRegistradaDTO> recibirConHallazgo(
            @Parameter(description = "JSON de RecepcionCustodiaRequest")
            @RequestPart("datos") String datosJson,
            @Parameter(description = "Fotografia nueva, obligatoria si la previa falta")
            @RequestPart(value = "imagen", required = false) MultipartFile imagen,
            @AuthenticationPrincipal UsuarioPrincipal usuario) {
        RecepcionCustodiaRequest datos = convertir(datosJson, RecepcionCustodiaRequest.class);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(servicio.recibirConImagen(datos, imagen, usuario.idPersona()));
    }

    @Operation(summary = "Recepcion directa sin hallazgo previo (fotografia obligatoria)",
            description = "No inventa un reporte; crea OBJETO y OBJETOS_CUSTODIADOS.")
    @PostMapping(value = "/directa", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<CustodiaRegistradaDTO> recibirDirectamente(
            @Parameter(description = "JSON de RecepcionDirectaCustodiaRequest")
            @RequestPart("datos") String datosJson,
            @RequestPart("imagen") MultipartFile imagen,
            @AuthenticationPrincipal UsuarioPrincipal usuario) {
        RecepcionDirectaCustodiaRequest datos = convertir(
                datosJson, RecepcionDirectaCustodiaRequest.class);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(servicio.recibirDirectamente(datos, imagen, usuario.idPersona()));
    }

    @Operation(summary = "Consultar custodia registrada por ID")
    @GetMapping("/{idCustodia}")
    public ResponseEntity<CustodiaRegistradaDTO> consultar(@PathVariable Long idCustodia) {
        return ResponseEntity.ok(servicio.consultar(idCustodia));
    }

    @Operation(summary = "Ver fotografia de custodia (o del hallazgo asociado)")
    @GetMapping("/{idCustodia}/imagen")
    public ResponseEntity<byte[]> imagen(@PathVariable Long idCustodia) {
        AlmacenamientoImagenCustodiaService.Imagen foto = servicio.leerImagen(idCustodia);
        return ResponseEntity.ok()
                .cacheControl(CacheControl.noStore())
                .contentType(foto.mediaType())
                .body(foto.contenido());
    }

    private <T> T convertir(String json, Class<T> clase) {
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
