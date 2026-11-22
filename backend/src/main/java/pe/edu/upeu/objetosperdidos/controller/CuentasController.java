package pe.edu.upeu.objetosperdidos.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.CacheControl;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import pe.edu.upeu.objetosperdidos.dto.*;
import pe.edu.upeu.objetosperdidos.security.UsuarioPrincipal;
import pe.edu.upeu.objetosperdidos.services.ICuentaService;
import pe.edu.upeu.objetosperdidos.services.IPersonaService;

@RestController
@Tag(name = "Gestion segura de cuentas", description = "Clave estudiantil inicial por ADMIN, cambio de clave propia y mi perfil")
public class CuentasController {
    private final ICuentaService cuentas;
    private final IPersonaService personas;

    public CuentasController(ICuentaService cuentas, IPersonaService personas) {
        this.cuentas = cuentas;
        this.personas = personas;
    }

    @PostMapping("/api/v1/admin/estudiantes/{idPersona}/clave")
    @Operation(summary = "ADMIN: habilitar o restablecer la clave de un estudiante activo")
    public ResponseEntity<Void> provisionar(@PathVariable Long idPersona,
                                            @Valid @RequestBody DefinirClaveRequest dto, @AuthenticationPrincipal UsuarioPrincipal admin) {
        cuentas.aprovisionarEstudiante(idPersona, dto.nuevaClave(), admin.idPersona());
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/api/v1/cuenta/clave")
    @Operation(summary = "Cambiar mi clave presentando la anterior")
    public ResponseEntity<Void> cambiar(@Valid @RequestBody CambiarClaveRequest dto,
                                        @AuthenticationPrincipal UsuarioPrincipal usuario) {
        cuentas.cambiarMiClave(usuario.idPersona(), dto.claveActual(), dto.nuevaClave());
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/api/v1/cuenta/perfil")
    @Operation(summary = "Mis datos (nombre, documento y codigo) a partir del token")
    public ResponseEntity<PersonaResumenDTO> perfil(@AuthenticationPrincipal UsuarioPrincipal usuario) {
        return ResponseEntity.ok().cacheControl(CacheControl.noStore())
                .body(personas.perfil(usuario.idPersona()));
    }
}