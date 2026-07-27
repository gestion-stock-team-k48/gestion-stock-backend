package cm.kfokam.stock.entreprise;

import cm.kfokam.stock.entreprise.dto.EntrepriseRequest;
import cm.kfokam.stock.entreprise.dto.EntrepriseResponse;
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

@WebMvcTest(EntrepriseController.class)
@AutoConfigureMockMvc(addFilters = false)
class EntrepriseControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private EntrepriseService entrepriseService;

    private EntrepriseRequest validRequest() {
        return new EntrepriseRequest(
                "Kfokam SARL", "Gestion de stock", null, "Douala", null, "Cameroun",
                "CF-001", "logo.png", "contact@kfokam.cm", "+237600000000", "https://kfokam.cm"
        );
    }

    private EntrepriseResponse sampleResponse() {
        return new EntrepriseResponse(
                1L, "Kfokam SARL", "Gestion de stock", null, "Douala", null, "Cameroun",
                "CF-001", "logo.png", "contact@kfokam.cm", "+237600000000", "https://kfokam.cm"
        );
    }

    @Test
    void create_shouldReturn201_whenValidRequest() throws Exception {
        EntrepriseRequest request = validRequest();
        EntrepriseResponse response = sampleResponse();

        when(entrepriseService.create(request)).thenReturn(response);

        mockMvc.perform(post("/api/entreprises")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.codeFiscal").value("CF-001"));
    }

    @Test
    void create_shouldReturn400_whenNomIsBlank() throws Exception {
        EntrepriseRequest invalidRequest = new EntrepriseRequest(
                "", "Gestion de stock", null, "Douala", null, "Cameroun",
                "CF-001", "logo.png", "contact@kfokam.cm", "+237600000000", "https://kfokam.cm"
        );

        mockMvc.perform(post("/api/entreprises")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest());

        verify(entrepriseService, never()).create(any());
    }

    @Test
    void create_shouldReturn400_whenEmailIsInvalid() throws Exception {
        EntrepriseRequest invalidRequest = new EntrepriseRequest(
                "Kfokam SARL", "Gestion de stock", null, "Douala", null, "Cameroun",
                "CF-001", "logo.png", "not-an-email", "+237600000000", "https://kfokam.cm"
        );

        mockMvc.perform(post("/api/entreprises")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest());

        verify(entrepriseService, never()).create(any());
    }

    @Test
    void create_shouldReturn409_whenCodeFiscalAlreadyUsed() throws Exception {
        EntrepriseRequest request = validRequest();

        when(entrepriseService.create(request))
                .thenThrow(new DuplicateCodeException("Le code fiscal 'CF-001' est déjà utilisé"));

        mockMvc.perform(post("/api/entreprises")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict());
    }

    @Test
    void getById_shouldReturn200_whenFound() throws Exception {
        when(entrepriseService.getById(1L)).thenReturn(sampleResponse());

        mockMvc.perform(get("/api/entreprises/{id}", 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L));
    }

    @Test
    void getById_shouldReturn404_whenNotFound() throws Exception {
        when(entrepriseService.getById(99L))
                .thenThrow(new EntityNotFoundException("Entreprise introuvable avec l'id : 99"));

        mockMvc.perform(get("/api/entreprises/{id}", 99L))
                .andExpect(status().isNotFound());
    }

    @Test
    void getAll_shouldReturn200WithList() throws Exception {
        List<EntrepriseResponse> responses = List.of(sampleResponse());
        when(entrepriseService.getAll()).thenReturn(responses);

        mockMvc.perform(get("/api/entreprises"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].codeFiscal").value("CF-001"));
    }

    @Test
    void update_shouldReturn200_whenValidRequest() throws Exception {
        EntrepriseRequest request = validRequest();
        EntrepriseResponse response = sampleResponse();

        when(entrepriseService.update(eq(1L), any(EntrepriseRequest.class))).thenReturn(response);

        mockMvc.perform(put("/api/entreprises/{id}", 1L)
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.codeFiscal").value("CF-001"));
    }

    @Test
    void update_shouldReturn404_whenEntrepriseNotFound() throws Exception {
        EntrepriseRequest request = validRequest();

        when(entrepriseService.update(eq(99L), any(EntrepriseRequest.class)))
                .thenThrow(new EntityNotFoundException("Entreprise introuvable avec l'id : 99"));

        mockMvc.perform(put("/api/entreprises/{id}", 99L)
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound());
    }

    @Test
    void delete_shouldReturn204_whenFound() throws Exception {
        mockMvc.perform(delete("/api/entreprises/{id}", 1L))
                .andExpect(status().isNoContent())
                .andExpect(content().string(""));

        verify(entrepriseService).delete(1L);
    }

    @Test
    void delete_shouldReturn404_whenNotFound() throws Exception {
        org.mockito.Mockito.doThrow(new EntityNotFoundException("Entreprise introuvable avec l'id : 99"))
                .when(entrepriseService).delete(99L);

        mockMvc.perform(delete("/api/entreprises/{id}", 99L))
                .andExpect(status().isNotFound());
    }
}
