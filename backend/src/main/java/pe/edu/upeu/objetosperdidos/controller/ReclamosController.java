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
import pe.edu.upeu.objetosperdidos.dto.RegistrarReclamoRequest;
import pe.edu.upeu.objetosperdidos.dto.RevisarCasoRequest;
import pe.edu.upeu.objetosperdidos.entity.EstadoReclamo;
import pe.edu.upeu.objetosperdidos.entity.TipoReclamo;
import pe.edu.upeu.objetosperdidos.security.UsuarioPrincipal;
import pe.edu.upeu.objetosperdidos.services.ICasosService;
import pe.edu.upeu.objetosperdidos.services.IFileStorageService;

@RestController
@RequestMapping("/api/v1/reclamos")
@Tag(name = "Reclamos de devolucion", description = "Reclamos sobre entregas ya realizadas")
public class ReclamosController {

    private final ICasosService casos;

    public ReclamosController(ICasosService casos) {
        this.casos = casos;
    }

    @PostMapping(consumes = { MediaType.MULTIPART_FORM_DATA_VALUE })
    @Operation(summary = "Presentar reclamo sobre una entrega ya realizada")
    public ResponseEntity<CasoRegistradoDTO> crear(
            @ModelAttribute @ParameterObject @Valid RegistrarReclamoRequest dto,
            @Parameter(description = "Foto de evidencia (JPG, PNG o WebP, max 5 MB)")
            @RequestPart(value = "evidencia", required = false) MultipartFile evidencia,
            @AuthenticationPrincipal UsuarioPrincipal usuario) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(casos.reclamar(dto, evidencia, usuario));
    }

    @GetMapping("/mios")
    @Operation(summary = "Seguimiento privado de mis reclamos")
    public List<CasoRegistradoDTO> mios(@AuthenticationPrincipal UsuarioPrincipal usuario) {
        return casos.misReclamos(usuario.idPersona());
    }

    @GetMapping
    @Operation(summary = "Bandeja de reclamos para personal (filtro de estado opcional)")
    public List<CasoRegistradoDTO> listar(@RequestParam(required = false) EstadoReclamo estado,
            @AuthenticationPrincipal UsuarioPrincipal usuario) {
        return casos.listar(TipoReclamo.RECLAMO, estado, usuario.idPersona());
    }

    @GetMapping("/pendientes")
    @Operation(summary = "Reclamos pendientes, del mas antiguo al mas nuevo")
    public List<CasoRegistradoDTO> pendientes(@AuthenticationPrincipal UsuarioPrincipal usuario) {
        return casos.pendientes(TipoReclamo.RECLAMO, usuario.idPersona());
    }

    @GetMapping("/{id}")
    @Operation(summary = "Consultar reclamo propio o consultar como personal")
    public CasoRegistradoDTO consultar(@PathVariable Long id,
            @AuthenticationPrincipal UsuarioPrincipal usuario) {
        return casos.consultarReclamo(id, usuario);
    }

    @GetMapping("/{id}/evidencia")
    @Operation(summary = "Ver la evidencia del reclamo (dueno o personal)")
    public ResponseEntity<byte[]> evidencia(@PathVariable Long id,
            @AuthenticationPrincipal UsuarioPrincipal usuario) {
        IFileStorageService.Imagen foto =
                casos.leerEvidencia(id, TipoReclamo.RECLAMO, usuario);
        return ResponseEntity.ok().cacheControl(CacheControl.noStore())
                .contentType(foto.mediaType()).body(foto.contenido());
    }

    @PatchMapping("/{id}/revision")
    @Operation(summary = "Pasar a EN_REVISION, o cerrar como APROBADO / RECHAZADO",
            description = "Cerrar exige resolucion (10-500 caracteres). No revierte la entrega; "
                    + "solo cambia el estado y notifica al estudiante.")
    public CasoRegistradoDTO revisar(@PathVariable Long id, @Valid @RequestBody RevisarCasoRequest dto,
            @AuthenticationPrincipal UsuarioPrincipal usuario) {
        return casos.revisar(id, TipoReclamo.RECLAMO, dto, usuario.idPersona());
    }
}
