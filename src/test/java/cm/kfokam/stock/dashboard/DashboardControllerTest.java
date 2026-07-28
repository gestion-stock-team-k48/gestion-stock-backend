package cm.kfokam.stock.dashboard;

import cm.kfokam.stock.dashboard.dto.DashboardStatsResponse;
import cm.kfokam.stock.dashboard.dto.TopArticleVenduResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.List;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(DashboardController.class)
@AutoConfigureMockMvc(addFilters = false)
class DashboardControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private DashboardService dashboardService;

    @Test
    void statistiques_shouldReturn200WithStats() throws Exception {
        DashboardStatsResponse stats = new DashboardStatsResponse(
                new BigDecimal("560.00"), new BigDecimal("360.00"),
                2, 1, 1, 1,
                List.of(new TopArticleVenduResponse(10L, "Article 1", new BigDecimal("5")))
        );
        when(dashboardService.getStatistiques()).thenReturn(stats);

        mockMvc.perform(get("/api/dashboard/statistiques"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.chiffreAffairesTotal").value(560.00))
                .andExpect(jsonPath("$.chiffreAffairesMoisCourant").value(360.00))
                .andExpect(jsonPath("$.commandesClientEnCours").value(2))
                .andExpect(jsonPath("$.commandesClientLivrees").value(1))
                .andExpect(jsonPath("$.commandesFournisseurEnCours").value(1))
                .andExpect(jsonPath("$.commandesFournisseurLivrees").value(1))
                .andExpect(jsonPath("$.topArticlesVendus.length()").value(1))
                .andExpect(jsonPath("$.topArticlesVendus[0].articleId").value(10L))
                .andExpect(jsonPath("$.topArticlesVendus[0].designation").value("Article 1"));
    }
}
