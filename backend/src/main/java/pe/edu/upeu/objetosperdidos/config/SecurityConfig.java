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
                // Documentacion Swagger
                .requestMatchers(
                        "/swagger-ui.html",
                        "/swagger-ui/**",
                        "/v3/api-docs",
                        "/v3/api-docs/**",
                        "/error"
                ).permitAll()

                // Login existente
                .requestMatchers(HttpMethod.POST, "/api/v1/auth/login").permitAll()

                // Lectura publica de catalogos (tambien carreras)
                .requestMatchers(
                        HttpMethod.GET,
                        "/api/v1/categorias", "/api/v1/categorias/**",
                        "/api/v1/ubicaciones", "/api/v1/ubicaciones/**",
                        "/api/v1/areas", "/api/v1/areas/**",
                        "/api/v1/carreras", "/api/v1/carreras/**"
                ).permitAll()

                // Cambios en catalogos: unicamente ADMIN
                .requestMatchers(
                        "/api/v1/categorias", "/api/v1/categorias/**",
                        "/api/v1/ubicaciones", "/api/v1/ubicaciones/**",
                        "/api/v1/areas", "/api/v1/areas/**",
                        "/api/v1/carreras", "/api/v1/carreras/**"
                ).hasAuthority("ADMIN")

                // Reportes institucionales (sin cambios)
                .requestMatchers("/api/v1/reportes/**")
                    .hasAnyAuthority("PERSONAL", "ADMIN")

                // Denegar endpoints futuros hasta definir permisos
                .anyRequest().denyAll()
            )
            .sessionManagement(session -> session.sessionCreationPolicy(
                    SessionCreationPolicy.STATELESS))
            .authenticationProvider(authenticationProvider)
            .addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class)
            .exceptionHandling(exception -> exception
                    .authenticationEntryPoint(authenticationEntryPoint)
                    .accessDeniedHandler(accessDeniedHandler)
            );
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
