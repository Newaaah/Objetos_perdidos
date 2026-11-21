package pe.edu.upeu.objetosperdidos.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.http.CacheControl;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import pe.edu.upeu.objetosperdidos.dto.EntregaRegistradaDTO;
import pe.edu.upeu.objetosperdidos.dto.EstadoCustodiaEntregaDTO;
import pe.edu.upeu.objetosperdidos.dto.MiEntregaDTO;
import pe.edu.upeu.objetosperdidos.dto.RegistrarEntregaRequest;
import pe.edu.upeu.objetosperdidos.security.UsuarioPrincipal;
import pe.edu.upeu.objetosperdidos.services.IEntregaService;
import pe.edu.upeu.objetosperdidos.services.IFileStorageService;

@RestController
@RequestMapping("/api/v1/entregas")
@Tag(name = "Devolucion fisica de objetos", description = "Solo PERSONAL y ADMIN")
public class EntregaController {

    private final IEntregaService servicio;

    public EntregaController(IEntregaService servicio) {
        this.servicio = servicio;
    }

    @Operation(summary = "Consultar mi historial privado de devoluciones")
    @GetMapping("/mias")
    public ResponseEntity<List<MiEntregaDTO>> mias(
            @AuthenticationPrincipal UsuarioPrincipal usuario) {
        return ResponseEntity.ok().cacheControl(CacheControl.noStore())
                .body(servicio.misEntregas(usuario.idPersona()));
    }

    @Operation(summary = "Comprobar si una custodia esta disponible para entrega")
    @GetMapping("/custodias/{idCustodia}/estado")
    public ResponseEntity<EstadoCustodiaEntregaDTO> estadoCustodia(
            @PathVariable Long idCustodia) {
        return ResponseEntity.ok(servicio.estadoCustodia(idCustodia));
    }

    @Operation(summary = "Registrar devolucion con verificacion presencial y fotografia")
    @PostMapping(consumes = { MediaType.MULTIPART_FORM_DATA_VALUE })
    public ResponseEntity<EntregaRegistradaDTO> registrar(
            @ModelAttribute @ParameterObject @Valid RegistrarEntregaRequest dto,
            @Parameter(description = "Foto de evidencia JPG, PNG o WebP, hasta 5 MB")
            @RequestPart("fotografia") MultipartFile fotografia,
            @AuthenticationPrincipal UsuarioPrincipal usuario) {
        return new ResponseEntity<>(
                servicio.registrar(dto, fotografia, usuario.idPersona()), HttpStatus.CREATED);
    }

    @Operation(summary = "Consultar una entrega registrada")
    @GetMapping("/{idEntrega}")
    public ResponseEntity<EntregaRegistradaDTO> consultar(@PathVariable Long idEntrega) {
        return ResponseEntity.ok(servicio.consultar(idEntrega));
    }

    @Operation(summary = "Ver la fotografia de evidencia de una entrega")
    @GetMapping("/{idEntrega}/evidencia")
    public ResponseEntity<byte[]> evidencia(@PathVariable Long idEntrega) {
        IFileStorageService.Imagen imagen = servicio.leerEvidencia(idEntrega);
        return ResponseEntity.ok()
                .cacheControl(CacheControl.noStore())
                .contentType(imagen.mediaType())
                .body(imagen.contenido());
    }
}
