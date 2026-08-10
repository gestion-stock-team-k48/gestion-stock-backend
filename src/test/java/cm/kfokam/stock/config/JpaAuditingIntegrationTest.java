package cm.kfokam.stock.config;

import cm.kfokam.stock.entreprise.model.Entreprise;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Test d'intégration bout-en-bout (contexte Spring complet + base réelle) qui prouve que
 * {@link cm.kfokam.stock.common.entity.AbstractEntity} se remplit correctement via
 * {@code @EnableJpaAuditing} + {@link ApplicationAuditorAware}, sans violation de contrainte
 * NOT NULL sur {@code created_at}/{@code created_by} (colonnes non nullables, voir
 * {@code V1__init_schema.sql}).
 */
@SpringBootTest
@Transactional
class JpaAuditingIntegrationTest {

    @PersistenceContext
    private EntityManager entityManager;

    @AfterEach
    void clearSecurityContext() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void fillsCreatedAndUpdatedFieldsWithSystemFallbackWhenNoAuthenticationIsPresent() {
        SecurityContextHolder.clearContext();

        Entreprise entreprise = newEntreprise();

        // Ne doit pas lever d'exception de violation de contrainte NOT NULL sur created_at/created_by.
        entityManager.persist(entreprise);
        entityManager.flush();

        assertThat(entreprise.getCreatedAt()).isNotNull();
        assertThat(entreprise.getCreatedBy()).isEqualTo("SYSTEM");
        assertThat(entreprise.getUpdatedAt()).isNotNull();
        assertThat(entreprise.getUpdatedBy()).isEqualTo("SYSTEM");
    }

    @Test
    void fillsCreatedByWithAuthenticatedUserEmailFromSecurityContext() {
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken("franckytchana08@gmail.com", null, List.of()));

        Entreprise entreprise = newEntreprise();
        entityManager.persist(entreprise);
        entityManager.flush();

        assertThat(entreprise.getCreatedBy()).isEqualTo("franckytchana08@gmail.com");
        assertThat(entreprise.getUpdatedBy()).isEqualTo("franckytchana08@gmail.com");
    }

    @Test
    void updatesOnlyLastModifiedFieldsOnSubsequentChangeKeepingCreationFieldsStable() {
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken("creator@kfokam.cm", null, List.of()));

        Entreprise entreprise = newEntreprise();
        entityManager.persist(entreprise);
        entityManager.flush();

        LocalDateTime createdAt = entreprise.getCreatedAt();
        String createdBy = entreprise.getCreatedBy();

        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken("editor@kfokam.cm", null, List.of()));
        entreprise.setDescription("Mise à jour de test");
        entityManager.flush();

        assertThat(entreprise.getCreatedAt()).isEqualTo(createdAt);
        assertThat(entreprise.getCreatedBy()).isEqualTo(createdBy);
        assertThat(entreprise.getUpdatedBy()).isEqualTo("editor@kfokam.cm");
        assertThat(entreprise.getUpdatedAt()).isAfterOrEqualTo(createdAt);
    }

    private Entreprise newEntreprise() {
        String unique = UUID.randomUUID().toString();
        // code_fiscal est VARCHAR(30) (V1__init_schema.sql) : on ne garde que les 8 premiers
        // caractères de l'UUID pour rester sous la limite tout en garantissant l'unicité.
        String shortUnique = unique.substring(0, 8);
        return Entreprise.builder()
                .nom("Audit Test SARL")
                .codeFiscal("AUDIT-" + shortUnique)
                .email("audit-" + unique + "@test.cm")
                .build();
    }
}
