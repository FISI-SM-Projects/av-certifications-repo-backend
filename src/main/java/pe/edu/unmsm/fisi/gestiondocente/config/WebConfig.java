package pe.edu.unmsm.fisi.gestiondocente.config;

import java.util.Arrays;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.lang.NonNull;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    private final String[] allowedOrigins;

    public WebConfig(@Value("${app.cors.allowed-origins:http://localhost:3000,http://localhost:3001}")
            String allowedOrigins) {
        this.allowedOrigins = parseAllowedOrigins(allowedOrigins);
    }

    @Override
    public void addCorsMappings(@NonNull CorsRegistry registry) {
        registry.addMapping("/api/**")
                .allowedOrigins(allowedOrigins)
                .allowedMethods("GET", "POST")
                .allowedHeaders("*");
    }

    private String[] parseAllowedOrigins(String configuredOrigins) {
        if (configuredOrigins == null || configuredOrigins.trim().isEmpty()) {
            return new String[] { "http://localhost:3000", "http://localhost:3001" };
        }

        String[] origins = Arrays.stream(configuredOrigins.split(","))
                .map(String::trim)
                .filter(origin -> !origin.isEmpty())
                .toArray(String[]::new);

        if (origins.length == 0) {
            return new String[] { "http://localhost:3000", "http://localhost:3001" };
        }

        return origins;
    }
}
