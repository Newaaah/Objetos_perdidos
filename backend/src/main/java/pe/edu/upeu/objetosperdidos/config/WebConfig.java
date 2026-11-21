package pe.edu.upeu.objetosperdidos.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    // Las imagenes NO se publican como carpeta estatica (/images/**):
    // fotos de entregas y reclamos son privadas, por eso se sirven
    // desde los controladores, que revisan quien las pide.

    // Configuracion para CORS (Conexion con Angular)
    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**")
                .allowedOrigins("http://localhost:4200") // El puerto de Angular
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS", "PATCH")
                .allowedHeaders("*")
                .allowCredentials(true);
    }
}
