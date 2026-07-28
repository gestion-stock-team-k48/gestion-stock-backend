package cm.kfokam.stock.commandeclient;

import cm.kfokam.stock.commandeclient.dto.CommandeClientRequest;
import cm.kfokam.stock.commandeclient.dto.CommandeClientResponse;
import cm.kfokam.stock.commandeclient.dto.EtatCommandeRequest;
import cm.kfokam.stock.commandeclient.dto.LigneCommandeClientRequest;
import cm.kfokam.stock.commandeclient.dto.LigneCommandeClientResponse;
import cm.kfokam.stock.commandeclient.model.EtatCommande;
import cm.kfokam.stock.exception.DuplicateCodeException;
import cm.kfokam.stock.exception.EntityNotFoundException;
import cm.kfokam.stock.exception.InvalidStateTransitionException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(CommandeClientController.class)
@AutoConfigureMockMvc(addFilters = false)
class CommandeClientControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private CommandeClientService commandeClientService;

    private CommandeClientRequest validRequest() {
        return new CommandeClientRequest(
                null,
                LocalDate.of(2026, 7, 27),
                1L,
                List.of(new LigneCommandeClientRequest(1L, 2))
        );
    }

    private CommandeClientResponse sampleResponse() {
        return new CommandeClientResponse(
                1L,
                "CC-2026-0001",
                LocalDate.of(2026, 7, 27),
                EtatCommande.EN_PREPARATION,
                1L, "Doe", "John",
                new BigDecimal("1000.00"), new BigDecimal("192.50"), new BigDecimal("1192.50"),
                List.of(new LigneCommandeClientResponse(1L, 1L, "Ordinateur portable", 2,
                        new BigDecimal("500.00"), new BigDecimal("596.25")))
        );
    }

    @Test
    void create_shouldReturn201_whenValidRequest() throws Exception {
        CommandeClientRequest request = validRequest();
        CommandeClientResponse response = sampleResponse();

        when(commandeClientService.create(request)).thenReturn(response);

        mockMvc.perform(post("/api/commandes-client")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.codeCommande").value("CC-2026-0001"))
                .andExpect(jsonPath("$.idClient").value(1L))
                .andExpect(jsonPath("$.etatCommande").value("EN_PREPARATION"));
    }

    @Test
    void create_shouldReturn400_whenIdClientIsMissing() throws Exception {
        CommandeClientRequest invalidRequest = new CommandeClientRequest(
                null, LocalDate.of(2026, 7, 27), null,
                List.of(new LigneCommandeClientRequest(1L, 2))
        );

        mockMvc.perform(post("/api/commandes-client")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest());

        verify(commandeClientService, never()).create(any());
    }

    @Test
    void create_shouldReturn400_whenLignesIsEmpty() throws Exception {
        CommandeClientRequest invalidRequest = new CommandeClientRequest(
                null, LocalDate.of(2026, 7, 27), 1L, List.of()
        );

        mockMvc.perform(post("/api/commandes-client")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest());

        verify(commandeClientService, never()).create(any());
    }

    @Test
    void create_shouldReturn404_whenClientNotFound() throws Exception {
        CommandeClientRequest request = validRequest();

        when(commandeClientService.create(request))
                .thenThrow(new EntityNotFoundException("Client introuvable avec l'id : 1"));

        mockMvc.perform(post("/api/commandes-client")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound());
    }

    @Test
    void create_shouldReturn409_whenCodeAlreadyUsed() throws Exception {
        CommandeClientRequest request = validRequest();

        when(commandeClientService.create(request))
                .thenThrow(new DuplicateCodeException("Le code 'CC-2026-0001' est déjà utilisé"));

        mockMvc.perform(post("/api/commandes-client")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict());
    }

    @Test
    void getById_shouldReturn200_whenFound() throws Exception {
        when(commandeClientService.getById(1L)).thenReturn(sampleResponse());

        mockMvc.perform(get("/api/commandes-client/{id}", 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.codeCommande").value("CC-2026-0001"));
    }

    @Test
    void getById_shouldReturn404_whenNotFound() throws Exception {
        when(commandeClientService.getById(99L))
                .thenThrow(new EntityNotFoundException("Commande client introuvable avec l'id : 99"));

        mockMvc.perform(get("/api/commandes-client/{id}", 99L))
                .andExpect(status().isNotFound());
    }

    @Test
    void getAll_shouldReturn200WithList() throws Exception {
        List<CommandeClientResponse> responses = List.of(sampleResponse());
        when(commandeClientService.getAll()).thenReturn(responses);

        mockMvc.perform(get("/api/commandes-client"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].codeCommande").value("CC-2026-0001"));
    }

    @Test
    void getHistoriqueByClient_shouldReturn200WithList() throws Exception {
        List<CommandeClientResponse> responses = List.of(sampleResponse());
        when(commandeClientService.getHistoriqueByClient(1L)).thenReturn(responses);

        mockMvc.perform(get("/api/commandes-client/client/{idClient}", 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].idClient").value(1L));
    }

    @Test
    void getHistoriqueByClient_shouldReturn404_whenClientNotFound() throws Exception {
        when(commandeClientService.getHistoriqueByClient(99L))
                .thenThrow(new EntityNotFoundException("Client introuvable avec l'id : 99"));

        mockMvc.perform(get("/api/commandes-client/client/{idClient}", 99L))
                .andExpect(status().isNotFound());
    }

    @Test
    void update_shouldReturn200_whenValidRequest() throws Exception {
        CommandeClientRequest request = validRequest();
        CommandeClientResponse response = sampleResponse();

        when(commandeClientService.update(eq(1L), any(CommandeClientRequest.class))).thenReturn(response);

        mockMvc.perform(put("/api/commandes-client/{id}", 1L)
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.codeCommande").value("CC-2026-0001"));
    }

    @Test
    void update_shouldReturn404_whenCommandeNotFound() throws Exception {
        CommandeClientRequest request = validRequest();

        when(commandeClientService.update(eq(99L), any(CommandeClientRequest.class)))
                .thenThrow(new EntityNotFoundException("Commande client introuvable avec l'id : 99"));

        mockMvc.perform(put("/api/commandes-client/{id}", 99L)
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound());
    }

    @Test
    void delete_shouldReturn204_whenFound() throws Exception {
        mockMvc.perform(delete("/api/commandes-client/{id}", 1L))
                .andExpect(status().isNoContent())
                .andExpect(content().string(""));

        verify(commandeClientService).delete(1L);
    }

    @Test
    void delete_shouldReturn404_whenNotFound() throws Exception {
        org.mockito.Mockito.doThrow(new EntityNotFoundException("Commande client introuvable avec l'id : 99"))
                .when(commandeClientService).delete(99L);

        mockMvc.perform(delete("/api/commandes-client/{id}", 99L))
                .andExpect(status().isNotFound());
    }

    @Test
    void updateEtat_shouldReturn200_whenValidTransition() throws Exception {
        EtatCommandeRequest etatRequest = new EtatCommandeRequest(EtatCommande.VALIDEE);
        CommandeClientResponse response = sampleResponse();

        when(commandeClientService.updateEtatCommande(1L, EtatCommande.VALIDEE)).thenReturn(response);

        mockMvc.perform(patch("/api/commandes-client/{id}/etat", 1L)
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(etatRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L));
    }

    @Test
    void updateEtat_shouldReturn400_whenEtatIsMissing() throws Exception {
        mockMvc.perform(patch("/api/commandes-client/{id}/etat", 1L)
                        .contentType("application/json")
                        .content("{}"))
                .andExpect(status().isBadRequest());

        verify(commandeClientService, never()).updateEtatCommande(any(), any());
    }

    @Test
    void updateEtat_shouldReturn409_whenTransitionInvalid() throws Exception {
        EtatCommandeRequest etatRequest = new EtatCommandeRequest(EtatCommande.LIVREE);

        when(commandeClientService.updateEtatCommande(1L, EtatCommande.LIVREE))
                .thenThrow(new InvalidStateTransitionException("Transition invalide de 'EN_PREPARATION' vers 'LIVREE'"));

        mockMvc.perform(patch("/api/commandes-client/{id}/etat", 1L)
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(etatRequest)))
                .andExpect(status().isConflict());
    }
}