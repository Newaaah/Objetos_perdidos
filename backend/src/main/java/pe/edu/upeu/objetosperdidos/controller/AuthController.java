package pe.edu.upeu.objetosperdidos.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

import jakarta.validation.Valid;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import pe.edu.upeu.objetosperdidos.dto.AuthResponse;
import pe.edu.upeu.objetosperdidos.dto.LoginRequest;
import pe.edu.upeu.objetosperdidos.services.IAuthService;

@RestController
@RequestMapping("/api/v1/auth")
@Tag(
        name = "Autenticacion",
        description = "Inicio de sesion de estudiantes, personal y administradores"
)
public class AuthController {

    private final IAuthService authService;

    public AuthController(IAuthService authService) {
        this.authService = authService;
    }

    @Operation(
            summary = "Iniciar sesion",
            description = "ESTUDIANTE: codigo universitario o DNI. " +
                    "PERSONAL y ADMIN: DNI y contrasena obligatoria."
    )
    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(
            @Valid @RequestBody LoginRequest request) {

        AuthResponse response = authService.login(request);

        return ResponseEntity.ok(response);
    }
}
