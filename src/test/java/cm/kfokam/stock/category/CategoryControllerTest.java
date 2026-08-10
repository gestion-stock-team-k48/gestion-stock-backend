package cm.kfokam.stock.category;

import cm.kfokam.stock.category.dto.CategoryRequest;
import cm.kfokam.stock.category.dto.CategoryResponse;
import cm.kfokam.stock.exception.DuplicateCodeException;
import cm.kfokam.stock.exception.EntityNotFoundException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(CategoryController.class)
@AutoConfigureMockMvc(addFilters = false)
class CategoryControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private CategoryService categoryService;

    @Test
    void create_shouldReturn201_whenValidRequest() throws Exception {
        CategoryRequest request = new CategoryRequest("CAT-01", "Informatique");
        CategoryResponse response = new CategoryResponse(1L, "CAT-01", "Informatique", null, null, null, null);

        when(categoryService.create(request)).thenReturn(response);

        mockMvc.perform(post("/categories")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.code").value("CAT-01"))
                .andExpect(jsonPath("$.designation").value("Informatique"));
    }

    @Test
    void create_shouldReturn400_whenCodeIsBlank() throws Exception {
        CategoryRequest invalidRequest = new CategoryRequest(" ", "Informatique");

        mockMvc.perform(post("/categories")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest());

        verify(categoryService, never()).create(any());
    }

    @Test
    void create_shouldReturn409_whenCodeAlreadyUsed() throws Exception {
        CategoryRequest request = new CategoryRequest("CAT-01", "Informatique");

        when(categoryService.create(request))
                .thenThrow(new DuplicateCodeException("Le code 'CAT-01' est déjà utilisé"));

        mockMvc.perform(post("/categories")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict());
    }

    @Test
    void getById_shouldReturn200_whenFound() throws Exception {
        CategoryResponse response = new CategoryResponse(1L, "CAT-01", "Informatique", null, null, null, null);
        when(categoryService.getById(1L)).thenReturn(response);

        mockMvc.perform(get("/categories/{id}", 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.code").value("CAT-01"));
    }

    @Test
    void getById_shouldReturn404_whenNotFound() throws Exception {
        when(categoryService.getById(99L))
                .thenThrow(new EntityNotFoundException("Catégorie introuvable avec l'id : 99"));

        mockMvc.perform(get("/categories/{id}", 99L))
                .andExpect(status().isNotFound());
    }

    @Test
    void getAll_shouldReturn200WithList() throws Exception {
        List<CategoryResponse> responses = List.of(
                new CategoryResponse(1L, "CAT-01", "Informatique", null, null, null, null),
                new CategoryResponse(2L, "CAT-02", "Bureautique", null, null, null, null)
        );
        when(categoryService.getAll()).thenReturn(responses);

        mockMvc.perform(get("/categories"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].code").value("CAT-01"))
                .andExpect(jsonPath("$[1].code").value("CAT-02"));
    }

    @Test
    void update_shouldReturn200_whenValidRequest() throws Exception {
        CategoryRequest request = new CategoryRequest("CAT-01", "Informatique modifiée");
        CategoryResponse response = new CategoryResponse(1L, "CAT-01", "Informatique modifiée", null, null, null, null);

        when(categoryService.update(eq(1L), any(CategoryRequest.class))).thenReturn(response);

        mockMvc.perform(put("/categories/{id}", 1L)
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.designation").value("Informatique modifiée"));
    }

    @Test
    void update_shouldReturn404_whenCategoryNotFound() throws Exception {
        CategoryRequest request = new CategoryRequest("CAT-01", "Informatique");

        when(categoryService.update(eq(99L), any(CategoryRequest.class)))
                .thenThrow(new EntityNotFoundException("Catégorie introuvable avec l'id : 99"));

        mockMvc.perform(put("/categories/{id}", 99L)
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound());
    }

    @Test
    void update_shouldReturn409_whenCodeAlreadyUsedByAnotherCategory() throws Exception {
        CategoryRequest request = new CategoryRequest("CAT-02", "Informatique");

        when(categoryService.update(eq(1L), any(CategoryRequest.class)))
                .thenThrow(new DuplicateCodeException("Le code 'CAT-02' est déjà utilisé"));

        mockMvc.perform(put("/categories/{id}", 1L)
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict());
    }

    @Test
    void delete_shouldReturn204_whenFound() throws Exception {
        mockMvc.perform(delete("/categories/{id}", 1L))
                .andExpect(status().isNoContent())
                .andExpect(content().string(""));

        verify(categoryService).delete(1L);
    }

    @Test
    void delete_shouldReturn404_whenNotFound() throws Exception {
        org.mockito.Mockito.doThrow(new EntityNotFoundException("Catégorie introuvable avec l'id : 99"))
                .when(categoryService).delete(99L);

        mockMvc.perform(delete("/categories/{id}", 99L))
                .andExpect(status().isNotFound());
    }
}
