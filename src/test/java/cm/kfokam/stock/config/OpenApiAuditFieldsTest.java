package cm.kfokam.stock.config;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;

import java.util.List;
import java.util.stream.StreamSupport;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Vérifie, à partir du document OpenAPI réellement généré par springdoc (endpoint
 * {@code /v3/api-docs}), que les champs d'audit (createdAt, updatedAt, createdBy, updatedBy) :
 * <ul>
 *     <li>apparaissent dans les schémas des DTOs de réponse, marqués {@code readOnly: true} ;</li>
 *     <li>sont absents des schémas des DTOs de requête correspondants.</li>
 * </ul>
 * C'est le seul moyen fiable de prouver la visibilité Swagger/OpenAPI : les annotations
 * {@code @Schema} ne garantissent rien tant qu'on ne relit pas la documentation produite.
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class OpenApiAuditFieldsTest {

    private static final List<String> AUDIT_FIELDS = List.of("createdAt", "updatedAt", "createdBy", "updatedBy");

    @LocalServerPort
    private int port;

    @Autowired
    private TestRestTemplate restTemplate;

    private JsonNode schemas() throws Exception {
        String body = restTemplate.getForObject("http://localhost:" + port + "/api/v1/v3/api-docs", String.class);
        return new ObjectMapper().readTree(body).path("components").path("schemas");
    }

    @Test
    void responseDtoSchemas_exposeAuditFieldsAsReadOnly() throws Exception {
        JsonNode schemas = schemas();
        List<String> responseDtos = List.of(
                "ArticleResponse", "CategoryResponse", "ClientResponse", "FournisseurResponse",
                "EntrepriseResponse", "UtilisateurResponse", "VenteResponse", "LigneVenteResponse",
                "CommandeClientResponse", "LigneCommandeClientResponse",
                "CommandeFournisseurResponse", "LigneCommandeFournisseurResponse", "MvtStkResponse"
        );

        for (String dto : responseDtos) {
            JsonNode properties = schemas.path(dto).path("properties");
            assertThat(properties.isMissingNode())
                    .as("Le schéma OpenAPI de %s doit exister", dto)
                    .isFalse();

            for (String field : AUDIT_FIELDS) {
                JsonNode fieldSchema = properties.path(field);
                assertThat(fieldSchema.isMissingNode())
                        .as("%s.%s doit être présent dans le schéma OpenAPI", dto, field)
                        .isFalse();
                assertThat(fieldSchema.path("readOnly").asBoolean(false))
                        .as("%s.%s doit être marqué readOnly", dto, field)
                        .isTrue();
            }
        }
    }

    @Test
    void requestDtoSchemas_neverExposeAuditFields() throws Exception {
        JsonNode schemas = schemas();
        List<String> requestDtos = List.of(
                "ArticleRequest", "CategoryRequest", "ClientRequest", "FournisseurRequest",
                "EntrepriseRequest", "UtilisateurRequest", "CommandeClientRequest", "CommandeFournisseurRequest"
        );

        for (String dto : requestDtos) {
            JsonNode properties = schemas.path(dto).path("properties");
            assertThat(properties.isMissingNode())
                    .as("Le schéma OpenAPI de %s doit exister", dto)
                    .isFalse();

            List<String> fieldNames = StreamSupport.stream(
                    java.util.Spliterators.spliteratorUnknownSize(properties.fieldNames(), 0), false).toList();

            assertThat(fieldNames)
                    .as("%s ne doit exiger aucun champ d'audit en entrée", dto)
                    .doesNotContainAnyElementsOf(AUDIT_FIELDS);
        }
    }
}
