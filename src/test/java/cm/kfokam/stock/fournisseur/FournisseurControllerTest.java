package cm.kfokam.stock.fournisseur;

import cm.kfokam.stock.exception.DuplicateEmailException;
import cm.kfokam.stock.exception.EntityNotFoundException;
import cm.kfokam.stock.fournisseur.dto.FournisseurRequest;
import cm.kfokam.stock.fournisseur.dto.FournisseurResponse;
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

@WebMvcTest(FournisseurController.class)
@AutoConfigureMockMvc(addFilters = false)
class FournisseurControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private FournisseurService fournisseurService;

    private FournisseurRequest validRequest() {
        return new FournisseurRequest(
                "Kamdem", "Paul", "paul@example.com", "+237600000002",
                null, "Douala", null, "Cameroun", "photo.png"
        );
    }

    private FournisseurResponse sampleResponse() {
        return new FournisseurResponse(
                1L, "Kamdem", "Paul", "paul@example.com", "+237600000002",
                null, "Douala", null, "Cameroun", "photo.png",
                null, null, null, null
        );
    }

    @Test
    void create_shouldReturn201_whenValidRequest() throws Exception {
        FournisseurRequest request = validRequest();
        FournisseurResponse response = sampleResponse();

        when(fournisseurService.create(request)).thenReturn(response);

        mockMvc.perform(post("/fournisseurs")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.email").value("paul@example.com"));
    }

    @Test
    void create_shouldReturn400_whenNomIsBlank() throws Exception {
        FournisseurRequest invalidRequest = new FournisseurRequest(
                " ", "Paul", "paul@example.com", "+237600000002",
                null, "Douala", null, "Cameroun", "photo.png"
        );

        mockMvc.perform(post("/fournisseurs")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest());

        verify(fournisseurService, never()).create(any());
    }

    @Test
    void create_shouldReturn400_whenEmailIsInvalid() throws Exception {
        FournisseurRequest invalidRequest = new FournisseurRequest(
                "Kamdem", "Paul", "not-an-email", "+237600000002",
                null, "Douala", null, "Cameroun", "photo.png"
        );

        mockMvc.perform(post("/fournisseurs")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest());

        verify(fournisseurService, never()).create(any());
    }

    @Test
    void create_shouldReturn409_whenEmailAlreadyUsed() throws Exception {
        FournisseurRequest request = validRequest();

        when(fournisseurService.create(request))
                .thenThrow(new DuplicateEmailException("L'email 'paul@example.com' est déjà utilisé"));

        mockMvc.perform(post("/fournisseurs")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict());
    }

    @Test
    void getById_shouldReturn200_whenFound() throws Exception {
        when(fournisseurService.getById(1L)).thenReturn(sampleResponse());

        mockMvc.perform(get("/fournisseurs/{id}", 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L));
    }

    @Test
    void getById_shouldReturn404_whenNotFound() throws Exception {
        when(fournisseurService.getById(99L))
                .thenThrow(new EntityNotFoundException("Fournisseur introuvable avec l'id : 99"));

        mockMvc.perform(get("/fournisseurs/{id}", 99L))
                .andExpect(status().isNotFound());
    }

    @Test
    void getAll_shouldReturn200WithList() throws Exception {
        List<FournisseurResponse> responses = List.of(
                sampleResponse(),
                new FournisseurResponse(2L, "Njoya", "Aissatou", "aissatou@example.com", null,
                        null, "Yaoundé", null, "Cameroun", null,
                        null, null, null, null)
        );
        Page<FournisseurResponse> page = new PageImpl<>(responses);
        when(fournisseurService.getAll(any(Pageable.class))).thenReturn(page);

        mockMvc.perform(get("/fournisseurs"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(2))
                .andExpect(jsonPath("$.content[0].email").value("paul@example.com"))
                .andExpect(jsonPath("$.content[1].email").value("aissatou@example.com"));
    }

    @Test
    void update_shouldReturn200_whenValidRequest() throws Exception {
        FournisseurRequest request = validRequest();
        FournisseurResponse response = sampleResponse();

        when(fournisseurService.update(eq(1L), any(FournisseurRequest.class))).thenReturn(response);

        mockMvc.perform(put("/fournisseurs/{id}", 1L)
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value("paul@example.com"));
    }

    @Test
    void update_shouldReturn404_whenFournisseurNotFound() throws Exception {
        FournisseurRequest request = validRequest();

        when(fournisseurService.update(eq(99L), any(FournisseurRequest.class)))
                .thenThrow(new EntityNotFoundException("Fournisseur introuvable avec l'id : 99"));

        mockMvc.perform(put("/fournisseurs/{id}", 99L)
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound());
    }

    @Test
    void update_shouldReturn409_whenEmailAlreadyUsedByAnotherFournisseur() throws Exception {
        FournisseurRequest request = validRequest();

        when(fournisseurService.update(eq(1L), any(FournisseurRequest.class)))
                .thenThrow(new DuplicateEmailException("L'email 'paul@example.com' est déjà utilisé"));

        mockMvc.perform(put("/fournisseurs/{id}", 1L)
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict());
    }

    @Test
    void uploadPhoto_shouldReturn200_whenValidFile() throws Exception {
        MockMultipartFile file = new MockMultipartFile("file", "test.jpg", "image/jpeg", "dummy content".getBytes());

        when(fournisseurService.uploadPhoto(eq(1L), any())).thenReturn(sampleResponse());

        mockMvc.perform(multipart("/fournisseurs/{id}/photo", 1L).file(file))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L));
    }

    @Test
    void uploadPhoto_shouldReturn404_whenFournisseurNotFound() throws Exception {
        MockMultipartFile file = new MockMultipartFile("file", "test.jpg", "image/jpeg", "dummy content".getBytes());

        when(fournisseurService.uploadPhoto(eq(99L), any()))
                .thenThrow(new EntityNotFoundException("Fournisseur introuvable avec l'id : 99"));

        mockMvc.perform(multipart("/fournisseurs/{id}/photo", 99L).file(file))
                .andExpect(status().isNotFound());
    }

    @Test
    void delete_shouldReturn204_whenFound() throws Exception {
        mockMvc.perform(delete("/fournisseurs/{id}", 1L))
                .andExpect(status().isNoContent())
                .andExpect(content().string(""));

        verify(fournisseurService).delete(1L);
    }

    @Test
    void delete_shouldReturn404_whenNotFound() throws Exception {
        doThrow(new EntityNotFoundException("Fournisseur introuvable avec l'id : 99"))
                .when(fournisseurService).delete(99L);

        mockMvc.perform(delete("/fournisseurs/{id}", 99L))
                .andExpect(status().isNotFound());
    }
}