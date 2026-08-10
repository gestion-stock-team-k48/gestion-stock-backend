package cm.kfokam.stock.client;

import cm.kfokam.stock.auth.CurrentUserService;
import cm.kfokam.stock.client.dto.ClientRequest;
import cm.kfokam.stock.client.dto.ClientResponse;
import cm.kfokam.stock.client.model.Client;
import cm.kfokam.stock.common.entity.Adresse;
import cm.kfokam.stock.exception.DuplicateEmailException;
import cm.kfokam.stock.exception.EntityNotFoundException;
import cm.kfokam.stock.storage.FileStorageService;
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
import org.springframework.web.multipart.MultipartFile;

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
class ClientServiceImplTest {

    private static final Long ENTREPRISE_ID = 1L;

    @Mock
    private ClientRepository clientRepository;

    @Mock
    private ClientMapper clientMapper;

    @Mock
    private CurrentUserService currentUserService;

    @Mock
    private EntityManager entityManager;

    @Mock
    private FileStorageService fileStorageService;

    @InjectMocks
    private ClientServiceImpl clientService;

    private Client client;
    private ClientRequest request;
    private ClientResponse response;

    @BeforeEach
    void setUp() {
        when(currentUserService.getCurrentEntrepriseId()).thenReturn(ENTREPRISE_ID);

        client = Client.builder()
                .id(1L)
                .nom("Ngono")
                .prenom("ange")
                .email("ange@example.com")
                .numTel("+237600000000")
                .adresse(Adresse.builder().ville("Douala").pays("Cameroun").build())
                .photo("photo.png")
                .build();

        request = new ClientRequest(
                "Ngono", "ange", "ange@example.com", "+237600000000",
                null, "Douala", null, "Cameroun", "photo.png"
        );

        response = new ClientResponse(
                1L, "Ngono", "ange", "ange@example.com", "+237600000000",
                null, "Douala", null, "Cameroun", "photo.png",
                null, null, null, null
        );
    }

    @Test
    void create_shouldReturnResponse_whenEmailNotUsed() {
        when(clientRepository.existsByEmailAndEntrepriseId("ange@example.com", ENTREPRISE_ID)).thenReturn(false);
        when(clientMapper.toEntity(request)).thenReturn(client);
        when(clientRepository.save(client)).thenReturn(client);
        when(clientMapper.toResponse(client)).thenReturn(response);

        ClientResponse result = clientService.create(request);

        assertThat(result).isEqualTo(response);
        verify(clientRepository).save(client);
    }

    @Test
    void create_shouldThrowDuplicateEmailException_whenEmailAlreadyUsed() {
        when(clientRepository.existsByEmailAndEntrepriseId("ange@example.com", ENTREPRISE_ID)).thenReturn(true);

        assertThatThrownBy(() -> clientService.create(request))
                .isInstanceOf(DuplicateEmailException.class)
                .hasMessageContaining("ange@example.com");

        verify(clientRepository, never()).save(any());
    }

    @Test
    void getById_shouldReturnResponse_whenFound() {
        when(clientRepository.findByIdAndEntrepriseId(1L, ENTREPRISE_ID)).thenReturn(Optional.of(client));
        when(clientMapper.toResponse(client)).thenReturn(response);

        ClientResponse result = clientService.getById(1L);

        assertThat(result).isEqualTo(response);
    }

    @Test
    void getById_shouldThrowEntityNotFoundException_whenNotFound() {
        when(clientRepository.findByIdAndEntrepriseId(99L, ENTREPRISE_ID)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> clientService.getById(99L))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessageContaining("99");
    }

    @Test
    void getAll_shouldReturnPageOfResponses() {
        Pageable pageable = PageRequest.of(0, 20);
        Page<Client> clientPage = new PageImpl<>(List.of(client));

        when(clientRepository.findAllByEntrepriseId(ENTREPRISE_ID, pageable)).thenReturn(clientPage);
        when(clientMapper.toResponse(client)).thenReturn(response);

        Page<ClientResponse> result = clientService.getAll(pageable);

        assertThat(result.getContent()).containsExactly(response);
    }

    @Test
    void update_shouldReturnUpdatedResponse_whenValid() {
        ClientRequest updateRequest = new ClientRequest(
                "Ngono", "ange", "new@example.com", "+237600000001",
                null, "Yaoundé", null, "Cameroun", "photo2.png"
        );
        Client updatedClient = Client.builder().id(1L).nom("Ngono").prenom("ange")
                .email("new@example.com").numTel("+237600000001")
                .adresse(Adresse.builder().ville("Yaoundé").pays("Cameroun").build())
                .photo("photo2.png").build();
        ClientResponse updatedResponse = new ClientResponse(
                1L, "Ngono", "ange", "new@example.com", "+237600000001",
                null, "Yaoundé", null, "Cameroun", "photo2.png",
                null, null, null, null
        );

        when(clientRepository.findByIdAndEntrepriseId(1L, ENTREPRISE_ID)).thenReturn(Optional.of(client));
        when(clientRepository.findByEmailAndEntrepriseId("new@example.com", ENTREPRISE_ID)).thenReturn(Optional.empty());
        doAnswer(invocation -> {
            client.setEmail("new@example.com");
            client.setNumTel("+237600000001");
            return null;
        }).when(clientMapper).updateEntityFromRequest(updateRequest, client);
        when(clientRepository.save(client)).thenReturn(updatedClient);
        when(clientMapper.toResponse(updatedClient)).thenReturn(updatedResponse);

        ClientResponse result = clientService.update(1L, updateRequest);

        assertThat(result).isEqualTo(updatedResponse);
    }

    @Test
    void update_shouldThrowEntityNotFoundException_whenClientNotFound() {
        when(clientRepository.findByIdAndEntrepriseId(99L, ENTREPRISE_ID)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> clientService.update(99L, request))
                .isInstanceOf(EntityNotFoundException.class);

        verify(clientRepository, never()).save(any());
    }

    @Test
    void update_shouldThrowDuplicateEmailException_whenEmailUsedByAnotherClient() {
        Client otherClient = Client.builder().id(2L).email("other@example.com").build();
        ClientRequest updateRequest = new ClientRequest(
                "Ngono", "ange", "other@example.com", "+237600000000",
                null, "Douala", null, "Cameroun", "photo.png"
        );

        when(clientRepository.findByIdAndEntrepriseId(1L, ENTREPRISE_ID)).thenReturn(Optional.of(client));
        when(clientRepository.findByEmailAndEntrepriseId("other@example.com", ENTREPRISE_ID)).thenReturn(Optional.of(otherClient));

        assertThatThrownBy(() -> clientService.update(1L, updateRequest))
                .isInstanceOf(DuplicateEmailException.class)
                .hasMessageContaining("other@example.com");

        verify(clientRepository, never()).save(any());
    }

    @Test
    void update_shouldAllowSameEmail_whenEmailUnchanged() {
        when(clientRepository.findByIdAndEntrepriseId(1L, ENTREPRISE_ID)).thenReturn(Optional.of(client));
        when(clientRepository.findByEmailAndEntrepriseId("ange@example.com", ENTREPRISE_ID)).thenReturn(Optional.of(client));
        when(clientRepository.save(client)).thenReturn(client);
        when(clientMapper.toResponse(client)).thenReturn(response);

        ClientResponse result = clientService.update(1L, request);

        assertThat(result).isEqualTo(response);
        verify(clientMapper).updateEntityFromRequest(request, client);
    }

    @Test
    void uploadPhoto_shouldReplacePhoto_andDeleteOldOne() {
        MultipartFile file = new MockMultipartFile("file", "new.png", "image/png", "content".getBytes());
        when(clientRepository.findByIdAndEntrepriseId(1L, ENTREPRISE_ID)).thenReturn(Optional.of(client));
        when(fileStorageService.uploadFile(file, "clients")).thenReturn("clients/new-uuid.png");
        when(clientRepository.save(client)).thenReturn(client);
        when(clientMapper.toResponse(client)).thenReturn(response);

        ClientResponse result = clientService.uploadPhoto(1L, file);

        assertThat(result).isEqualTo(response);
        assertThat(client.getPhoto()).isEqualTo("clients/new-uuid.png");
        verify(fileStorageService).deleteFile("photo.png");
    }

    @Test
    void uploadPhoto_shouldNotDeleteOldPhoto_whenClientHadNone() {
        client.setPhoto(null);
        MultipartFile file = new MockMultipartFile("file", "new.png", "image/png", "content".getBytes());
        when(clientRepository.findByIdAndEntrepriseId(1L, ENTREPRISE_ID)).thenReturn(Optional.of(client));
        when(fileStorageService.uploadFile(file, "clients")).thenReturn("clients/new-uuid.png");
        when(clientRepository.save(client)).thenReturn(client);
        when(clientMapper.toResponse(client)).thenReturn(response);

        clientService.uploadPhoto(1L, file);

        verify(fileStorageService, never()).deleteFile(any());
    }

    @Test
    void uploadPhoto_shouldThrowEntityNotFoundException_whenClientNotFound() {
        MultipartFile file = new MockMultipartFile("file", "new.png", "image/png", "content".getBytes());
        when(clientRepository.findByIdAndEntrepriseId(99L, ENTREPRISE_ID)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> clientService.uploadPhoto(99L, file))
                .isInstanceOf(EntityNotFoundException.class);

        verify(fileStorageService, never()).uploadFile(any(), any());
    }

    @Test
    void delete_shouldDeleteClient_whenFound() {
        when(clientRepository.findByIdAndEntrepriseId(1L, ENTREPRISE_ID)).thenReturn(Optional.of(client));

        clientService.delete(1L);

        verify(clientRepository, times(1)).delete(client);
        verify(fileStorageService).deleteFile("photo.png");
    }

    @Test
    void delete_shouldNotDeletePhoto_whenPhotoIsBlank() {
        client.setPhoto("   ");
        when(clientRepository.findByIdAndEntrepriseId(1L, ENTREPRISE_ID)).thenReturn(Optional.of(client));

        clientService.delete(1L);

        verify(clientRepository, times(1)).delete(client);
        verify(fileStorageService, never()).deleteFile(any());
    }

    @Test
    void delete_shouldThrowEntityNotFoundException_whenNotFound() {
        when(clientRepository.findByIdAndEntrepriseId(99L, ENTREPRISE_ID)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> clientService.delete(99L))
                .isInstanceOf(EntityNotFoundException.class);

        verify(clientRepository, never()).delete(any());
    }
}
