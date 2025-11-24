package br.com.smartmesquitaapi.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Configuração do Spring MVC
 *
 * Nota: Rate limiting agora é feito via AOP (@RateLimit annotation)
 * ao invés de interceptors HTTP
 */
@Configuration
public class WebMvcConfig implements WebMvcConfigurer {

    // Rate limiting removido - agora usa @RateLimit annotation com AOP

    @Override
    public void addCorsMappings(CorsRegistry registry){
        registry.addMapping("/api/**")
                .allowedOrigins(
                        "http://localhost:3000",
                        "https://app.smartmesquita.com"
                )
                .allowedMethods("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS")
                .allowedHeaders("*")
                .allowCredentials(true)
                .maxAge(3600);
    }

}
