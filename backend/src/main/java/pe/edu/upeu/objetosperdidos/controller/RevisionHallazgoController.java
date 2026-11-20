package pe.edu.upeu.objetosperdidos.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import pe.edu.upeu.objetosperdidos.dto.HallazgoPendienteRevisionDTO;
import pe.edu.upeu.objetosperdidos.dto.HallazgoRevisionResultadoDTO;
import pe.edu.upeu.objetosperdidos.dto.ResolverHallazgoRequest;
import pe.edu.upeu.objetosperdidos.security.UsuarioPrincipal;
import pe.edu.upeu.objetosperdidos.services.IRevisionHallazgoService;
import pe.edu.upeu.objetosperdidos.services.LecturaImagenHallazgoService;

@RestController
@RequestMapping("/api/v1/hallazgos/revision")
@Tag(name = "Revision de hallazgos", description = "Solo PERSONAL y ADMIN")
public class RevisionHallazgoController {

    private final IRevisionHallazgoService revisiones;
    private final LecturaImagenHallazgoService imagenes;

    public RevisionHallazgoController(
            IRevisionHallazgoService revisiones,
            LecturaImagenHallazgoService imagenes) {
        this.revisiones = revisiones;
        this.imagenes = imagenes;
    }

    @Operation(summary = "Listar hallazgos pendientes de revisar")
    @GetMapping("/pendientes")
    public ResponseEntity<List<HallazgoPendienteRevisionDTO>> pendientes() {
        return ResponseEntity.ok(revisiones.pendientes());
    }

    @Operation(summary = "Ver imagen privada de un hallazgo")
    @GetMapping("/{idHallazgo}/imagen")
    public ResponseEntity<byte[]> imagen(@PathVariable Long idHallazgo) {
        LecturaImagenHallazgoService.Imagen imagen = imagenes.leer(idHallazgo);
        return ResponseEntity.ok()
                .contentType(imagen.mediaType())
                .header(HttpHeaders.CACHE_CONTROL, "no-store")
                .header("X-Content-Type-Options", "nosniff")
                .body(imagen.contenido());
    }

    @Operation(summary = "Aprobar o rechazar un reporte PENDIENTE (sin crear custodia)")
    @PatchMapping("/{idHallazgo}/decision")
    public ResponseEntity<HallazgoRevisionResultadoDTO> resolver(
            @PathVariable Long idHallazgo,
            @Valid @RequestBody ResolverHallazgoRequest solicitud,
            @AuthenticationPrincipal UsuarioPrincipal usuario) {
        return ResponseEntity.ok(
                revisiones.resolver(idHallazgo, solicitud, usuario.idPersona()));
    }
}
