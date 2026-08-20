package cm.kfokam.stock.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

/**
 * Configuration CORS dédiée aux frontends Angular et React en développement.
 * <p>
 * Le {@link CorsConfigurationSource} produit ici est branché dans
 * {@link cm.kfokam.stock.auth.SecurityConfig} via {@code HttpSecurity.cors(...)}, ce qui
 * enregistre le {@code CorsFilter} de Spring Security en tête de la chaîne de filtres : les
 * requêtes de pre-flight ({@code OPTIONS}) sont ainsi traitées avant toute vérification
 * d'authentification/autorisation et ne remontent jamais un 401/403.
 */
@Configuration
public class CorsConfig {

    // Ports par défaut des serveurs de développement : Angular CLI (4200),
    // Create React App (3000) et Vite (5173).
    private static final List<String> ALLOWED_ORIGINS = List.of(
            "http://localhost:4200",
            "http://localhost:3000",
            "http://localhost:5173"
    );

    private static final List<String> ALLOWED_METHODS =
            List.of("GET", "POST", "PUT", "DELETE", "PATCH", "OPTIONS");

    private static final List<String> ALLOWED_HEADERS =
            List.of("Authorization", "Content-Type", "X-Requested-With", "Accept");

    // En-têtes exposés en lecture au code JavaScript (HttpClient/Axios/Fetch) : le CORS
    // "simple" ne rend visibles que quelques en-têtes par défaut, donc Authorization et les
    // métadonnées de pagination doivent être déclarés explicitement pour rester lisibles.
    private static final List<String> EXPOSED_HEADERS = List.of(
            "Authorization",
            "X-Total-Count",
            "X-Total-Pages",
            "X-Page-Number",
            "X-Page-Size"
    );

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(ALLOWED_ORIGINS);
        configuration.setAllowedMethods(ALLOWED_METHODS);
        configuration.setAllowedHeaders(ALLOWED_HEADERS);
        configuration.setExposedHeaders(EXPOSED_HEADERS);
        configuration.setAllowCredentials(true);
        configuration.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
}
