package cm.kfokam.stock.fournisseur;

import cm.kfokam.stock.auth.CurrentUserService;
import cm.kfokam.stock.entreprise.model.Adresse;
import cm.kfokam.stock.exception.DuplicateEmailException;
import cm.kfokam.stock.exception.EntityNotFoundException;
import cm.kfokam.stock.fournisseur.dto.FournisseurRequest;
import cm.kfokam.stock.fournisseur.dto.FournisseurResponse;
import cm.kfokam.stock.fournisseur.model.Fournisseur;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class FournisseurServiceImplTest {

    private static final Long ENTREPRISE_ID = 1L;

    @Mock
    private FournisseurRepository fournisseurRepository;

    @Mock
    private FournisseurMapper fournisseurMapper;

    @Mock
    private CurrentUserService currentUserService;

    @Mock
    private EntityManager entityManager;

    @InjectMocks
    private FournisseurServiceImpl fournisseurService;

    private Fournisseur fournisseur;
    private FournisseurRequest request;
    private FournisseurResponse response;

    @BeforeEach
    void setUp() {
        when(currentUserService.getCurrentEntrepriseId()).thenReturn(ENTREPRISE_ID);

        fournisseur = Fournisseur.builder()
                .id(1L)
                .nom("Kamdem")
                .prenom("Paul")
                .email("paul@example.com")
                .numTel("+237600000002")
                .adresse(Adresse.builder().ville("Douala").pays("Cameroun").build())
                .photo("photo.png")
                .build();

        request = new FournisseurRequest(
                "Kamdem", "Paul", "paul@example.com", "+237600000002",
                null, "Douala", null, "Cameroun", "photo.png"
        );

        response = new FournisseurResponse(
                1L, "Kamdem", "Paul", "paul@example.com", "+237600000002",
                null, "Douala", null, "Cameroun", "photo.png"
        );
    }

    @Test
    void create_shouldReturnResponse_whenEmailNotUsed() {
        when(fournisseurRepository.existsByEmailAndEntrepriseId("paul@example.com", ENTREPRISE_ID)).thenReturn(false);
        when(fournisseurMapper.toEntity(request)).thenReturn(fournisseur);
        when(fournisseurRepository.save(fournisseur)).thenReturn(fournisseur);
        when(fournisseurMapper.toResponse(fournisseur)).thenReturn(response);

        FournisseurResponse result = fournisseurService.create(request);

        assertThat(result).isEqualTo(response);
        verify(fournisseurRepository).save(fournisseur);
    }

    @Test
    void create_shouldThrowDuplicateEmailException_whenEmailAlreadyUsed() {
        when(fournisseurRepository.existsByEmailAndEntrepriseId("paul@example.com", ENTREPRISE_ID)).thenReturn(true);

        assertThatThrownBy(() -> fournisseurService.create(request))
                .isInstanceOf(DuplicateEmailException.class)
                .hasMessageContaining("paul@example.com");

        verify(fournisseurRepository, never()).save(any());
    }

    @Test
    void getById_shouldReturnResponse_whenFound() {
        when(fournisseurRepository.findByIdAndEntrepriseId(1L, ENTREPRISE_ID)).thenReturn(Optional.of(fournisseur));
        when(fournisseurMapper.toResponse(fournisseur)).thenReturn(response);

        FournisseurResponse result = fournisseurService.getById(1L);

        assertThat(result).isEqualTo(response);
    }

    @Test
    void getById_shouldThrowEntityNotFoundException_whenNotFound() {
        when(fournisseurRepository.findByIdAndEntrepriseId(99L, ENTREPRISE_ID)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> fournisseurService.getById(99L))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessageContaining("99");
    }

    @Test
    void getAll_shouldReturnListOfResponses() {
        List<Fournisseur> fournisseurs = List.of(fournisseur);
        List<FournisseurResponse> responses = List.of(response);

        when(fournisseurRepository.findAllByEntrepriseId(ENTREPRISE_ID)).thenReturn(fournisseurs);
        when(fournisseurMapper.toResponseList(fournisseurs)).thenReturn(responses);

        List<FournisseurResponse> result = fournisseurService.getAll();

        assertThat(result).containsExactly(response);
    }

    @Test
    void update_shouldReturnUpdatedResponse_whenValid() {
        FournisseurRequest updateRequest = new FournisseurRequest(
                "Kamdem", "Paul", "new@example.com", "+237600000003",
                null, "Yaoundé", null, "Cameroun", "photo2.png"
        );
        Fournisseur updatedFournisseur = Fournisseur.builder().id(1L).nom("Kamdem").prenom("Paul")
                .email("new@example.com").numTel("+237600000003")
                .adresse(Adresse.builder().ville("Yaoundé").pays("Cameroun").build())
                .photo("photo2.png").build();
        FournisseurResponse updatedResponse = new FournisseurResponse(
                1L, "Kamdem", "Paul", "new@example.com", "+237600000003",
                null, "Yaoundé", null, "Cameroun", "photo2.png"
        );

        when(fournisseurRepository.findByIdAndEntrepriseId(1L, ENTREPRISE_ID)).thenReturn(Optional.of(fournisseur));
        when(fournisseurRepository.findByEmailAndEntrepriseId("new@example.com", ENTREPRISE_ID)).thenReturn(Optional.empty());
        doAnswer(invocation -> {
            fournisseur.setEmail("new@example.com");
            fournisseur.setNumTel("+237600000003");
            return null;
        }).when(fournisseurMapper).updateEntityFromRequest(updateRequest, fournisseur);
        when(fournisseurRepository.save(fournisseur)).thenReturn(updatedFournisseur);
        when(fournisseurMapper.toResponse(updatedFournisseur)).thenReturn(updatedResponse);

        FournisseurResponse result = fournisseurService.update(1L, updateRequest);

        assertThat(result).isEqualTo(updatedResponse);
    }

    @Test
    void update_shouldThrowEntityNotFoundException_whenFournisseurNotFound() {
        when(fournisseurRepository.findByIdAndEntrepriseId(99L, ENTREPRISE_ID)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> fournisseurService.update(99L, request))
                .isInstanceOf(EntityNotFoundException.class);

        verify(fournisseurRepository, never()).save(any());
    }

    @Test
    void update_shouldThrowDuplicateEmailException_whenEmailUsedByAnotherFournisseur() {
        Fournisseur other = Fournisseur.builder().id(2L).email("other@example.com").build();
        FournisseurRequest updateRequest = new FournisseurRequest(
                "Kamdem", "Paul", "other@example.com", "+237600000002",
                null, "Douala", null, "Cameroun", "photo.png"
        );

        when(fournisseurRepository.findByIdAndEntrepriseId(1L, ENTREPRISE_ID)).thenReturn(Optional.of(fournisseur));
        when(fournisseurRepository.findByEmailAndEntrepriseId("other@example.com", ENTREPRISE_ID)).thenReturn(Optional.of(other));

        assertThatThrownBy(() -> fournisseurService.update(1L, updateRequest))
                .isInstanceOf(DuplicateEmailException.class)
                .hasMessageContaining("other@example.com");

        verify(fournisseurRepository, never()).save(any());
    }

    @Test
    void update_shouldAllowSameEmail_whenEmailUnchanged() {
        when(fournisseurRepository.findByIdAndEntrepriseId(1L, ENTREPRISE_ID)).thenReturn(Optional.of(fournisseur));
        when(fournisseurRepository.findByEmailAndEntrepriseId("paul@example.com", ENTREPRISE_ID)).thenReturn(Optional.of(fournisseur));
        when(fournisseurRepository.save(fournisseur)).thenReturn(fournisseur);
        when(fournisseurMapper.toResponse(fournisseur)).thenReturn(response);

        FournisseurResponse result = fournisseurService.update(1L, request);

        assertThat(result).isEqualTo(response);
        verify(fournisseurMapper).updateEntityFromRequest(request, fournisseur);
    }

    @Test
    void delete_shouldDeleteFournisseur_whenFound() {
        when(fournisseurRepository.findByIdAndEntrepriseId(1L, ENTREPRISE_ID)).thenReturn(Optional.of(fournisseur));

        fournisseurService.delete(1L);

        verify(fournisseurRepository, times(1)).delete(fournisseur);
    }

    @Test
    void delete_shouldThrowEntityNotFoundException_whenNotFound() {
        when(fournisseurRepository.findByIdAndEntrepriseId(99L, ENTREPRISE_ID)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> fournisseurService.delete(99L))
                .isInstanceOf(EntityNotFoundException.class);

        verify(fournisseurRepository, never()).delete(any());
    }
}
