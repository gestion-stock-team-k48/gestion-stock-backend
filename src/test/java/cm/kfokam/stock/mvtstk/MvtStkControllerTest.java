package cm.kfokam.stock.mvtstk;

import cm.kfokam.stock.exception.EntityNotFoundException;
import cm.kfokam.stock.mvtstk.dto.AlerteStockResponse;
import cm.kfokam.stock.mvtstk.dto.MvtStkCorrectionRequest;
import cm.kfokam.stock.mvtstk.dto.MvtStkRequest;
import cm.kfokam.stock.mvtstk.dto.MvtStkResponse;
import cm.kfokam.stock.mvtstk.model.SourceMvtStk;
import cm.kfokam.stock.mvtstk.model.TypeMvtStk;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(MvtStkController.class)
@AutoConfigureMockMvc(addFilters = false)
class MvtStkControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private MvtStkService mvtStkService;

    private MvtStkRequest validRequest() {
        return new MvtStkRequest(1L, new BigDecimal("5"), SourceMvtStk.COMMANDE_FOURNISSEUR);
    }

    private MvtStkCorrectionRequest validCorrectionRequest() {
        return new MvtStkCorrectionRequest(1L, new BigDecimal("5"), "Casse en entrepôt");
    }

    private MvtStkResponse sampleResponse(TypeMvtStk type) {
        return new MvtStkResponse(1L, Instant.parse("2026-07-27T10:00:00Z"), new BigDecimal("5"),
                1L, "Ordinateur portable", type, SourceMvtStk.COMMANDE_FOURNISSEUR, null, 1L);
    }

    @Test
    void entreeStock_shouldReturn201_whenValidRequest() throws Exception {
        MvtStkRequest request = validRequest();
        MvtStkResponse response = sampleResponse(TypeMvtStk.ENTREE);

        when(mvtStkService.entreeStock(request)).thenReturn(response);

        mockMvc.perform(post("/mouvements-stock/entree")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.typeMvt").value("ENTREE"))
                .andExpect(jsonPath("$.articleId").value(1L));
    }

    @Test
    void entreeStock_shouldReturn400_whenQuantiteIsNotPositive() throws Exception {
        MvtStkRequest invalidRequest = new MvtStkRequest(1L, new BigDecimal("-1"), SourceMvtStk.COMMANDE_FOURNISSEUR);

        mockMvc.perform(post("/mouvements-stock/entree")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest());

        verify(mvtStkService, never()).entreeStock(any());
    }

    @Test
    void entreeStock_shouldReturn400_whenArticleIdIsMissing() throws Exception {
        MvtStkRequest invalidRequest = new MvtStkRequest(null, new BigDecimal("5"), SourceMvtStk.COMMANDE_FOURNISSEUR);

        mockMvc.perform(post("/mouvements-stock/entree")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest());

        verify(mvtStkService, never()).entreeStock(any());
    }

    @Test
    void entreeStock_shouldReturn404_whenArticleNotFound() throws Exception {
        MvtStkRequest request = validRequest();

        when(mvtStkService.entreeStock(request))
                .thenThrow(new EntityNotFoundException("Article introuvable avec l'id : 1"));

        mockMvc.perform(post("/mouvements-stock/entree")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound());
    }

    @Test
    void sortieStock_shouldReturn201_whenValidRequest() throws Exception {
        MvtStkRequest request = validRequest();
        MvtStkResponse response = sampleResponse(TypeMvtStk.SORTIE);

        when(mvtStkService.sortieStock(request)).thenReturn(response);

        mockMvc.perform(post("/mouvements-stock/sortie")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.typeMvt").value("SORTIE"));
    }

    @Test
    void correctionPositive_shouldReturn201_whenValidRequest() throws Exception {
        MvtStkCorrectionRequest request = validCorrectionRequest();
        MvtStkResponse response = sampleResponse(TypeMvtStk.CORRECTION_POS);

        when(mvtStkService.correctionStockPos(request)).thenReturn(response);

        mockMvc.perform(post("/mouvements-stock/correction-positive")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.typeMvt").value("CORRECTION_POS"));
    }

    @Test
    void correctionPositive_shouldReturn400_whenMotifIsBlank() throws Exception {
        MvtStkCorrectionRequest invalidRequest = new MvtStkCorrectionRequest(1L, new BigDecimal("5"), " ");

        mockMvc.perform(post("/mouvements-stock/correction-positive")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest());

        verify(mvtStkService, never()).correctionStockPos(any());
    }

    @Test
    void correctionNegative_shouldReturn201_whenValidRequest() throws Exception {
        MvtStkCorrectionRequest request = validCorrectionRequest();
        MvtStkResponse response = sampleResponse(TypeMvtStk.CORRECTION_NEG);

        when(mvtStkService.correctionStockNeg(request)).thenReturn(response);

        mockMvc.perform(post("/mouvements-stock/correction-negative")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.typeMvt").value("CORRECTION_NEG"));
    }

    @Test
    void correctionNegative_shouldReturn400_whenMotifIsBlank() throws Exception {
        MvtStkCorrectionRequest invalidRequest = new MvtStkCorrectionRequest(1L, new BigDecimal("5"), "");

        mockMvc.perform(post("/mouvements-stock/correction-negative")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest());

        verify(mvtStkService, never()).correctionStockNeg(any());
    }

    @Test
    void mvtStkArticle_shouldReturn200WithList() throws Exception {
        List<MvtStkResponse> responses = List.of(sampleResponse(TypeMvtStk.ENTREE), sampleResponse(TypeMvtStk.SORTIE));
        Page<MvtStkResponse> page = new PageImpl<>(responses);
        when(mvtStkService.mvtStkArticle(eq(1L), any(Pageable.class))).thenReturn(page);

        mockMvc.perform(get("/mouvements-stock/article/{idArticle}", 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(2))
                .andExpect(jsonPath("$.content[0].typeMvt").value("ENTREE"))
                .andExpect(jsonPath("$.content[1].typeMvt").value("SORTIE"));
    }

    @Test
    void mvtStkArticle_shouldReturn404_whenArticleNotFound() throws Exception {
        when(mvtStkService.mvtStkArticle(eq(99L), any(Pageable.class)))
                .thenThrow(new EntityNotFoundException("Article introuvable avec l'id : 99"));

        mockMvc.perform(get("/mouvements-stock/article/{idArticle}", 99L))
                .andExpect(status().isNotFound());
    }

    @Test
    void stockReelArticle_shouldReturn200WithValue() throws Exception {
        when(mvtStkService.stockReelArticle(1L)).thenReturn(new BigDecimal("8.00"));

        mockMvc.perform(get("/mouvements-stock/article/{idArticle}/stock-reel", 1L))
                .andExpect(status().isOk())
                .andExpect(content().string("8.00"));
    }

    @Test
    void stockReelArticle_shouldReturn404_whenArticleNotFound() throws Exception {
        when(mvtStkService.stockReelArticle(99L))
                .thenThrow(new EntityNotFoundException("Article introuvable avec l'id : 99"));

        mockMvc.perform(get("/mouvements-stock/article/{idArticle}/stock-reel", 99L))
                .andExpect(status().isNotFound());
    }

    @Test
    void alertesStock_shouldReturn200WithList() throws Exception {
        List<AlerteStockResponse> alertes = List.of(
                new AlerteStockResponse(1L, "ART-01", "Ordinateur portable", new BigDecimal("2"), new BigDecimal("5"))
        );
        when(mvtStkService.articlesEnAlerte()).thenReturn(alertes);

        mockMvc.perform(get("/mouvements-stock/alertes-stock"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].articleId").value(1L))
                .andExpect(jsonPath("$[0].quantiteStock").value(2))
                .andExpect(jsonPath("$[0].seuilMinimum").value(5));
    }
}