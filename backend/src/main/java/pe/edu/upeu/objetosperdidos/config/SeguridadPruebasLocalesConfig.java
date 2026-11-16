package pe.edu.upeu.objetosperdidos.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;

/**
 * SOLO PARA PRUEBAS LOCALES CON SWAGGER.
 * Requiere perfil 'local' y application-local.yaml (127.0.0.1).
 * NO usar con usuarios reales, en producción, ni exponiendo el puerto a la red.
 * Más adelante se sustituirá por JWT + autorización por roles.
 */
@Configuration
@Profile("local")
public class SeguridadPruebasLocalesConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/api/v1/categorias", "/api/v1/categorias/**").permitAll()
                .anyRequest().authenticated())
            // Unicamente para los endpoints demo de categorías en localhost.
            .csrf(csrf -> csrf.ignoringRequestMatchers(
                    "/api/v1/categorias", "/api/v1/categorias/**"))
            .formLogin(Customizer.withDefaults())
            .httpBasic(Customizer.withDefaults());
        return http.build();
    }
}
