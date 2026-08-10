package cm.kfokam.stock.entreprise;

import cm.kfokam.stock.auth.CurrentUserService;
import cm.kfokam.stock.entreprise.dto.EntrepriseRequest;
import cm.kfokam.stock.entreprise.dto.EntrepriseResponse;
import cm.kfokam.stock.exception.EntityNotFoundException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(EntrepriseController.class)
@AutoConfigureMockMvc(addFilters = false)
class EntrepriseControllerTest {

    private static final Long ENTREPRISE_ID = 1L;

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private EntrepriseService entrepriseService;

    @MockBean
    private CurrentUserService currentUserService;

    private EntrepriseRequest validRequest() {
        return new EntrepriseRequest(
                "Kfokam SARL", "Gestion de stock", null, "Douala", null, "Cameroun",
                "CF-001", "logo.png", "contact@kfokam.cm", "+237600000000", "https://kfokam.cm"
        );
    }

    private EntrepriseResponse sampleResponse() {
        return new EntrepriseResponse(
                1L, "Kfokam SARL", "Gestion de stock", null, "Douala", null, "Cameroun",
                "CF-001", "logo.png", "contact@kfokam.cm", "+237600000000", "https://kfokam.cm",
                null, null, null, null
        );
    }

    @Test
    void getMine_shouldReturn200_whenFound() throws Exception {
        when(currentUserService.getCurrentEntrepriseId()).thenReturn(ENTREPRISE_ID);
        when(entrepriseService.getById(ENTREPRISE_ID)).thenReturn(sampleResponse());

        mockMvc.perform(get("/entreprises/me"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.codeFiscal").value("CF-001"));
    }

    @Test
    void getMine_shouldReturn404_whenNotFound() throws Exception {
        when(currentUserService.getCurrentEntrepriseId()).thenReturn(ENTREPRISE_ID);
        when(entrepriseService.getById(ENTREPRISE_ID))
                .thenThrow(new EntityNotFoundException("Entreprise introuvable avec l'id : 1"));

        mockMvc.perform(get("/entreprises/me"))
                .andExpect(status().isNotFound());
    }

    @Test
    void updateMine_shouldReturn200_whenValidRequest() throws Exception {
        EntrepriseRequest request = validRequest();
        EntrepriseResponse response = sampleResponse();

        when(currentUserService.getCurrentEntrepriseId()).thenReturn(ENTREPRISE_ID);
        when(entrepriseService.update(ENTREPRISE_ID, request)).thenReturn(response);

        mockMvc.perform(put("/entreprises/me")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.codeFiscal").value("CF-001"));
    }

    @Test
    void updateMine_shouldReturn400_whenNomIsBlank() throws Exception {
        EntrepriseRequest invalidRequest = new EntrepriseRequest(
                "", "Gestion de stock", null, "Douala", null, "Cameroun",
                "CF-001", "logo.png", "contact@kfokam.cm", "+237600000000", "https://kfokam.cm"
        );

        mockMvc.perform(put("/entreprises/me")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void updateMine_shouldReturn404_whenEntrepriseNotFound() throws Exception {
        EntrepriseRequest request = validRequest();

        when(currentUserService.getCurrentEntrepriseId()).thenReturn(ENTREPRISE_ID);
        when(entrepriseService.update(any(), any()))
                .thenThrow(new EntityNotFoundException("Entreprise introuvable avec l'id : 1"));

        mockMvc.perform(put("/entreprises/me")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound());
    }
}
