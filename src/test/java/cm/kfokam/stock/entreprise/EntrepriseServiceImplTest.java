package cm.kfokam.stock.entreprise;

import cm.kfokam.stock.entreprise.dto.EntrepriseRequest;
import cm.kfokam.stock.entreprise.dto.EntrepriseResponse;
import cm.kfokam.stock.common.entity.Adresse;
import cm.kfokam.stock.entreprise.model.Entreprise;
import cm.kfokam.stock.exception.DuplicateCodeException;
import cm.kfokam.stock.exception.EntityNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class EntrepriseServiceImplTest {

    @Mock
    private EntrepriseRepository entrepriseRepository;

    @Mock
    private EntrepriseMapper entrepriseMapper;

    @InjectMocks
    private EntrepriseServiceImpl entrepriseService;

    private Entreprise entreprise;
    private EntrepriseRequest request;
    private EntrepriseResponse response;

    @BeforeEach
    void setUp() {
        entreprise = Entreprise.builder()
                .id(1L)
                .nom("Kfokam SARL")
                .description("Gestion de stock")
                .codeFiscal("CF-001")
                .email("contact@kfokam.cm")
                .numTel("+237600000000")
                .siteWeb("https://kfokam.cm")
                .adresse(Adresse.builder().ville("Douala").pays("Cameroun").build())
                .photo("logo.png")
                .build();

        request = new EntrepriseRequest(
                "Kfokam SARL", "Gestion de stock", null, "Douala", null, "Cameroun",
                "CF-001", "logo.png", "contact@kfokam.cm", "+237600000000", "https://kfokam.cm"
        );

        response = new EntrepriseResponse(
                1L, "Kfokam SARL", "Gestion de stock", null, "Douala", null, "Cameroun",
                "CF-001", "logo.png", "contact@kfokam.cm", "+237600000000", "https://kfokam.cm",
                null, null, null, null
        );
    }

    @Test
    void create_shouldReturnResponse_whenCodeFiscalNotUsed() {
        when(entrepriseRepository.existsByCodeFiscal("CF-001")).thenReturn(false);
        when(entrepriseMapper.toEntity(request)).thenReturn(entreprise);
        when(entrepriseRepository.save(entreprise)).thenReturn(entreprise);
        when(entrepriseMapper.toResponse(entreprise)).thenReturn(response);

        EntrepriseResponse result = entrepriseService.create(request);

        assertThat(result).isEqualTo(response);
        verify(entrepriseRepository).save(entreprise);
    }

    @Test
    void create_shouldThrowDuplicateCodeException_whenCodeFiscalAlreadyUsed() {
        when(entrepriseRepository.existsByCodeFiscal("CF-001")).thenReturn(true);

        assertThatThrownBy(() -> entrepriseService.create(request))
                .isInstanceOf(DuplicateCodeException.class)
                .hasMessageContaining("CF-001");

        verify(entrepriseRepository, never()).save(any());
    }

    @Test
    void getById_shouldReturnResponse_whenFound() {
        when(entrepriseRepository.findById(1L)).thenReturn(Optional.of(entreprise));
        when(entrepriseMapper.toResponse(entreprise)).thenReturn(response);

        EntrepriseResponse result = entrepriseService.getById(1L);

        assertThat(result).isEqualTo(response);
    }

    @Test
    void getById_shouldThrowEntityNotFoundException_whenNotFound() {
        when(entrepriseRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> entrepriseService.getById(99L))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessageContaining("99");
    }

    @Test
    void update_shouldReturnUpdatedResponse_whenValid() {
        EntrepriseRequest updateRequest = new EntrepriseRequest(
                "Kfokam SARL", "Nouvelle description", null, "Yaoundé", null, "Cameroun",
                "CF-001", "logo2.png", "contact@kfokam.cm", "+237600000001", "https://kfokam.cm"
        );
        Entreprise updatedEntreprise = Entreprise.builder().id(1L).nom("Kfokam SARL")
                .description("Nouvelle description").codeFiscal("CF-001")
                .email("contact@kfokam.cm").numTel("+237600000001").siteWeb("https://kfokam.cm")
                .adresse(Adresse.builder().ville("Yaoundé").pays("Cameroun").build())
                .photo("logo2.png").build();
        EntrepriseResponse updatedResponse = new EntrepriseResponse(
                1L, "Kfokam SARL", "Nouvelle description", null, "Yaoundé", null, "Cameroun",
                "CF-001", "logo2.png", "contact@kfokam.cm", "+237600000001", "https://kfokam.cm",
                null, null, null, null
        );

        when(entrepriseRepository.findById(1L)).thenReturn(Optional.of(entreprise));
        when(entrepriseRepository.findByCodeFiscal("CF-001")).thenReturn(Optional.of(entreprise));
        doAnswer(invocation -> {
            entreprise.setDescription("Nouvelle description");
            entreprise.setNumTel("+237600000001");
            entreprise.setPhoto("logo2.png");
            return null;
        }).when(entrepriseMapper).updateEntityFromRequest(updateRequest, entreprise);
        when(entrepriseRepository.save(entreprise)).thenReturn(updatedEntreprise);
        when(entrepriseMapper.toResponse(updatedEntreprise)).thenReturn(updatedResponse);

        EntrepriseResponse result = entrepriseService.update(1L, updateRequest);

        assertThat(result).isEqualTo(updatedResponse);
    }

    @Test
    void update_shouldThrowEntityNotFoundException_whenEntrepriseNotFound() {
        when(entrepriseRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> entrepriseService.update(99L, request))
                .isInstanceOf(EntityNotFoundException.class);

        verify(entrepriseRepository, never()).save(any());
    }

    @Test
    void update_shouldThrowDuplicateCodeException_whenCodeFiscalUsedByAnotherEntreprise() {
        Entreprise other = Entreprise.builder().id(2L).codeFiscal("CF-999").build();
        EntrepriseRequest updateRequest = new EntrepriseRequest(
                "Kfokam SARL", "Gestion de stock", null, "Douala", null, "Cameroun",
                "CF-999", "logo.png", "contact@kfokam.cm", "+237600000000", "https://kfokam.cm"
        );

        when(entrepriseRepository.findById(1L)).thenReturn(Optional.of(entreprise));
        when(entrepriseRepository.findByCodeFiscal("CF-999")).thenReturn(Optional.of(other));

        assertThatThrownBy(() -> entrepriseService.update(1L, updateRequest))
                .isInstanceOf(DuplicateCodeException.class)
                .hasMessageContaining("CF-999");

        verify(entrepriseRepository, never()).save(any());
    }
}
