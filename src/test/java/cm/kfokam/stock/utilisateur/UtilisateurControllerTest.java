package cm.kfokam.stock.utilisateur;

import cm.kfokam.stock.exception.DuplicateEmailException;
import cm.kfokam.stock.exception.EntityNotFoundException;
import cm.kfokam.stock.utilisateur.dto.ChangePasswordRequest;
import cm.kfokam.stock.utilisateur.dto.UtilisateurRequest;
import cm.kfokam.stock.utilisateur.dto.UtilisateurResponse;
import cm.kfokam.stock.utilisateur.model.Role;
import cm.kfokam.stock.utilisateur.model.Utilisateur;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.util.List;
import java.util.Set;

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

@WebMvcTest(UtilisateurController.class)
@AutoConfigureMockMvc(addFilters = false)
class UtilisateurControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private UtilisateurService utilisateurService;

    private UtilisateurRequest validRequest() {
        return new UtilisateurRequest(
                "Tchana", "Francky", "francky@kfokam.cm",
                LocalDate.of(1995, 3, 10), null, null, "Douala", null, "Cameroun",
                Set.of(Role.ROLE_ADMIN)
        );
    }

    private UtilisateurResponse sampleResponse() {
        return new UtilisateurResponse(
                1L, "Tchana", "Francky", "francky@kfokam.cm", LocalDate.of(1995, 3, 10),
                null, null, "Douala", null, "Cameroun", 1L, "Kfokam SARL", Set.of(Role.ROLE_ADMIN), true
        );
    }

    @AfterEach
    void clearSecurityContext() {
        SecurityContextHolder.clearContext();
    }

    private void authenticateAsUser(Long id) {
        Utilisateur principal = Utilisateur.builder()
                .id(id)
                .nom("Tchana")
                .prenom("Francky")
                .email("francky@kfokam.cm")
                .motDePasse("encoded-pwd")
                .roles(Set.of(Role.ROLE_ADMIN))
                .build();
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(principal, null, principal.getAuthorities()));
    }

    @Test
    void create_shouldReturn201_whenValidRequest() throws Exception {
        UtilisateurRequest request = validRequest();
        UtilisateurResponse response = sampleResponse();

        when(utilisateurService.create(request)).thenReturn(response);

        mockMvc.perform(post("/utilisateurs")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.email").value("francky@kfokam.cm"))
                .andExpect(jsonPath("$.motDePasse").doesNotExist());
    }

    @Test
    void create_shouldReturn400_whenEmailIsInvalid() throws Exception {
        UtilisateurRequest invalidRequest = new UtilisateurRequest(
                "Tchana", "Francky", "not-an-email",
                LocalDate.of(1995, 3, 10), null, null, "Douala", null, "Cameroun",
                Set.of(Role.ROLE_ADMIN)
        );

        mockMvc.perform(post("/utilisateurs")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest());

        verify(utilisateurService, never()).create(any());
    }

    @Test
    void create_shouldReturn400_whenRolesIsEmpty() throws Exception {
        UtilisateurRequest invalidRequest = new UtilisateurRequest(
                "Tchana", "Francky", "francky@kfokam.cm",
                LocalDate.of(1995, 3, 10), null, null, "Douala", null, "Cameroun",
                Set.of()
        );

        mockMvc.perform(post("/utilisateurs")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest());

        verify(utilisateurService, never()).create(any());
    }

    @Test
    void create_shouldReturn409_whenEmailAlreadyUsed() throws Exception {
        UtilisateurRequest request = validRequest();

        when(utilisateurService.create(request))
                .thenThrow(new DuplicateEmailException("L'email 'francky@kfokam.cm' est déjà utilisé"));

        mockMvc.perform(post("/utilisateurs")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict());
    }

    @Test
    void create_shouldReturn404_whenEntrepriseNotFound() throws Exception {
        UtilisateurRequest request = validRequest();

        when(utilisateurService.create(request))
                .thenThrow(new EntityNotFoundException("Entreprise introuvable avec l'id : 1"));

        mockMvc.perform(post("/utilisateurs")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound());
    }

    @Test
    void getById_shouldReturn200_whenFound() throws Exception {
        when(utilisateurService.getById(1L)).thenReturn(sampleResponse());

        mockMvc.perform(get("/utilisateurs/{id}", 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L));
    }

    @Test
    void getById_shouldReturn404_whenNotFound() throws Exception {
        when(utilisateurService.getById(99L))
                .thenThrow(new EntityNotFoundException("Utilisateur introuvable avec l'id : 99"));

        mockMvc.perform(get("/utilisateurs/{id}", 99L))
                .andExpect(status().isNotFound());
    }

    @Test
    void getAll_shouldReturn200WithList() throws Exception {
        List<UtilisateurResponse> responses = List.of(sampleResponse());
        when(utilisateurService.getAll()).thenReturn(responses);

        mockMvc.perform(get("/utilisateurs"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].email").value("francky@kfokam.cm"));
    }

    @Test
    void update_shouldReturn200_whenValidRequest() throws Exception {
        UtilisateurRequest request = validRequest();
        UtilisateurResponse response = sampleResponse();

        when(utilisateurService.update(eq(1L), any(UtilisateurRequest.class))).thenReturn(response);

        mockMvc.perform(put("/utilisateurs/{id}", 1L)
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value("francky@kfokam.cm"));
    }

    @Test
    void update_shouldReturn404_whenUtilisateurNotFound() throws Exception {
        UtilisateurRequest request = validRequest();

        when(utilisateurService.update(eq(99L), any(UtilisateurRequest.class)))
                .thenThrow(new EntityNotFoundException("Utilisateur introuvable avec l'id : 99"));

        mockMvc.perform(put("/utilisateurs/{id}", 99L)
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound());
    }

    @Test
    void delete_shouldReturn204_whenFound() throws Exception {
        mockMvc.perform(delete("/utilisateurs/{id}", 1L))
                .andExpect(status().isNoContent())
                .andExpect(content().string(""));

        verify(utilisateurService).delete(1L);
    }

    @Test
    void delete_shouldReturn404_whenNotFound() throws Exception {
        org.mockito.Mockito.doThrow(new EntityNotFoundException("Utilisateur introuvable avec l'id : 99"))
                .when(utilisateurService).delete(99L);

        mockMvc.perform(delete("/utilisateurs/{id}", 99L))
                .andExpect(status().isNotFound());
    }

    @Test
    void changePassword_shouldReturn204_whenValid() throws Exception {
        authenticateAsUser(1L);
        ChangePasswordRequest request = new ChangePasswordRequest("OldP@ss1", "NewP@ss1!");

        mockMvc.perform(post("/utilisateurs/change-password")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNoContent());

        verify(utilisateurService).changePassword(1L, request);
    }

    @Test
    void changePassword_shouldReturn400_whenNewPasswordTooShort() throws Exception {
        authenticateAsUser(1L);
        ChangePasswordRequest invalidRequest = new ChangePasswordRequest("OldP@ss1", "short");

        mockMvc.perform(post("/utilisateurs/change-password")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest());

        verify(utilisateurService, never()).changePassword(any(), any());
    }

    @Test
    void changePassword_shouldReturn401_whenOldPasswordIncorrect() throws Exception {
        authenticateAsUser(1L);
        ChangePasswordRequest request = new ChangePasswordRequest("wrong-old-password", "NewP@ss1!");

        org.mockito.Mockito.doThrow(new BadCredentialsException("L'ancien mot de passe est incorrect"))
                .when(utilisateurService).changePassword(1L, request);

        mockMvc.perform(post("/utilisateurs/change-password")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized());
    }
}
