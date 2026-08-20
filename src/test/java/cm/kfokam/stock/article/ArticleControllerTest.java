package cm.kfokam.stock.article;

import cm.kfokam.stock.article.dto.ArticleRequest;
import cm.kfokam.stock.article.dto.ArticleResponse;
import cm.kfokam.stock.exception.DuplicateCodeException;
import cm.kfokam.stock.exception.EntityNotFoundException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.List;

import static org.mockito.Mockito.doThrow;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ArticleController.class)
@AutoConfigureMockMvc(addFilters = false)
class ArticleControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private ArticleService articleService;

    private ArticleRequest validRequest() {
        return new ArticleRequest(
                "ART-01",
                "Ordinateur portable",
                new BigDecimal("500.00"),
                new BigDecimal("19.25"),
                new BigDecimal("596.25"),
                "photo.png",
                new BigDecimal("5"),
                1L
        );
    }

    private ArticleResponse sampleResponse() {
        return new ArticleResponse(
                1L,
                "ART-01",
                "Ordinateur portable",
                new BigDecimal("500.00"),
                new BigDecimal("19.25"),
                new BigDecimal("596.25"),
                "photo.png",
                new BigDecimal("5"),
                1L,
                "Informatique",
                null, null, null, null
        );
    }

    @Test
    void create_shouldReturn201_whenValidRequest() throws Exception {
        ArticleRequest request = validRequest();
        ArticleResponse response = sampleResponse();

        when(articleService.create(request)).thenReturn(response);

        mockMvc.perform(post("/articles")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.code").value("ART-01"))
                .andExpect(jsonPath("$.categoryId").value(1L))
                .andExpect(jsonPath("$.categoryDesignation").value("Informatique"));
    }

    @Test
    void create_shouldReturn400_whenCodeIsBlank() throws Exception {
        ArticleRequest invalidRequest = new ArticleRequest(
                " ", "Ordinateur portable", new BigDecimal("500.00"),
                new BigDecimal("19.25"), new BigDecimal("596.25"), "photo.png", new BigDecimal("5"), 1L
        );

        mockMvc.perform(post("/articles")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest());

        verify(articleService, never()).create(any());
    }

    @Test
    void create_shouldReturn400_whenCategoryIdIsMissing() throws Exception {
        ArticleRequest invalidRequest = new ArticleRequest(
                "ART-01", "Ordinateur portable", new BigDecimal("500.00"),
                new BigDecimal("19.25"), new BigDecimal("596.25"), "photo.png", new BigDecimal("5"), null
        );

        mockMvc.perform(post("/articles")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest());

        verify(articleService, never()).create(any());
    }

    @Test
    void create_shouldReturn409_whenCodeAlreadyUsed() throws Exception {
        ArticleRequest request = validRequest();

        when(articleService.create(request))
                .thenThrow(new DuplicateCodeException("Le code 'ART-01' est déjà utilisé"));

        mockMvc.perform(post("/articles")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict());
    }

    @Test
    void create_shouldReturn404_whenCategoryNotFound() throws Exception {
        ArticleRequest request = validRequest();

        when(articleService.create(request))
                .thenThrow(new EntityNotFoundException("Catégorie introuvable avec l'id : 1"));

        mockMvc.perform(post("/articles")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound());
    }

    @Test
    void getById_shouldReturn200_whenFound() throws Exception {
        when(articleService.getById(1L)).thenReturn(sampleResponse());

        mockMvc.perform(get("/articles/{id}", 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.code").value("ART-01"));
    }

    @Test
    void getById_shouldReturn404_whenNotFound() throws Exception {
        when(articleService.getById(99L))
                .thenThrow(new EntityNotFoundException("Article introuvable avec l'id : 99"));

        mockMvc.perform(get("/articles/{id}", 99L))
                .andExpect(status().isNotFound());
    }

    @Test
    void getAll_shouldReturn200WithList() throws Exception {
        List<ArticleResponse> responses = List.of(
                sampleResponse(),
                new ArticleResponse(2L, "ART-02", "Souris", new BigDecimal("10.00"),
                        new BigDecimal("19.25"), new BigDecimal("11.93"), null, new BigDecimal("5"), 1L, "Informatique",
                        null, null, null, null)
        );
        Page<ArticleResponse> page = new PageImpl<>(responses);
        when(articleService.getAll(any(Pageable.class))).thenReturn(page);

        mockMvc.perform(get("/articles"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(2))
                .andExpect(jsonPath("$.content[0].code").value("ART-01"))
                .andExpect(jsonPath("$.content[1].code").value("ART-02"));
    }

    @Test
    void update_shouldReturn200_whenValidRequest() throws Exception {
        ArticleRequest request = validRequest();
        ArticleResponse response = sampleResponse();

        when(articleService.update(eq(1L), any(ArticleRequest.class))).thenReturn(response);

        mockMvc.perform(put("/articles/{id}", 1L)
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("ART-01"));
    }

    @Test
    void update_shouldReturn404_whenArticleNotFound() throws Exception {
        ArticleRequest request = validRequest();

        when(articleService.update(eq(99L), any(ArticleRequest.class)))
                .thenThrow(new EntityNotFoundException("Article introuvable avec l'id : 99"));

        mockMvc.perform(put("/articles/{id}", 99L)
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound());
    }

    @Test
    void update_shouldReturn409_whenCodeAlreadyUsedByAnotherArticle() throws Exception {
        ArticleRequest request = validRequest();

        when(articleService.update(eq(1L), any(ArticleRequest.class)))
                .thenThrow(new DuplicateCodeException("Le code 'ART-01' est déjà utilisé"));

        mockMvc.perform(put("/articles/{id}", 1L)
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict());
    }

    @Test
    void uploadPhoto_shouldReturn200_whenValidFile() throws Exception {
        MockMultipartFile file = new MockMultipartFile("file", "test.jpg", "image/jpeg", "dummy content".getBytes());

        when(articleService.uploadPhoto(eq(1L), any())).thenReturn(sampleResponse());

        mockMvc.perform(multipart("/articles/{id}/photo", 1L).file(file))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L));
    }

    @Test
    void uploadPhoto_shouldReturn404_whenArticleNotFound() throws Exception {
        MockMultipartFile file = new MockMultipartFile("file", "test.jpg", "image/jpeg", "dummy content".getBytes());

        when(articleService.uploadPhoto(eq(99L), any()))
                .thenThrow(new EntityNotFoundException("Article introuvable avec l'id : 99"));

        mockMvc.perform(multipart("/articles/{id}/photo", 99L).file(file))
                .andExpect(status().isNotFound());
    }

    @Test
    void delete_shouldReturn204_whenFound() throws Exception {
        mockMvc.perform(delete("/articles/{id}", 1L))
                .andExpect(status().isNoContent())
                .andExpect(content().string(""));

        verify(articleService).delete(1L);
    }

    @Test
    void delete_shouldReturn404_whenNotFound() throws Exception {
        doThrow(new EntityNotFoundException("Article introuvable avec l'id : 99"))
                .when(articleService).delete(99L);

        mockMvc.perform(delete("/articles/{id}", 99L))
                .andExpect(status().isNotFound());
    }
}