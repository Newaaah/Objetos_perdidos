
package pe.edu.upeu.objetosperdidos.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;

import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import pe.edu.upeu.objetosperdidos.entity.EstadoEstudiante;
import pe.edu.upeu.objetosperdidos.entity.EstadoPersonal;
import pe.edu.upeu.objetosperdidos.entity.EstadoUsuario;
import pe.edu.upeu.objetosperdidos.entity.RolUsuario;
import pe.edu.upeu.objetosperdidos.entity.Usuario;

import pe.edu.upeu.objetosperdidos.repository.EstudianteRepository;
import pe.edu.upeu.objetosperdidos.repository.PersonalRepository;
import pe.edu.upeu.objetosperdidos.repository.UsuarioRepository;

import pe.edu.upeu.objetosperdidos.security.IdentificadorLogin;
import pe.edu.upeu.objetosperdidos.security.UsuarioPrincipal;

import java.util.Optional;

@Configuration
public class ApplicationConfig {

    private final UsuarioRepository usuarioRepository;
    private final PersonalRepository personalRepository;
    private final EstudianteRepository estudianteRepository;

    public ApplicationConfig(
            UsuarioRepository usuarioRepository,
            PersonalRepository personalRepository,
            EstudianteRepository estudianteRepository) {

        this.usuarioRepository = usuarioRepository;
        this.personalRepository = personalRepository;
        this.estudianteRepository = estudianteRepository;
    }

    @Bean
    public UserDetailsService userDetailsService() {

        return identificador -> {

            final String[] partes;

            try {
                partes = IdentificadorLogin.separar(identificador);
            } catch (IllegalArgumentException ex) {
                throw new UsernameNotFoundException(
                        "Credenciales invalidas"
                );
            }

            String nombreUsuario = IdentificadorLogin.construir(
                    partes[0],
                    partes[1]
            );

            // Primero: comprobar si existe cuenta en USUARIO.
            Optional<Usuario> cuenta = usuarioRepository
                    .findByPersonaTipoDocumentoAndPersonaDocumentoIdentidad(
                            partes[0],
                            partes[1]
                    );

            if (cuenta.isPresent()) {

                Usuario usuario = cuenta.get();

                RolUsuario rol = usuario.getRol();

                boolean habilitado =
                        usuario.getEstado() == EstadoUsuario.ACTIVO;

                // ADMIN y PERSONAL:
                // conservar la validacion de personal activo.
                if (rol == RolUsuario.ADMIN
                        || rol == RolUsuario.PERSONAL) {

                    habilitado = habilitado
                            && personalRepository
                            .findById(usuario.getIdPersona())
                            .map(personal ->
                                    personal.getEstado()
                                            == EstadoPersonal.ACTIVO
                            )
                            .orElse(false);
                }

                // ESTUDIANTE con cuenta en USUARIO:
                // respetar el estado de ambas entidades.
                if (rol == RolUsuario.ESTUDIANTE) {

                    habilitado = habilitado
                            && estudianteRepository
                            .findById(usuario.getIdPersona())
                            .map(estudiante ->
                                    estudiante.getEstado()
                                            == EstadoEstudiante.ACTIVO
                            )
                            .orElse(false);

                    // No permitir eludir las reglas del personal.
                    habilitado = habilitado
                            && !personalRepository.existsById(
                            usuario.getIdPersona()
                    );
                }

                return new UsuarioPrincipal(
                        usuario.getIdPersona(),
                        nombreUsuario,
                        usuario.getPassword(),
                        rol,
                        habilitado
                );
            }

            // Segundo: si no existe cuenta en USUARIO,
            // reconocer exclusivamente a estudiantes activos.
            var estudiante = estudianteRepository
                    .findByPersonaTipoDocumentoAndPersonaDocumentoIdentidad(
                            partes[0],
                            partes[1]
                    )
                    .orElseThrow(() ->
                            new UsernameNotFoundException(
                                    "Credenciales invalidas"
                            )
                    );

            if (estudiante.getEstado() != EstadoEstudiante.ACTIVO) {
                throw new UsernameNotFoundException(
                        "Credenciales invalidas"
                );
            }

            // Una persona que figura como personal
            // no puede utilizar el acceso sin contrasena.
            if (personalRepository.existsById(
                    estudiante.getIdPersona())) {

                throw new UsernameNotFoundException(
                        "Credenciales invalidas"
                );
            }

            // Estudiante sin cuenta en USUARIO.
            return new UsuarioPrincipal(
                    estudiante.getIdPersona(),
                    nombreUsuario,
                    "",
                    RolUsuario.ESTUDIANTE,
                    true
            );
        };
    }

    @Bean
    public AuthenticationProvider authenticationProvider() {

        DaoAuthenticationProvider provider =
                new DaoAuthenticationProvider();

        provider.setUserDetailsService(userDetailsService());
        provider.setPasswordEncoder(passwordEncoder());

        return provider;
    }

    @Bean
    public AuthenticationManager authenticationManager(
            AuthenticationConfiguration config) throws Exception {

        return config.getAuthenticationManager();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder(12);
    }
}
