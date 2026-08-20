package cm.kfokam.stock.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

/**
 * Configuration CORS.
 * <p>
 * Le {@link CorsConfigurationSource} produit ici est branché dans
 * {@link cm.kfokam.stock.auth.SecurityConfig} via {@code HttpSecurity.cors(...)}, ce qui
 * enregistre le {@code CorsFilter} de Spring Security en tête de la chaîne de filtres : les
 * requêtes de pre-flight ({@code OPTIONS}) sont ainsi traitées avant toute vérification
 * d'authentification/autorisation et ne remontent jamais un 401/403.
 */
@Configuration
public class CorsConfig {

    /**
     * Origines autorisées, lues de la configuration.
     *
     * Les valeurs par défaut sont les ports des serveurs de développement — Angular CLI,
     * Create React App, Vite — pour qu'une machine de développeur n'ait rien à exporter.
     *
     * Tout déploiement doit renseigner la sienne, y compris quand l'interface et l'API
     * partagent une origine derrière un reverse proxy : le navigateur joint un en-tête
     * `Origin` à toute requête qui n'est ni `GET` ni `HEAD`, même en même origine, et Spring
     * la traite alors comme une requête CORS. Une origine absente de cette liste se voit
     * répondre « Invalid CORS request » en 403 — ce qui ne se remarque pas en interrogeant
     * l'API avec `curl`, qui n'envoie pas cet en-tête.
     */
    private final List<String> allowedOrigins;

    public CorsConfig(
            @Value("${application.cors.allowed-origins}") List<String> allowedOrigins) {
        this.allowedOrigins = allowedOrigins;
    }

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
        configuration.setAllowedOrigins(allowedOrigins);
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
