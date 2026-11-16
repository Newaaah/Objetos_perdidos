
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

import pe.edu.upeu.objetosperdidos.entity.EstadoPersonal;
import pe.edu.upeu.objetosperdidos.entity.EstadoUsuario;
import pe.edu.upeu.objetosperdidos.entity.RolUsuario;

import pe.edu.upeu.objetosperdidos.repository.PersonalRepository;
import pe.edu.upeu.objetosperdidos.repository.UsuarioRepository;

import pe.edu.upeu.objetosperdidos.security.IdentificadorLogin;
import pe.edu.upeu.objetosperdidos.security.UsuarioPrincipal;

@Configuration
public class ApplicationConfig {

    private final UsuarioRepository usuarioRepository;
    private final PersonalRepository personalRepository;

    public ApplicationConfig(
            UsuarioRepository usuarioRepository,
            PersonalRepository personalRepository) {

        this.usuarioRepository = usuarioRepository;
        this.personalRepository = personalRepository;
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

            var usuario = usuarioRepository
                    .findByPersonaTipoDocumentoAndPersonaDocumentoIdentidad(
                            partes[0],
                            partes[1]
                    )
                    .orElseThrow(() ->
                            new UsernameNotFoundException(
                                    "Credenciales invalidas"
                            )
                    );

            boolean habilitado =
                    usuario.getEstado() == EstadoUsuario.ACTIVO;

            if (usuario.getRol() == RolUsuario.PERSONAL
                    || usuario.getRol() == RolUsuario.ADMIN) {

                habilitado = habilitado
                        && personalRepository
                        .findById(usuario.getIdPersona())
                        .map(personal ->
                                personal.getEstado()
                                        == EstadoPersonal.ACTIVO
                        )
                        .orElse(false);
            }

            return new UsuarioPrincipal(
                    usuario.getIdPersona(),
                    IdentificadorLogin.construir(
                            partes[0],
                            partes[1]
                    ),
                    usuario.getPassword(),
                    usuario.getRol(),
                    habilitado
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
