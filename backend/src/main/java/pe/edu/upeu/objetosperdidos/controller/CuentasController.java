package pe.edu.upeu.objetosperdidos.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import pe.edu.upeu.objetosperdidos.dto.*;
import pe.edu.upeu.objetosperdidos.security.UsuarioPrincipal;
import pe.edu.upeu.objetosperdidos.services.ICuentaService;

@RestController
@Tag(name = "Gestion segura de cuentas", description = "Clave estudiantil inicial por ADMIN y cambio de clave propia")
public class CuentasController {
    private final ICuentaService cuentas;
    public CuentasController(ICuentaService cuentas) { this.cuentas = cuentas; }

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
}
