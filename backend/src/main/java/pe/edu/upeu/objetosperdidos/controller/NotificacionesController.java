package pe.edu.upeu.objetosperdidos.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import org.springframework.http.CacheControl;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import pe.edu.upeu.objetosperdidos.dto.ConteoNoLeidasDTO;
import pe.edu.upeu.objetosperdidos.dto.NotificacionDTO;
import pe.edu.upeu.objetosperdidos.security.UsuarioPrincipal;
import pe.edu.upeu.objetosperdidos.services.INotificacionService;

@RestController
@RequestMapping("/api/v1/notificaciones")
@Tag(name = "Notificaciones privadas", description = "Cada persona ve y marca solo sus propias notificaciones")
public class NotificacionesController {
    private final INotificacionService bandeja;
    public NotificacionesController(INotificacionService bandeja) { this.bandeja = bandeja; }

    @Operation(summary = "Listar mis notificaciones (por fecha descendente)")
    @GetMapping("/mias")
    public ResponseEntity<List<NotificacionDTO>> mias(
            @RequestParam(defaultValue = "false") boolean soloNoLeidas,
            @AuthenticationPrincipal UsuarioPrincipal usuario) {
        return ResponseEntity.ok().cacheControl(CacheControl.noStore())
                .body(bandeja.mias(usuario.idPersona(), soloNoLeidas));
    }

    @Operation(summary = "Contar mis notificaciones sin leer")
    @GetMapping("/no-leidas/conteo")
    public ResponseEntity<ConteoNoLeidasDTO> pendientes(@AuthenticationPrincipal UsuarioPrincipal usuario) {
        return ResponseEntity.ok().cacheControl(CacheControl.noStore())
                .body(bandeja.pendientes(usuario.idPersona()));
    }

    @Operation(summary = "Marcar como leida una notificacion propia")
    @PatchMapping("/{id}/leida")
    public ResponseEntity<NotificacionDTO> leida(@PathVariable Long id,
            @AuthenticationPrincipal UsuarioPrincipal usuario) {
        return ResponseEntity.ok().cacheControl(CacheControl.noStore())
                .body(bandeja.marcarLeida(id, usuario.idPersona()));
    }
}
