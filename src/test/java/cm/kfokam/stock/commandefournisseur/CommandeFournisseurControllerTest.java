package cm.kfokam.stock.commandefournisseur;

import cm.kfokam.stock.commandefournisseur.dto.CommandeFournisseurRequest;
import cm.kfokam.stock.commandefournisseur.dto.CommandeFournisseurResponse;
import cm.kfokam.stock.commandefournisseur.dto.EtatCommandeRequest;
import cm.kfokam.stock.commandefournisseur.dto.LigneCommandeFournisseurRequest;
import cm.kfokam.stock.commandefournisseur.dto.LigneCommandeFournisseurResponse;
import cm.kfokam.stock.commandefournisseur.model.EtatCommande;
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

@WebMvcTest(CommandeFournisseurController.class)
@AutoConfigureMockMvc(addFilters = false)
class CommandeFournisseurControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private CommandeFournisseurService commandeFournisseurService;

    private CommandeFournisseurRequest validRequest() {
        return new CommandeFournisseurRequest(
                null,
                LocalDate.of(2026, 7, 27),
                1L,
                List.of(new LigneCommandeFournisseurRequest(1L, 2))
        );
    }

    private CommandeFournisseurResponse sampleResponse() {
        return new CommandeFournisseurResponse(
                1L,
                "CF-2026-0001",
                LocalDate.of(2026, 7, 27),
                EtatCommande.EN_PREPARATION,
                1L, "Martin", "Paul",
                new BigDecimal("1000.00"), new BigDecimal("192.50"), new BigDecimal("1192.50"),
                List.of(new LigneCommandeFournisseurResponse(1L, 1L, "Ordinateur portable", 2,
                        new BigDecimal("500.00"), new BigDecimal("596.25")))
        );
    }

    @Test
    void create_shouldReturn201_whenValidRequest() throws Exception {
        CommandeFournisseurRequest request = validRequest();
        CommandeFournisseurResponse response = sampleResponse();

        when(commandeFournisseurService.create(request)).thenReturn(response);

        mockMvc.perform(post("/commandes-fournisseur")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.codeCommande").value("CF-2026-0001"))
                .andExpect(jsonPath("$.idFournisseur").value(1L))
                .andExpect(jsonPath("$.etatCommande").value("EN_PREPARATION"));
    }

    @Test
    void create_shouldReturn400_whenIdFournisseurIsMissing() throws Exception {
        CommandeFournisseurRequest invalidRequest = new CommandeFournisseurRequest(
                null, LocalDate.of(2026, 7, 27), null,
                List.of(new LigneCommandeFournisseurRequest(1L, 2))
        );

        mockMvc.perform(post("/commandes-fournisseur")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest());

        verify(commandeFournisseurService, never()).create(any());
    }

    @Test
    void create_shouldReturn400_whenLignesIsEmpty() throws Exception {
        CommandeFournisseurRequest invalidRequest = new CommandeFournisseurRequest(
                null, LocalDate.of(2026, 7, 27), 1L, List.of()
        );

        mockMvc.perform(post("/commandes-fournisseur")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest());

        verify(commandeFournisseurService, never()).create(any());
    }

    @Test
    void create_shouldReturn404_whenFournisseurNotFound() throws Exception {
        CommandeFournisseurRequest request = validRequest();

        when(commandeFournisseurService.create(request))
                .thenThrow(new EntityNotFoundException("Fournisseur introuvable avec l'id : 1"));

        mockMvc.perform(post("/commandes-fournisseur")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound());
    }

    @Test
    void create_shouldReturn409_whenCodeAlreadyUsed() throws Exception {
        CommandeFournisseurRequest request = validRequest();

        when(commandeFournisseurService.create(request))
                .thenThrow(new DuplicateCodeException("Le code 'CF-2026-0001' est déjà utilisé"));

        mockMvc.perform(post("/commandes-fournisseur")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict());
    }

    @Test
    void getById_shouldReturn200_whenFound() throws Exception {
        when(commandeFournisseurService.getById(1L)).thenReturn(sampleResponse());

        mockMvc.perform(get("/commandes-fournisseur/{id}", 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.codeCommande").value("CF-2026-0001"));
    }

    @Test
    void getById_shouldReturn404_whenNotFound() throws Exception {
        when(commandeFournisseurService.getById(99L))
                .thenThrow(new EntityNotFoundException("Commande fournisseur introuvable avec l'id : 99"));

        mockMvc.perform(get("/commandes-fournisseur/{id}", 99L))
                .andExpect(status().isNotFound());
    }

    @Test
    void getAll_shouldReturn200WithList() throws Exception {
        List<CommandeFournisseurResponse> responses = List.of(sampleResponse());
        when(commandeFournisseurService.getAll()).thenReturn(responses);

        mockMvc.perform(get("/commandes-fournisseur"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].codeCommande").value("CF-2026-0001"));
    }

    @Test
    void getHistoriqueByFournisseur_shouldReturn200WithList() throws Exception {
        List<CommandeFournisseurResponse> responses = List.of(sampleResponse());
        when(commandeFournisseurService.getHistoriqueByFournisseur(1L)).thenReturn(responses);

        mockMvc.perform(get("/commandes-fournisseur/fournisseur/{idFournisseur}", 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].idFournisseur").value(1L));
    }

    @Test
    void getHistoriqueByFournisseur_shouldReturn404_whenFournisseurNotFound() throws Exception {
        when(commandeFournisseurService.getHistoriqueByFournisseur(99L))
                .thenThrow(new EntityNotFoundException("Fournisseur introuvable avec l'id : 99"));

        mockMvc.perform(get("/commandes-fournisseur/fournisseur/{idFournisseur}", 99L))
                .andExpect(status().isNotFound());
    }

    @Test
    void update_shouldReturn200_whenValidRequest() throws Exception {
        CommandeFournisseurRequest request = validRequest();
        CommandeFournisseurResponse response = sampleResponse();

        when(commandeFournisseurService.update(eq(1L), any(CommandeFournisseurRequest.class))).thenReturn(response);

        mockMvc.perform(put("/commandes-fournisseur/{id}", 1L)
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.codeCommande").value("CF-2026-0001"));
    }

    @Test
    void update_shouldReturn404_whenCommandeNotFound() throws Exception {
        CommandeFournisseurRequest request = validRequest();

        when(commandeFournisseurService.update(eq(99L), any(CommandeFournisseurRequest.class)))
                .thenThrow(new EntityNotFoundException("Commande fournisseur introuvable avec l'id : 99"));

        mockMvc.perform(put("/commandes-fournisseur/{id}", 99L)
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound());
    }

    @Test
    void delete_shouldReturn204_whenFound() throws Exception {
        mockMvc.perform(delete("/commandes-fournisseur/{id}", 1L))
                .andExpect(status().isNoContent())
                .andExpect(content().string(""));

        verify(commandeFournisseurService).delete(1L);
    }

    @Test
    void delete_shouldReturn404_whenNotFound() throws Exception {
        org.mockito.Mockito.doThrow(new EntityNotFoundException("Commande fournisseur introuvable avec l'id : 99"))
                .when(commandeFournisseurService).delete(99L);

        mockMvc.perform(delete("/commandes-fournisseur/{id}", 99L))
                .andExpect(status().isNotFound());
    }

    @Test
    void updateEtat_shouldReturn200_whenValidTransition() throws Exception {
        EtatCommandeRequest etatRequest = new EtatCommandeRequest(EtatCommande.VALIDEE);
        CommandeFournisseurResponse response = sampleResponse();

        when(commandeFournisseurService.updateEtatCommande(1L, EtatCommande.VALIDEE)).thenReturn(response);

        mockMvc.perform(patch("/commandes-fournisseur/{id}/etat", 1L)
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(etatRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L));
    }

    @Test
    void updateEtat_shouldReturn400_whenEtatIsMissing() throws Exception {
        mockMvc.perform(patch("/commandes-fournisseur/{id}/etat", 1L)
                        .contentType("application/json")
                        .content("{}"))
                .andExpect(status().isBadRequest());

        verify(commandeFournisseurService, never()).updateEtatCommande(any(), any());
    }

    @Test
    void updateEtat_shouldReturn409_whenTransitionInvalid() throws Exception {
        EtatCommandeRequest etatRequest = new EtatCommandeRequest(EtatCommande.LIVREE);

        when(commandeFournisseurService.updateEtatCommande(1L, EtatCommande.LIVREE))
                .thenThrow(new InvalidStateTransitionException("Transition invalide de 'EN_PREPARATION' vers 'LIVREE'"));

        mockMvc.perform(patch("/commandes-fournisseur/{id}/etat", 1L)
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(etatRequest)))
                .andExpect(status().isConflict());
    }
}
