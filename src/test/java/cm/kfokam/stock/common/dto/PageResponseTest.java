package cm.kfokam.stock.common.dto;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class PageResponseTest {

    // Vérifie le contrat JSON attendu côté frontend (Angular/React) : les records Java exposent
    // leurs composants sous leur nom exact, y compris "isLast" — contrairement à une classe classique
    // dont l'accesseur isLast() serait normalisé en "last" par les conventions JavaBean de Jackson.
    @Test
    void from_shouldMapSpringDataPageToStableFrontendFriendlyJson() throws Exception {
        PageImpl<String> springPage = new PageImpl<>(List.of("a", "b"), PageRequest.of(0, 2), 5);

        PageResponse<String> response = PageResponse.from(springPage);

        assertThat(response.content()).containsExactly("a", "b");
        assertThat(response.pageNumber()).isZero();
        assertThat(response.pageSize()).isEqualTo(2);
        assertThat(response.totalElements()).isEqualTo(5);
        assertThat(response.totalPages()).isEqualTo(3);
        assertThat(response.isLast()).isFalse();

        String json = new ObjectMapper().writeValueAsString(response);

        assertThat(json).contains("\"content\":[\"a\",\"b\"]")
                .contains("\"pageNumber\":0")
                .contains("\"pageSize\":2")
                .contains("\"totalElements\":5")
                .contains("\"totalPages\":3")
                .contains("\"isLast\":false");
    }
}
