package cm.kfokam.stock.utilisateur;

import cm.kfokam.stock.utilisateur.dto.AdminInitialRequest;
import cm.kfokam.stock.auth.CurrentUserService;
import cm.kfokam.stock.email.EmailService;
import cm.kfokam.stock.entreprise.model.Entreprise;
import cm.kfokam.stock.exception.DuplicateEmailException;
import cm.kfokam.stock.exception.EntityNotFoundException;
import cm.kfokam.stock.storage.FileStorageService;
import cm.kfokam.stock.utilisateur.dto.ChangePasswordRequest;
import cm.kfokam.stock.utilisateur.dto.UtilisateurMeRequest;
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
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UtilisateurServiceImplTest {

    private static final Long ENTREPRISE_ID = 1L;

    @Mock
    private UtilisateurRepository utilisateurRepository;

    @Mock
    private CurrentUserService currentUserService;

    @Mock
    private UtilisateurMapper utilisateurMapper;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private EntityManager entityManager;

    @Mock
    private FileStorageService fileStorageService;

    @Mock
    private EmailService emailService;

    @InjectMocks
    private UtilisateurServiceImpl utilisateurService;

    private Entreprise entreprise;
    private Utilisateur utilisateur;
    private UtilisateurRequest request;
    private UtilisateurResponse response;
    private ChangePasswordRequest changePasswordRequest;

    @BeforeEach
    void setUp() {
        entreprise = Entreprise.builder().id(1L).nom("Kfokam SARL").build();

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
                Set.of(Role.ROLE_ADMIN)
        );

        response = new UtilisateurResponse(
                1L, "Tchana", "Francky", "francky@kfokam.cm", LocalDate.of(1995, 3, 10),
                null, null, "Douala", null, "Cameroun", 1L, "Kfokam SARL", Set.of(Role.ROLE_ADMIN), true,
                null, null, null, null
        );

        changePasswordRequest = new ChangePasswordRequest("OldP@ss1", "NewP@ss1!");
    }

    @Test
    void create_shouldReturnResponse_whenValid() {
        when(currentUserService.getCurrentEntrepriseId()).thenReturn(ENTREPRISE_ID);
        when(utilisateurRepository.existsByEmail("francky@kfokam.cm")).thenReturn(false);
        when(utilisateurMapper.toEntity(request)).thenReturn(utilisateur);
        when(passwordEncoder.encode(anyString())).thenReturn("encoded-temp-pwd");
        when(entityManager.getReference(Entreprise.class, ENTREPRISE_ID)).thenReturn(entreprise);
        when(utilisateurRepository.save(utilisateur)).thenReturn(utilisateur);
        when(utilisateurMapper.toResponse(utilisateur)).thenReturn(response);

        UtilisateurResponse result = utilisateurService.create(request);

        assertThat(result).isEqualTo(response);
        verify(utilisateurRepository).save(utilisateur);
    }

    @Test
    void create_shouldSendTemporaryPasswordByEmail() {
        when(currentUserService.getCurrentEntrepriseId()).thenReturn(ENTREPRISE_ID);
        when(utilisateurRepository.existsByEmail("francky@kfokam.cm")).thenReturn(false);
        when(utilisateurMapper.toEntity(request)).thenReturn(utilisateur);
        when(passwordEncoder.encode(anyString())).thenReturn("encoded-temp-pwd");
        when(entityManager.getReference(Entreprise.class, ENTREPRISE_ID)).thenReturn(entreprise);
        when(utilisateurRepository.save(utilisateur)).thenReturn(utilisateur);
        when(utilisateurMapper.toResponse(utilisateur)).thenReturn(response);

        utilisateurService.create(request);

        verify(emailService).envoyerMotDePasseTemporaire(eq("francky@kfokam.cm"), eq("Francky"), anyString());
    }

    @Test
    void create_shouldGenerateAndEncodeTemporaryPassword() {
        when(currentUserService.getCurrentEntrepriseId()).thenReturn(ENTREPRISE_ID);
        when(utilisateurRepository.existsByEmail("francky@kfokam.cm")).thenReturn(false);
        when(utilisateurMapper.toEntity(request)).thenReturn(utilisateur);
        when(passwordEncoder.encode(anyString())).thenReturn("encoded-temp-pwd");
        when(entityManager.getReference(Entreprise.class, ENTREPRISE_ID)).thenReturn(entreprise);
        when(utilisateurRepository.save(utilisateur)).thenReturn(utilisateur);
        when(utilisateurMapper.toResponse(utilisateur)).thenReturn(response);

        utilisateurService.create(request);

        assertThat(utilisateur.getMotDePasse()).isEqualTo("encoded-temp-pwd");
        assertThat(utilisateur.isMustChangePassword()).isTrue();
        verify(passwordEncoder).encode(argThat(rawPassword -> rawPassword != null && !rawPassword.isEmpty()));
    }

    @Test
    void create_shouldThrowDuplicateEmailException_whenEmailAlreadyUsed() {
        when(utilisateurRepository.existsByEmail("francky@kfokam.cm")).thenReturn(true);

        assertThatThrownBy(() -> utilisateurService.create(request))
                .isInstanceOf(DuplicateEmailException.class)
                .hasMessageContaining("francky@kfokam.cm");

        verify(utilisateurRepository, never()).save(any());
    }

    @Test
    void createInitialAdmin_shouldCreateAdminWithGivenPassword_andSkipMustChangePassword() {
        when(utilisateurRepository.existsByEmail("francky@kfokam.cm")).thenReturn(false);
        when(passwordEncoder.encode("MyOwnP@ss1")).thenReturn("encoded-own-pwd");
        when(entityManager.getReference(Entreprise.class, ENTREPRISE_ID)).thenReturn(entreprise);
        when(utilisateurRepository.save(any(Utilisateur.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(utilisateurMapper.toResponse(any(Utilisateur.class))).thenReturn(response);

        utilisateurService.createInitialAdmin(new AdminInitialRequest(
                ENTREPRISE_ID, "Tchana", "Francky", "francky@kfokam.cm", "MyOwnP@ss1", LocalDate.of(1995, 3, 10), "Rue des Manguiers", "Yaoundé", "BP-123", "Cameroun"));

        verify(utilisateurRepository).save(argThat(saved ->
                saved.getMotDePasse().equals("encoded-own-pwd")
                        && !saved.isMustChangePassword()
                        && saved.getRoles().equals(Set.of(Role.ROLE_ADMIN))
        ));
    }

    @Test
    void createInitialAdmin_shouldSetAdresse_onSavedUtilisateurAndReturnedProfile() {
        UtilisateurResponse responseWithAdresse = new UtilisateurResponse(
                1L, "Tchana", "Francky", "francky@kfokam.cm", LocalDate.of(1995, 3, 10),
                null, "Rue des Manguiers", "Yaoundé", "BP-123", "Cameroun",
                1L, "Kfokam SARL", Set.of(Role.ROLE_ADMIN), false,
                null, null, null, null
        );

        when(utilisateurRepository.existsByEmail("francky@kfokam.cm")).thenReturn(false);
        when(passwordEncoder.encode("MyOwnP@ss1")).thenReturn("encoded-own-pwd");
        when(entityManager.getReference(Entreprise.class, ENTREPRISE_ID)).thenReturn(entreprise);
        when(utilisateurRepository.save(any(Utilisateur.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(utilisateurMapper.toResponse(any(Utilisateur.class))).thenReturn(responseWithAdresse);

        UtilisateurResponse result = utilisateurService.createInitialAdmin(new AdminInitialRequest(
                ENTREPRISE_ID, "Tchana", "Francky", "francky@kfokam.cm", "MyOwnP@ss1", LocalDate.of(1995, 3, 10), "Rue des Manguiers", "Yaoundé", "BP-123", "Cameroun"));

        verify(utilisateurRepository).save(argThat(saved ->
                saved.getAdresse() != null
                        && "Rue des Manguiers".equals(saved.getAdresse().getAdresse1())
                        && "Yaoundé".equals(saved.getAdresse().getVille())
                        && "BP-123".equals(saved.getAdresse().getCodePostal())
                        && "Cameroun".equals(saved.getAdresse().getPays())
        ));
        assertThat(result.rue()).isEqualTo("Rue des Manguiers");
        assertThat(result.ville()).isEqualTo("Yaoundé");
        assertThat(result.codePostal()).isEqualTo("BP-123");
        assertThat(result.pays()).isEqualTo("Cameroun");
    }

    @Test
    void createInitialAdmin_shouldThrowDuplicateEmailException_whenEmailAlreadyUsed() {
        when(utilisateurRepository.existsByEmail("francky@kfokam.cm")).thenReturn(true);

        assertThatThrownBy(() -> utilisateurService.createInitialAdmin(new AdminInitialRequest(
                ENTREPRISE_ID, "Tchana", "Francky", "francky@kfokam.cm", "MyOwnP@ss1", LocalDate.of(1995, 3, 10), "Rue des Manguiers", "Yaoundé", "BP-123", "Cameroun")))
                .isInstanceOf(DuplicateEmailException.class);

        verify(utilisateurRepository, never()).save(any());
    }

    @Test
    void getById_shouldReturnResponse_whenFound() {
        when(currentUserService.getCurrentEntrepriseId()).thenReturn(ENTREPRISE_ID);
        when(utilisateurRepository.findByIdAndEntrepriseId(1L, ENTREPRISE_ID)).thenReturn(Optional.of(utilisateur));
        when(utilisateurMapper.toResponse(utilisateur)).thenReturn(response);

        UtilisateurResponse result = utilisateurService.getById(1L);

        assertThat(result).isEqualTo(response);
    }

    @Test
    void getById_shouldThrowEntityNotFoundException_whenNotFound() {
        when(currentUserService.getCurrentEntrepriseId()).thenReturn(ENTREPRISE_ID);
        when(utilisateurRepository.findByIdAndEntrepriseId(99L, ENTREPRISE_ID)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> utilisateurService.getById(99L))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessageContaining("99");
    }

    @Test
    void getAll_shouldReturnPageOfResponses() {
        when(currentUserService.getCurrentEntrepriseId()).thenReturn(ENTREPRISE_ID);
        Pageable pageable = PageRequest.of(0, 20);
        Page<Utilisateur> utilisateurPage = new PageImpl<>(List.of(utilisateur));

        when(utilisateurRepository.findAllByEntrepriseId(ENTREPRISE_ID, pageable)).thenReturn(utilisateurPage);
        when(utilisateurMapper.toResponse(utilisateur)).thenReturn(response);

        Page<UtilisateurResponse> result = utilisateurService.getAll(pageable);

        assertThat(result.getContent()).containsExactly(response);
    }

    @Test
    void update_shouldReturnUpdatedResponse_whenValid() {
        when(currentUserService.getCurrentEntrepriseId()).thenReturn(ENTREPRISE_ID);
        when(utilisateurRepository.findByIdAndEntrepriseId(1L, ENTREPRISE_ID)).thenReturn(Optional.of(utilisateur));
        when(utilisateurRepository.findByEmail("francky@kfokam.cm")).thenReturn(Optional.of(utilisateur));
        when(utilisateurRepository.save(utilisateur)).thenReturn(utilisateur);
        when(utilisateurMapper.toResponse(utilisateur)).thenReturn(response);

        UtilisateurResponse result = utilisateurService.update(1L, request);

        assertThat(result).isEqualTo(response);
        verify(utilisateurMapper).updateEntityFromRequest(request, utilisateur);
    }

    @Test
    void update_shouldNotTouchPassword() {
        when(currentUserService.getCurrentEntrepriseId()).thenReturn(ENTREPRISE_ID);
        when(utilisateurRepository.findByIdAndEntrepriseId(1L, ENTREPRISE_ID)).thenReturn(Optional.of(utilisateur));
        when(utilisateurRepository.findByEmail("francky@kfokam.cm")).thenReturn(Optional.of(utilisateur));
        when(utilisateurRepository.save(utilisateur)).thenReturn(utilisateur);
        when(utilisateurMapper.toResponse(utilisateur)).thenReturn(response);

        utilisateurService.update(1L, request);

        verify(passwordEncoder, never()).encode(any());
        assertThat(utilisateur.getMotDePasse()).isEqualTo("encoded-pwd");
    }

    @Test
    void update_shouldThrowEntityNotFoundException_whenUtilisateurNotFound() {
        when(currentUserService.getCurrentEntrepriseId()).thenReturn(ENTREPRISE_ID);
        when(utilisateurRepository.findByIdAndEntrepriseId(99L, ENTREPRISE_ID)).thenReturn(Optional.empty());

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
                Set.of(Role.ROLE_ADMIN)
        );

        when(currentUserService.getCurrentEntrepriseId()).thenReturn(ENTREPRISE_ID);
        when(utilisateurRepository.findByIdAndEntrepriseId(1L, ENTREPRISE_ID)).thenReturn(Optional.of(utilisateur));
        when(utilisateurRepository.findByEmail("other@kfokam.cm")).thenReturn(Optional.of(other));

        assertThatThrownBy(() -> utilisateurService.update(1L, updateRequest))
                .isInstanceOf(DuplicateEmailException.class)
                .hasMessageContaining("other@kfokam.cm");

        verify(utilisateurRepository, never()).save(any());
    }

    @Test
    void updateMine_shouldReturnUpdatedResponse_whenValid() {
        UtilisateurMeRequest meRequest = new UtilisateurMeRequest(
                "Tchana", "Francky", LocalDate.of(1995, 3, 10), null, "Douala", null, "Cameroun"
        );

        when(currentUserService.getCurrentEntrepriseId()).thenReturn(ENTREPRISE_ID);
        when(utilisateurRepository.findByIdAndEntrepriseId(1L, ENTREPRISE_ID)).thenReturn(Optional.of(utilisateur));
        when(utilisateurRepository.save(utilisateur)).thenReturn(utilisateur);
        when(utilisateurMapper.toResponse(utilisateur)).thenReturn(response);

        UtilisateurResponse result = utilisateurService.updateMine(1L, meRequest);

        assertThat(result).isEqualTo(response);
        verify(utilisateurMapper).updateEntityFromMeRequest(meRequest, utilisateur);
    }

    @Test
    void updateMine_shouldNotTouchPasswordOrRoles() {
        UtilisateurMeRequest meRequest = new UtilisateurMeRequest(
                "Tchana", "Francky", LocalDate.of(1995, 3, 10), null, "Douala", null, "Cameroun"
        );

        when(currentUserService.getCurrentEntrepriseId()).thenReturn(ENTREPRISE_ID);
        when(utilisateurRepository.findByIdAndEntrepriseId(1L, ENTREPRISE_ID)).thenReturn(Optional.of(utilisateur));
        when(utilisateurRepository.save(utilisateur)).thenReturn(utilisateur);
        when(utilisateurMapper.toResponse(utilisateur)).thenReturn(response);

        utilisateurService.updateMine(1L, meRequest);

        verify(passwordEncoder, never()).encode(any());
        assertThat(utilisateur.getMotDePasse()).isEqualTo("encoded-pwd");
        assertThat(utilisateur.getRoles()).isEqualTo(Set.of(Role.ROLE_ADMIN));
    }

    @Test
    void updateMine_shouldThrowEntityNotFoundException_whenUtilisateurNotFound() {
        when(currentUserService.getCurrentEntrepriseId()).thenReturn(ENTREPRISE_ID);
        when(utilisateurRepository.findByIdAndEntrepriseId(99L, ENTREPRISE_ID)).thenReturn(Optional.empty());

        UtilisateurMeRequest meRequest = new UtilisateurMeRequest(
                "Tchana", "Francky", LocalDate.of(1995, 3, 10), null, "Douala", null, "Cameroun"
        );

        assertThatThrownBy(() -> utilisateurService.updateMine(99L, meRequest))
                .isInstanceOf(EntityNotFoundException.class);

        verify(utilisateurRepository, never()).save(any());
    }

    @Test
    void uploadPhoto_shouldReplacePhoto_andDeleteOldOne() {
        utilisateur.setPhoto("old-photo.png");
        MultipartFile file = new MockMultipartFile("file", "new.png", "image/png", "dummy content".getBytes());
        when(currentUserService.getCurrentUtilisateur()).thenReturn(utilisateur);
        when(currentUserService.getCurrentEntrepriseId()).thenReturn(ENTREPRISE_ID);
        when(utilisateurRepository.findByIdAndEntrepriseId(1L, ENTREPRISE_ID)).thenReturn(Optional.of(utilisateur));
        when(fileStorageService.uploadFile(file, "utilisateurs")).thenReturn("utilisateurs/new-uuid.png");
        when(utilisateurRepository.save(utilisateur)).thenReturn(utilisateur);
        when(utilisateurMapper.toResponse(utilisateur)).thenReturn(response);

        UtilisateurResponse result = utilisateurService.uploadPhoto(1L, file);

        assertThat(result).isEqualTo(response);
        assertThat(utilisateur.getPhoto()).isEqualTo("utilisateurs/new-uuid.png");
        verify(fileStorageService).deleteFile("old-photo.png");
    }

    @Test
    void uploadPhoto_shouldNotDeleteOldPhoto_whenUtilisateurHadNone() {
        MultipartFile file = new MockMultipartFile("file", "new.png", "image/png", "dummy content".getBytes());
        when(currentUserService.getCurrentUtilisateur()).thenReturn(utilisateur);
        when(currentUserService.getCurrentEntrepriseId()).thenReturn(ENTREPRISE_ID);
        when(utilisateurRepository.findByIdAndEntrepriseId(1L, ENTREPRISE_ID)).thenReturn(Optional.of(utilisateur));
        when(fileStorageService.uploadFile(file, "utilisateurs")).thenReturn("utilisateurs/new-uuid.png");
        when(utilisateurRepository.save(utilisateur)).thenReturn(utilisateur);
        when(utilisateurMapper.toResponse(utilisateur)).thenReturn(response);

        utilisateurService.uploadPhoto(1L, file);

        verify(fileStorageService, never()).deleteFile(any());
    }

    @Test
    void uploadPhoto_shouldThrowEntityNotFoundException_whenUtilisateurNotFound() {
        MultipartFile file = new MockMultipartFile("file", "new.png", "image/png", "dummy content".getBytes());
        when(currentUserService.getCurrentUtilisateur()).thenReturn(Utilisateur.builder().id(99L).build());
        when(currentUserService.getCurrentEntrepriseId()).thenReturn(ENTREPRISE_ID);
        when(utilisateurRepository.findByIdAndEntrepriseId(99L, ENTREPRISE_ID)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> utilisateurService.uploadPhoto(99L, file))
                .isInstanceOf(EntityNotFoundException.class);

        verify(fileStorageService, never()).uploadFile(any(), any());
    }

    @Test
    void uploadPhoto_shouldThrowAccessDeniedException_whenUploadingAnotherUsersPhoto() {
        MultipartFile file = new MockMultipartFile("file", "new.png", "image/png", "dummy content".getBytes());
        when(currentUserService.getCurrentUtilisateur()).thenReturn(Utilisateur.builder().id(2L).build());

        assertThatThrownBy(() -> utilisateurService.uploadPhoto(1L, file))
                .isInstanceOf(AccessDeniedException.class);

        verify(utilisateurRepository, never()).findByIdAndEntrepriseId(any(), any());
        verify(fileStorageService, never()).uploadFile(any(), any());
    }

    @Test
    void delete_shouldDeleteUtilisateur_whenFound() {
        when(currentUserService.getCurrentEntrepriseId()).thenReturn(ENTREPRISE_ID);
        when(utilisateurRepository.findByIdAndEntrepriseId(1L, ENTREPRISE_ID)).thenReturn(Optional.of(utilisateur));

        utilisateurService.delete(1L);

        verify(utilisateurRepository, times(1)).delete(utilisateur);
    }

    @Test
    void delete_shouldNotDeletePhoto_whenPhotoIsBlank() {
        utilisateur.setPhoto("   ");
        when(currentUserService.getCurrentEntrepriseId()).thenReturn(ENTREPRISE_ID);
        when(utilisateurRepository.findByIdAndEntrepriseId(1L, ENTREPRISE_ID)).thenReturn(Optional.of(utilisateur));

        utilisateurService.delete(1L);

        verify(utilisateurRepository, times(1)).delete(utilisateur);
        verify(fileStorageService, never()).deleteFile(any());
    }

    @Test
    void delete_shouldThrowEntityNotFoundException_whenNotFound() {
        when(currentUserService.getCurrentEntrepriseId()).thenReturn(ENTREPRISE_ID);
        when(utilisateurRepository.findByIdAndEntrepriseId(99L, ENTREPRISE_ID)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> utilisateurService.delete(99L))
                .isInstanceOf(EntityNotFoundException.class);

        verify(utilisateurRepository, never()).delete(any());
    }

    @Test
    void changePassword_shouldUpdatePasswordAndClearFlag_whenOldPasswordCorrect() {
        when(currentUserService.getCurrentEntrepriseId()).thenReturn(ENTREPRISE_ID);
        when(utilisateurRepository.findByIdAndEntrepriseId(1L, ENTREPRISE_ID)).thenReturn(Optional.of(utilisateur));
        when(passwordEncoder.matches("OldP@ss1", "encoded-pwd")).thenReturn(true);
        when(passwordEncoder.encode("NewP@ss1!")).thenReturn("new-encoded-pwd");

        utilisateurService.changePassword(1L, changePasswordRequest);

        assertThat(utilisateur.getMotDePasse()).isEqualTo("new-encoded-pwd");
        assertThat(utilisateur.isMustChangePassword()).isFalse();
        verify(utilisateurRepository).save(utilisateur);
    }

    @Test
    void changePassword_shouldThrowBadCredentialsException_whenOldPasswordIncorrect() {
        when(currentUserService.getCurrentEntrepriseId()).thenReturn(ENTREPRISE_ID);
        when(utilisateurRepository.findByIdAndEntrepriseId(1L, ENTREPRISE_ID)).thenReturn(Optional.of(utilisateur));
        when(passwordEncoder.matches("OldP@ss1", "encoded-pwd")).thenReturn(false);

        assertThatThrownBy(() -> utilisateurService.changePassword(1L, changePasswordRequest))
                .isInstanceOf(BadCredentialsException.class);

        verify(utilisateurRepository, never()).save(any());
        assertThat(utilisateur.isMustChangePassword()).isTrue();
    }

    @Test
    void changePassword_shouldThrowEntityNotFoundException_whenUtilisateurNotFound() {
        when(currentUserService.getCurrentEntrepriseId()).thenReturn(ENTREPRISE_ID);
        when(utilisateurRepository.findByIdAndEntrepriseId(99L, ENTREPRISE_ID)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> utilisateurService.changePassword(99L, changePasswordRequest))
                .isInstanceOf(EntityNotFoundException.class);

        verify(passwordEncoder, never()).matches(any(), any());
    }

    @Test
    void resetPassword_shouldUpdatePasswordAndClearFlag_withoutOldPasswordCheck() {
        when(utilisateurRepository.findById(1L)).thenReturn(Optional.of(utilisateur));
        when(passwordEncoder.encode("NewP@ss1!")).thenReturn("new-encoded-pwd");

        utilisateurService.resetPassword(1L, "NewP@ss1!");

        assertThat(utilisateur.getMotDePasse()).isEqualTo("new-encoded-pwd");
        assertThat(utilisateur.isMustChangePassword()).isFalse();
        verify(utilisateurRepository).save(utilisateur);
        verify(passwordEncoder, never()).matches(any(), any());
    }

    @Test
    void resetPassword_shouldThrowEntityNotFoundException_whenUtilisateurNotFound() {
        when(utilisateurRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> utilisateurService.resetPassword(99L, "NewP@ss1!"))
                .isInstanceOf(EntityNotFoundException.class);

        verify(utilisateurRepository, never()).save(any());
    }
}
