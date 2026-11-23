package pe.edu.upeu.objetosperdidos.config;

import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import pe.edu.upeu.objetosperdidos.exception.CustomAccessDeniedHandler;
import pe.edu.upeu.objetosperdidos.exception.CustomAuthenticationEntryPoint;
import pe.edu.upeu.objetosperdidos.security.JwtAuthenticationFilter;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtFilter;
    private final AuthenticationProvider authenticationProvider;
    private final CustomAccessDeniedHandler accessDeniedHandler;
    private final CustomAuthenticationEntryPoint authenticationEntryPoint;

    public SecurityConfig(
            JwtAuthenticationFilter jwtFilter,
            AuthenticationProvider authenticationProvider,
            CustomAccessDeniedHandler accessDeniedHandler,
            CustomAuthenticationEntryPoint authenticationEntryPoint) {
        this.jwtFilter = jwtFilter;
        this.authenticationProvider = authenticationProvider;
        this.accessDeniedHandler = accessDeniedHandler;
        this.authenticationEntryPoint = authenticationEntryPoint;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .cors(Customizer.withDefaults())
                .csrf(AbstractHttpConfigurer::disable)
                .formLogin(AbstractHttpConfigurer::disable)
                .httpBasic(AbstractHttpConfigurer::disable)
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(
                                "/swagger-ui.html", "/swagger-ui/**",
                                "/v3/api-docs", "/v3/api-docs/**", "/error"
                        ).permitAll()

                        .requestMatchers(HttpMethod.POST, "/api/v1/auth/login").permitAll()

                        // Catalogo publico: objetos en custodia y entregados, sin login.
                        .requestMatchers(HttpMethod.GET, "/api/v1/publico/**").permitAll()

                        .requestMatchers(HttpMethod.GET,
                                "/api/v1/categorias", "/api/v1/categorias/**",
                                "/api/v1/ubicaciones", "/api/v1/ubicaciones/**",
                                "/api/v1/areas", "/api/v1/areas/**",
                                "/api/v1/carreras", "/api/v1/carreras/**"
                        ).permitAll()

                        .requestMatchers(
                                "/api/v1/categorias", "/api/v1/categorias/**",
                                "/api/v1/ubicaciones", "/api/v1/ubicaciones/**",
                                "/api/v1/areas", "/api/v1/areas/**",
                                "/api/v1/carreras", "/api/v1/carreras/**"
                        ).hasAuthority("ADMIN")

                        // Registro de perdidas y consulta privada de las propias.
                        .requestMatchers(HttpMethod.POST, "/api/v1/perdidas")
                        .hasAuthority("ESTUDIANTE")
                        .requestMatchers(HttpMethod.GET, "/api/v1/perdidas/mias")
                        .hasAuthority("ESTUDIANTE")
                        .requestMatchers(HttpMethod.PATCH, "/api/v1/perdidas/*/recuperado")
                        .hasAuthority("ESTUDIANTE")
                        .requestMatchers(HttpMethod.GET, "/api/v1/perdidas/abiertas")
                        .hasAnyAuthority("PERSONAL", "ADMIN")

                        // Registro de hallazgos con imagen y consulta privada.
                        .requestMatchers(HttpMethod.POST, "/api/v1/hallazgos")
                        .hasAuthority("ESTUDIANTE")
                        .requestMatchers(HttpMethod.GET, "/api/v1/hallazgos/mios")
                        .hasAuthority("ESTUDIANTE")

                        // La revision y las fotografias de revision NO son publicas.
                        .requestMatchers(HttpMethod.GET,
                                "/api/v1/hallazgos/revision/pendientes",
                                "/api/v1/hallazgos/revision/*/imagen"
                        ).hasAnyAuthority("PERSONAL", "ADMIN")
                        .requestMatchers(HttpMethod.PATCH,
                                "/api/v1/hallazgos/revision/*/decision"
                        ).hasAnyAuthority("PERSONAL", "ADMIN")

                        // Recepcion fisica: solo PERSONAL/ADMIN; nunca ESTUDIANTE.
                        .requestMatchers(HttpMethod.GET,
                                "/api/v1/custodias/recepcion/pendientes",
                                "/api/v1/custodias/recepcion/*",
                                "/api/v1/custodias/recepcion/*/imagen"
                        ).hasAnyAuthority("PERSONAL", "ADMIN")
                        .requestMatchers(HttpMethod.POST,
                                "/api/v1/custodias/recepcion",
                                "/api/v1/custodias/recepcion/con-hallazgo",
                                "/api/v1/custodias/recepcion/directa"
                        ).hasAnyAuthority("PERSONAL", "ADMIN")

                        // Quien recibio un objeto puede ver solo su propio historial.
                        .requestMatchers(HttpMethod.GET, "/api/v1/entregas/mias")
                        .hasAuthority("ESTUDIANTE")

                        // Entregas: solo PERSONAL y ADMIN.
                        .requestMatchers(HttpMethod.POST, "/api/v1/entregas")
                        .hasAnyAuthority("PERSONAL", "ADMIN")
                        .requestMatchers(HttpMethod.GET,
                                "/api/v1/entregas/custodias/*/estado",
                                "/api/v1/entregas/*",
                                "/api/v1/entregas/*/evidencia"
                        ).hasAnyAuthority("PERSONAL", "ADMIN")

                        // Sugerencias privadas: solo para el titular del reporte autenticado.
                        .requestMatchers(HttpMethod.GET,
                                "/api/v1/coincidencias/sugerencias/mias/*")
                        .hasAuthority("ESTUDIANTE")

                        // Revision formal: datos internos y decision solo PERSONAL/ADMIN.
                        .requestMatchers(HttpMethod.GET, "/api/v1/coincidencias/revision/**")
                        .hasAnyAuthority("PERSONAL", "ADMIN")
                        .requestMatchers(HttpMethod.POST, "/api/v1/coincidencias/revision")
                        .hasAnyAuthority("PERSONAL", "ADMIN")

                        // Un administrador verificado aprovisiona estudiantes con BCrypt.
                        .requestMatchers(HttpMethod.POST, "/api/v1/admin/estudiantes/*/clave")
                        .hasAuthority("ADMIN")
                        .requestMatchers(HttpMethod.PATCH, "/api/v1/cuenta/clave")
                        .authenticated()
                        // NUEVO: mis datos (el ID sale del token, nunca del cliente)
                        .requestMatchers(HttpMethod.GET, "/api/v1/cuenta/perfil")
                        .authenticated()

                        // Reclamos privados: estudiante presenta y sigue solo los propios.
                        .requestMatchers(HttpMethod.POST, "/api/v1/reclamos")
                        .hasAuthority("ESTUDIANTE")
                        .requestMatchers(HttpMethod.GET, "/api/v1/reclamos", "/api/v1/reclamos/pendientes")
                        .hasAnyAuthority("PERSONAL", "ADMIN")
                        .requestMatchers(HttpMethod.GET, "/api/v1/reclamos/mios", "/api/v1/reclamos/*")
                        .hasAuthority("ESTUDIANTE")
                        // Evidencia: el servicio verifica que sea el dueno o personal activo.
                        .requestMatchers(HttpMethod.GET, "/api/v1/reclamos/*/evidencia")
                        .authenticated()
                        .requestMatchers(HttpMethod.PATCH, "/api/v1/reclamos/*/revision")
                        .hasAnyAuthority("PERSONAL", "ADMIN")

                        // Incidencias de custodias: solo personal autorizado.
                        .requestMatchers("/api/v1/incidencias", "/api/v1/incidencias/**")
                        .hasAnyAuthority("PERSONAL", "ADMIN")

                        // La bandeja pertenece al sujeto autenticado; el ID no viene del cliente.
                        .requestMatchers(HttpMethod.GET,
                                "/api/v1/notificaciones/mias", "/api/v1/notificaciones/no-leidas/conteo")
                        .authenticated()
                        .requestMatchers(HttpMethod.PATCH, "/api/v1/notificaciones/*/leida")
                        .authenticated()

                        // Busqueda de personas por DNI o codigo (recepcion, entrega y claves)
                        .requestMatchers(HttpMethod.GET, "/api/v1/personas/buscar")
                        .hasAnyAuthority("PERSONAL", "ADMIN")
                        .requestMatchers("/api/v1/reportes/**")
                        .hasAnyAuthority("PERSONAL", "ADMIN")

                        .anyRequest().denyAll()
                )
                .sessionManagement(session -> session.sessionCreationPolicy(
                        SessionCreationPolicy.STATELESS))
                .authenticationProvider(authenticationProvider)
                .addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class)
                .exceptionHandling(exception -> exception
                        .authenticationEntryPoint(authenticationEntryPoint)
                        .accessDeniedHandler(accessDeniedHandler));
        return http.build();
    }

    @Bean
    public FilterRegistrationBean<JwtAuthenticationFilter>
    jwtFilterRegistration(JwtAuthenticationFilter filter) {
        FilterRegistrationBean<JwtAuthenticationFilter> registration =
                new FilterRegistrationBean<>(filter);
        registration.setEnabled(false);
        return registration;
    }
}