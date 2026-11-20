
package pe.edu.upeu.objetosperdidos.services;

import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import pe.edu.upeu.objetosperdidos.dto.AuthResponse;
import pe.edu.upeu.objetosperdidos.dto.LoginRequest;

import pe.edu.upeu.objetosperdidos.entity.*;
import pe.edu.upeu.objetosperdidos.repository.*;

import pe.edu.upeu.objetosperdidos.security.IdentificadorLogin;
import pe.edu.upeu.objetosperdidos.security.JwtService;
import pe.edu.upeu.objetosperdidos.security.UsuarioPrincipal;

@Service
public class AuthServiceImpl implements IAuthService {

    private final UsuarioRepository usuarioRepository;
    private final EstudianteRepository estudianteRepository;
    private final PersonalRepository personalRepository;
    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;

    public AuthServiceImpl(
            UsuarioRepository usuarioRepository,
            EstudianteRepository estudianteRepository,
            PersonalRepository personalRepository,
            AuthenticationManager authenticationManager,
            JwtService jwtService) {

        this.usuarioRepository = usuarioRepository;
        this.estudianteRepository = estudianteRepository;
        this.personalRepository = personalRepository;
        this.authenticationManager = authenticationManager;
        this.jwtService = jwtService;
    }

    @Override
    @Transactional(readOnly = true)
    public AuthResponse login(LoginRequest request) {

        String ingreso;

        try {
            ingreso = IdentificadorLogin.normalizarIngreso(
                    request.ingreso()
            );
        } catch (IllegalArgumentException ex) {
            throw credencialesInvalidas();
        }

        if (IdentificadorLogin.esCodigoUniversitario(ingreso)) {
            return loginEstudiantePorCodigo(
                    ingreso,
                    request.password()
            );
        }

        return loginPorDni(ingreso, request.password());
    }

    private AuthResponse loginEstudiantePorCodigo(
            String codigo,
            String password) {

        Estudiante estudiante = estudianteRepository
                .findByCodigoUni(codigo)
                .orElseThrow(this::credencialesInvalidas);

        return autenticarEstudiante(estudiante, password);
    }

    private AuthResponse loginPorDni(
            String dni,
            String password) {

        Usuario usuario = usuarioRepository
                .findByPersonaTipoDocumentoAndPersonaDocumentoIdentidad(
                        "DNI",
                        dni
                )
                .orElse(null);

        if (usuario != null) {

            if (usuario.getRol() == RolUsuario.ESTUDIANTE) {

                Estudiante estudiante = estudianteRepository
                        .findById(usuario.getIdPersona())
                        .orElseThrow(this::credencialesInvalidas);

                return autenticarEstudiante(estudiante, password);
            }

            if (usuario.getRol() == RolUsuario.PERSONAL
                    || usuario.getRol() == RolUsuario.ADMIN) {

                return autenticarPersonal(usuario, dni, password);
            }

            throw credencialesInvalidas();
        }

        // Estudiante que aun no tiene cuenta en USUARIO.
        Estudiante estudiante = estudianteRepository
                .findByPersonaTipoDocumentoAndPersonaDocumentoIdentidad(
                        "DNI",
                        dni
                )
                .orElseThrow(this::credencialesInvalidas);

        return autenticarEstudiante(estudiante, password);
    }

    private AuthResponse autenticarEstudiante(
            Estudiante estudiante,
            String password) {

        if (estudiante.getEstado() != EstadoEstudiante.ACTIVO) {
            throw credencialesInvalidas();
        }

        Long idPersona = estudiante.getIdPersona();

        // Evitar acceso estudiantil a personas registradas
        // como personal, incluso si su registro esta inactivo.
        if (personalRepository.existsById(idPersona)) {
            throw credencialesInvalidas();
        }

        Usuario usuario = usuarioRepository
                .findById(idPersona)
                .orElse(null);

        if (usuario != null) {

            if (usuario.getEstado() != EstadoUsuario.ACTIVO
                    || usuario.getRol() != RolUsuario.ESTUDIANTE) {

                throw credencialesInvalidas();
            }
        }

        Persona persona = estudiante.getPersona();

        String identificador = IdentificadorLogin.construir(
                persona.getTipoDocumento(),
                persona.getDocumentoIdentidad()
        );

        String hash = usuario == null
                ? null
                : usuario.getPassword();

        // Si el estudiante configuro una contrasena,
        // esta pasa a ser obligatoria.
        if (hash != null && !hash.isBlank()) {

            if (password == null || password.isBlank()) {
                throw credencialesInvalidas();
            }

            UsuarioPrincipal principal = autenticarConPassword(
                    identificador,
                    password
            );

            if (!principal.isEnabled()
                    || principal.rol() != RolUsuario.ESTUDIANTE
                    || !idPersona.equals(principal.idPersona())) {

                throw credencialesInvalidas();
            }

            return generarRespuesta(principal);
        }

        // Sin contrasena configurada: acceso ligero.
        if (password != null && !password.isBlank()) {
            throw credencialesInvalidas();
        }

        UsuarioPrincipal principal = new UsuarioPrincipal(
                idPersona,
                identificador,
                "",
                RolUsuario.ESTUDIANTE,
                true
        );

        return generarRespuesta(principal);
    }

    private AuthResponse autenticarPersonal(
            Usuario usuario,
            String dni,
            String password) {

        if (usuario.getEstado() != EstadoUsuario.ACTIVO) {
            throw credencialesInvalidas();
        }

        if (usuario.getRol() != RolUsuario.PERSONAL
                && usuario.getRol() != RolUsuario.ADMIN) {
            throw credencialesInvalidas();
        }

        if (password == null || password.isBlank()) {
            throw credencialesInvalidas();
        }

        boolean personalActivo = personalRepository
                .findById(usuario.getIdPersona())
                .map(personal ->
                        personal.getEstado() == EstadoPersonal.ACTIVO
                )
                .orElse(false);

        if (!personalActivo) {
            throw credencialesInvalidas();
        }

        String identificador = IdentificadorLogin.construir(
                "DNI",
                dni
        );

        UsuarioPrincipal principal = autenticarConPassword(
                identificador,
                password
        );

        if (!principal.isEnabled()
                || !usuario.getIdPersona().equals(principal.idPersona())
                || principal.rol() != usuario.getRol()) {

            throw credencialesInvalidas();
        }

        return generarRespuesta(principal);
    }

    private UsuarioPrincipal autenticarConPassword(
            String identificador,
            String password) {

        Authentication authentication = authenticationManager
                .authenticate(
                        new UsernamePasswordAuthenticationToken(
                                identificador,
                                password
                        )
                );

        if (!(authentication.getPrincipal()
                instanceof UsuarioPrincipal principal)) {

            throw credencialesInvalidas();
        }

        return principal;
    }

    private AuthResponse generarRespuesta(
            UsuarioPrincipal principal) {

        String token = jwtService.generateToken(principal);

        return new AuthResponse(
                token,
                "Bearer",
                principal.idPersona(),
                principal.rol().name()
        );
    }

    private BadCredentialsException credencialesInvalidas() {

        return new BadCredentialsException(
                "Credenciales invalidas o cuenta no habilitada"
        );
    }
}
