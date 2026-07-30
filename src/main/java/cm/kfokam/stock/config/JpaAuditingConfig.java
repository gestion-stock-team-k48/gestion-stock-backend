package cm.kfokam.stock.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

// Kept off GestionStockApplication: there, @EnableJpaAuditing leaks into @WebMvcTest slices
// (no JPA metamodel loaded) and breaks them with "JPA metamodel must not be empty".
@Configuration
@EnableJpaAuditing(auditorAwareRef = "auditorAware")
public class JpaAuditingConfig {
}
