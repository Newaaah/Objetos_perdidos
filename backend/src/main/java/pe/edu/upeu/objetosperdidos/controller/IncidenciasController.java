package pe.edu.upeu.objetosperdidos.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.CacheControl;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springdoc.core.annotations.ParameterObject;
import pe.edu.upeu.objetosperdidos.dto.CasoRegistradoDTO;
import pe.edu.upeu.objetosperdidos.dto.RegistrarIncidenciaRequest;
import pe.edu.upeu.objetosperdidos.dto.RevisarCasoRequest;
import pe.edu.upeu.objetosperdidos.entity.EstadoReclamo;
import pe.edu.upeu.objetosperdidos.entity.TipoReclamo;
import pe.edu.upeu.objetosperdidos.security.UsuarioPrincipal;
import pe.edu.upeu.objetosperdidos.services.ICasosService;
import pe.edu.upeu.objetosperdidos.services.IFileStorageService;

@RestController
@RequestMapping("/api/v1/incidencias")
@Tag(name = "Incidencias de custodia", description = "Solo personal activo y administradores")
public class IncidenciasController {

    private final ICasosService casos;

    public IncidenciasController(ICasosService casos) {
        this.casos = casos;
    }

    @PostMapping(consumes = { MediaType.MULTIPART_FORM_DATA_VALUE })
    @Operation(summary = "Registrar problema durante custodia activa")
    public ResponseEntity<CasoRegistradoDTO> crear(
            @ModelAttribute @ParameterObject @Valid RegistrarIncidenciaRequest dto,
            @Parameter(description = "Foto de evidencia (JPG, PNG o WebP, max 5 MB)")
            @RequestPart(value = "evidencia", required = false) MultipartFile evidencia,
            @AuthenticationPrincipal UsuarioPrincipal usuario) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(casos.incidencia(dto, evidencia, usuario.idPersona()));
    }

    @GetMapping
    @Operation(summary = "Bandeja de incidencias para personal")
    public List<CasoRegistradoDTO> listar(@RequestParam(required = false) EstadoReclamo estado,
            @AuthenticationPrincipal UsuarioPrincipal usuario) {
        return casos.listar(TipoReclamo.INCIDENCIA, estado, usuario.idPersona());
    }

    @GetMapping("/pendientes")
    public List<CasoRegistradoDTO> pendientes(@AuthenticationPrincipal UsuarioPrincipal usuario) {
        return casos.pendientes(TipoReclamo.INCIDENCIA, usuario.idPersona());
    }

    @GetMapping("/{id}")
    public CasoRegistradoDTO consultar(@PathVariable Long id,
            @AuthenticationPrincipal UsuarioPrincipal usuario) {
        return casos.consultarIncidencia(id, usuario.idPersona());
    }

    @GetMapping("/{id}/evidencia")
    @Operation(summary = "Ver la evidencia de la incidencia")
    public ResponseEntity<byte[]> evidencia(@PathVariable Long id,
            @AuthenticationPrincipal UsuarioPrincipal usuario) {
        IFileStorageService.Imagen foto =
                casos.leerEvidencia(id, TipoReclamo.INCIDENCIA, usuario);
        return ResponseEntity.ok().cacheControl(CacheControl.noStore())
                .contentType(foto.mediaType()).body(foto.contenido());
    }

    @PatchMapping("/{id}/revision")
    @Operation(summary = "Pasar a EN_REVISION o cerrar como RESUELTO (no modifica la custodia)")
    public CasoRegistradoDTO revisar(@PathVariable Long id, @Valid @RequestBody RevisarCasoRequest dto,
            @AuthenticationPrincipal UsuarioPrincipal usuario) {
        return casos.revisar(id, TipoReclamo.INCIDENCIA, dto, usuario.idPersona());
    }
}
