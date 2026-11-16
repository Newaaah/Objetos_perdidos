package pe.edu.upeu.objetosperdidos.services;

import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.Authentication;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import pe.edu.upeu.objetosperdidos.dto.AuthResponse;
import pe.edu.upeu.objetosperdidos.dto.LoginRequest;

import pe.edu.upeu.objetosperdidos.entity.Estudiante;
import pe.edu.upeu.objetosperdidos.entity.EstadoEstudiante;
import pe.edu.upeu.objetosperdidos.entity.EstadoPersonal;
import pe.edu.upeu.objetosperdidos.entity.EstadoUsuario;
import pe.edu.upeu.objetosperdidos.entity.Persona;
import pe.edu.upeu.objetosperdidos.entity.RolUsuario;
import pe.edu.upeu.objetosperdidos.entity.Usuario;

import pe.edu.upeu.objetosperdidos.repository.EstudianteRepository;
import pe.edu.upeu.objetosperdidos.repository.PersonalRepository;
import pe.edu.upeu.objetosperdidos.repository.UsuarioRepository;

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

        // Un codigo universitario identifica al estudiante.
        if (IdentificadorLogin.esCodigoUniversitario(ingreso)) {
            return loginEstudiantePorCodigo(ingreso);
        }

        // Un DNI puede corresponder a cualquier rol.
        return loginPorDni(ingreso, request.password());
    }

    private AuthResponse loginEstudiantePorCodigo(String codigo) {

        Estudiante estudiante = estudianteRepository
                .findByCodigoUni(codigo)
                .orElseThrow(this::credencialesInvalidas);

        Usuario usuario = usuarioRepository
                .findById(estudiante.getIdPersona())
                .orElseThrow(this::credencialesInvalidas);

        return autenticarEstudiante(usuario);
    }

    private AuthResponse loginPorDni(
            String dni,
            String password) {

        Usuario usuario = usuarioRepository
                .findByPersonaTipoDocumentoAndPersonaDocumentoIdentidad(
                        "DNI",
                        dni
                )
                .orElseThrow(this::credencialesInvalidas);

        if (usuario.getEstado() != EstadoUsuario.ACTIVO) {
            throw credencialesInvalidas();
        }

        // El estudiante no necesita contrasena.
        if (usuario.getRol() == RolUsuario.ESTUDIANTE) {
            return autenticarEstudiante(usuario);
        }

        // PERSONAL y ADMIN siempre necesitan contrasena.
        if (usuario.getRol() == RolUsuario.PERSONAL
                || usuario.getRol() == RolUsuario.ADMIN) {

            return autenticarPersonal(usuario, dni, password);
        }

        throw credencialesInvalidas();
    }

    private AuthResponse autenticarEstudiante(Usuario usuario) {

        Estudiante estudiante = estudianteRepository
                .findById(usuario.getIdPersona())
                .orElseThrow(this::credencialesInvalidas);

        if (usuario.getEstado() != EstadoUsuario.ACTIVO
                || usuario.getRol() != RolUsuario.ESTUDIANTE
                || estudiante.getEstado() != EstadoEstudiante.ACTIVO) {

            throw credencialesInvalidas();
        }

        // Impedir acceso sin contrasena si tambien es
        // trabajador activo, incluso ante un rol mal asignado.
        boolean trabajadorActivo = personalRepository
                .findById(usuario.getIdPersona())
                .map(personal ->
                        personal.getEstado() == EstadoPersonal.ACTIVO
                )
                .orElse(false);

        if (trabajadorActivo) {
            throw credencialesInvalidas();
        }

        Persona persona = usuario.getPersona();

        String identificador = IdentificadorLogin.construir(
                persona.getTipoDocumento(),
                persona.getDocumentoIdentidad()
        );

        UsuarioPrincipal principal = new UsuarioPrincipal(
                usuario.getIdPersona(),
                identificador,
                usuario.getPassword(),
                RolUsuario.ESTUDIANTE,
                true
        );

        return generarRespuesta(principal);
    }

    private AuthResponse autenticarPersonal(
            Usuario usuario,
            String dni,
            String password) {

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

        // Spring Security verifica la contrasena con BCrypt.
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

        if (!principal.isEnabled()
                || (principal.rol() != RolUsuario.PERSONAL
                && principal.rol() != RolUsuario.ADMIN)) {

            throw credencialesInvalidas();
        }

        return generarRespuesta(principal);
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
