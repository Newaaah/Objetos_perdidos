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
