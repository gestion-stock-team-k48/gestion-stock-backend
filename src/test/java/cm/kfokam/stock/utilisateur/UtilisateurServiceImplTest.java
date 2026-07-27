package cm.kfokam.stock.utilisateur;

import cm.kfokam.stock.entreprise.EntrepriseService;
import cm.kfokam.stock.entreprise.dto.EntrepriseResponse;
import cm.kfokam.stock.entreprise.model.Entreprise;
import cm.kfokam.stock.exception.DuplicateEmailException;
import cm.kfokam.stock.exception.EntityNotFoundException;
import cm.kfokam.stock.utilisateur.dto.ChangePasswordRequest;
import cm.kfokam.stock.utilisateur.dto.UtilisateurRequest;
import cm.kfokam.stock.utilisateur.dto.UtilisateurResponse;
import cm.kfokam.stock.utilisateur.model.Role;
import cm.kfokam.stock.utilisateur.model.Utilisateur;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UtilisateurServiceImplTest {

    @Mock
    private UtilisateurRepository utilisateurRepository;

    @Mock
    private EntrepriseService entrepriseService;

    @Mock
    private UtilisateurMapper utilisateurMapper;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private EntityManager entityManager;

    @InjectMocks
    private UtilisateurServiceImpl utilisateurService;

    private Entreprise entreprise;
    private EntrepriseResponse entrepriseResponse;
    private Utilisateur utilisateur;
    private UtilisateurRequest request;
    private UtilisateurResponse response;
    private ChangePasswordRequest changePasswordRequest;

    @BeforeEach
    void setUp() {
        entreprise = Entreprise.builder().id(1L).nom("Kfokam SARL").build();
        entrepriseResponse = new EntrepriseResponse(1L, "Kfokam SARL", null, null, null, null, null,
                "CF-001", null, "contact@kfokam.cm", null, null);

        utilisateur = Utilisateur.builder()
                .id(1L)
                .nom("Tchana")
                .prenom("Francky")
                .email("francky@kfokam.cm")
                .motDePasse("encoded-pwd")
                .dateDeNaissance(LocalDate.of(1995, 3, 10))
                .entreprise(entreprise)
                .roles(Set.of(Role.ROLE_ADMIN))
                .mustChangePassword(true)
                .build();

        request = new UtilisateurRequest(
                "Tchana", "Francky", "francky@kfokam.cm",
                LocalDate.of(1995, 3, 10), null, null, "Douala", null, "Cameroun",
                1L, Set.of(Role.ROLE_ADMIN)
        );

        response = new UtilisateurResponse(
                1L, "Tchana", "Francky", "francky@kfokam.cm", LocalDate.of(1995, 3, 10),
                null, null, "Douala", null, "Cameroun", 1L, "Kfokam SARL", Set.of(Role.ROLE_ADMIN), true
        );

        changePasswordRequest = new ChangePasswordRequest("OldP@ss1", "NewP@ss1!");
    }

    @Test
    void create_shouldReturnResponse_whenValid() {
        when(utilisateurRepository.existsByEmail("francky@kfokam.cm")).thenReturn(false);
        when(entrepriseService.getById(1L)).thenReturn(entrepriseResponse);
        when(utilisateurMapper.toEntity(request)).thenReturn(utilisateur);
        when(passwordEncoder.encode(anyString())).thenReturn("encoded-temp-pwd");
        when(entityManager.getReference(Entreprise.class, 1L)).thenReturn(entreprise);
        when(utilisateurRepository.save(utilisateur)).thenReturn(utilisateur);
        when(utilisateurMapper.toResponse(utilisateur)).thenReturn(response);

        UtilisateurResponse result = utilisateurService.create(request);

        assertThat(result).isEqualTo(response);
        verify(utilisateurRepository).save(utilisateur);
    }

    @Test
    void create_shouldGenerateAndEncodeTemporaryPassword() {
        when(utilisateurRepository.existsByEmail("francky@kfokam.cm")).thenReturn(false);
        when(entrepriseService.getById(1L)).thenReturn(entrepriseResponse);
        when(utilisateurMapper.toEntity(request)).thenReturn(utilisateur);
        when(passwordEncoder.encode(anyString())).thenReturn("encoded-temp-pwd");
        when(entityManager.getReference(Entreprise.class, 1L)).thenReturn(entreprise);
        when(utilisateurRepository.save(utilisateur)).thenReturn(utilisateur);
        when(utilisateurMapper.toResponse(utilisateur)).thenReturn(response);

        utilisateurService.create(request);

        assertThat(utilisateur.getMotDePasse()).isEqualTo("encoded-temp-pwd");
        assertThat(utilisateur.isMustChangePassword()).isTrue();
        verify(passwordEncoder).encode(argThat(rawPassword -> rawPassword != null && rawPassword.length() > 0));
    }

    @Test
    void create_shouldThrowDuplicateEmailException_whenEmailAlreadyUsed() {
        when(utilisateurRepository.existsByEmail("francky@kfokam.cm")).thenReturn(true);

        assertThatThrownBy(() -> utilisateurService.create(request))
                .isInstanceOf(DuplicateEmailException.class)
                .hasMessageContaining("francky@kfokam.cm");

        verify(entrepriseService, never()).getById(any());
        verify(utilisateurRepository, never()).save(any());
    }

    @Test
    void create_shouldThrowEntityNotFoundException_whenEntrepriseNotFound() {
        when(utilisateurRepository.existsByEmail("francky@kfokam.cm")).thenReturn(false);
        when(entrepriseService.getById(1L)).thenThrow(new EntityNotFoundException("Entreprise introuvable avec l'id : 1"));

        assertThatThrownBy(() -> utilisateurService.create(request))
                .isInstanceOf(EntityNotFoundException.class);

        verify(utilisateurRepository, never()).save(any());
    }

    @Test
    void getById_shouldReturnResponse_whenFound() {
        when(utilisateurRepository.findById(1L)).thenReturn(Optional.of(utilisateur));
        when(utilisateurMapper.toResponse(utilisateur)).thenReturn(response);

        UtilisateurResponse result = utilisateurService.getById(1L);

        assertThat(result).isEqualTo(response);
    }

    @Test
    void getById_shouldThrowEntityNotFoundException_whenNotFound() {
        when(utilisateurRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> utilisateurService.getById(99L))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessageContaining("99");
    }

    @Test
    void getAll_shouldReturnListOfResponses() {
        List<Utilisateur> utilisateurs = List.of(utilisateur);
        List<UtilisateurResponse> responses = List.of(response);

        when(utilisateurRepository.findAll()).thenReturn(utilisateurs);
        when(utilisateurMapper.toResponseList(utilisateurs)).thenReturn(responses);

        List<UtilisateurResponse> result = utilisateurService.getAll();

        assertThat(result).containsExactly(response);
    }

    @Test
    void update_shouldReturnUpdatedResponse_whenValid() {
        when(utilisateurRepository.findById(1L)).thenReturn(Optional.of(utilisateur));
        when(utilisateurRepository.findByEmail("francky@kfokam.cm")).thenReturn(Optional.of(utilisateur));
        when(entrepriseService.getById(1L)).thenReturn(entrepriseResponse);
        when(entityManager.getReference(Entreprise.class, 1L)).thenReturn(entreprise);
        when(utilisateurRepository.save(utilisateur)).thenReturn(utilisateur);
        when(utilisateurMapper.toResponse(utilisateur)).thenReturn(response);

        UtilisateurResponse result = utilisateurService.update(1L, request);

        assertThat(result).isEqualTo(response);
        verify(utilisateurMapper).updateEntityFromRequest(request, utilisateur);
    }

    @Test
    void update_shouldNotTouchPassword() {
        when(utilisateurRepository.findById(1L)).thenReturn(Optional.of(utilisateur));
        when(utilisateurRepository.findByEmail("francky@kfokam.cm")).thenReturn(Optional.of(utilisateur));
        when(entrepriseService.getById(1L)).thenReturn(entrepriseResponse);
        when(entityManager.getReference(Entreprise.class, 1L)).thenReturn(entreprise);
        when(utilisateurRepository.save(utilisateur)).thenReturn(utilisateur);
        when(utilisateurMapper.toResponse(utilisateur)).thenReturn(response);

        utilisateurService.update(1L, request);

        verify(passwordEncoder, never()).encode(any());
        assertThat(utilisateur.getMotDePasse()).isEqualTo("encoded-pwd");
    }

    @Test
    void update_shouldThrowEntityNotFoundException_whenUtilisateurNotFound() {
        when(utilisateurRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> utilisateurService.update(99L, request))
                .isInstanceOf(EntityNotFoundException.class);

        verify(utilisateurRepository, never()).save(any());
    }

    @Test
    void update_shouldThrowDuplicateEmailException_whenEmailUsedByAnotherUtilisateur() {
        Utilisateur other = Utilisateur.builder().id(2L).email("other@kfokam.cm").build();
        UtilisateurRequest updateRequest = new UtilisateurRequest(
                "Tchana", "Francky", "other@kfokam.cm",
                LocalDate.of(1995, 3, 10), null, null, "Douala", null, "Cameroun",
                1L, Set.of(Role.ROLE_ADMIN)
        );

        when(utilisateurRepository.findById(1L)).thenReturn(Optional.of(utilisateur));
        when(utilisateurRepository.findByEmail("other@kfokam.cm")).thenReturn(Optional.of(other));

        assertThatThrownBy(() -> utilisateurService.update(1L, updateRequest))
                .isInstanceOf(DuplicateEmailException.class)
                .hasMessageContaining("other@kfokam.cm");

        verify(utilisateurRepository, never()).save(any());
    }

    @Test
    void delete_shouldDeleteUtilisateur_whenFound() {
        when(utilisateurRepository.findById(1L)).thenReturn(Optional.of(utilisateur));

        utilisateurService.delete(1L);

        verify(utilisateurRepository, times(1)).delete(utilisateur);
    }

    @Test
    void delete_shouldThrowEntityNotFoundException_whenNotFound() {
        when(utilisateurRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> utilisateurService.delete(99L))
                .isInstanceOf(EntityNotFoundException.class);

        verify(utilisateurRepository, never()).delete(any());
    }

    @Test
    void changePassword_shouldUpdatePasswordAndClearFlag_whenOldPasswordCorrect() {
        when(utilisateurRepository.findById(1L)).thenReturn(Optional.of(utilisateur));
        when(passwordEncoder.matches("OldP@ss1", "encoded-pwd")).thenReturn(true);
        when(passwordEncoder.encode("NewP@ss1!")).thenReturn("new-encoded-pwd");

        utilisateurService.changePassword(1L, changePasswordRequest);

        assertThat(utilisateur.getMotDePasse()).isEqualTo("new-encoded-pwd");
        assertThat(utilisateur.isMustChangePassword()).isFalse();
        verify(utilisateurRepository).save(utilisateur);
    }

    @Test
    void changePassword_shouldThrowBadCredentialsException_whenOldPasswordIncorrect() {
        when(utilisateurRepository.findById(1L)).thenReturn(Optional.of(utilisateur));
        when(passwordEncoder.matches("OldP@ss1", "encoded-pwd")).thenReturn(false);

        assertThatThrownBy(() -> utilisateurService.changePassword(1L, changePasswordRequest))
                .isInstanceOf(BadCredentialsException.class);

        verify(utilisateurRepository, never()).save(any());
        assertThat(utilisateur.isMustChangePassword()).isTrue();
    }

    @Test
    void changePassword_shouldThrowEntityNotFoundException_whenUtilisateurNotFound() {
        when(utilisateurRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> utilisateurService.changePassword(99L, changePasswordRequest))
                .isInstanceOf(EntityNotFoundException.class);

        verify(passwordEncoder, never()).matches(any(), any());
    }
}