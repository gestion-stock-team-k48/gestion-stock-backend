package cm.kfokam.stock.client;

import cm.kfokam.stock.client.dto.ClientRequest;
import cm.kfokam.stock.client.dto.ClientResponse;
import cm.kfokam.stock.client.model.Client;
import cm.kfokam.stock.entreprise.model.Adresse;
import cm.kfokam.stock.exception.DuplicateEmailException;
import cm.kfokam.stock.exception.EntityNotFoundException;
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
class ClientServiceImplTest {

    @Mock
    private ClientRepository clientRepository;

    @Mock
    private ClientMapper clientMapper;

    @InjectMocks
    private ClientServiceImpl clientService;

    private Client client;
    private ClientRequest request;
    private ClientResponse response;

    @BeforeEach
    void setUp() {
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
                null, "Douala", null, "Cameroun", "photo.png"
        );
    }

    @Test
    void create_shouldReturnResponse_whenEmailNotUsed() {
        when(clientRepository.existsByEmail("ange@example.com")).thenReturn(false);
        when(clientMapper.toEntity(request)).thenReturn(client);
        when(clientRepository.save(client)).thenReturn(client);
        when(clientMapper.toResponse(client)).thenReturn(response);

        ClientResponse result = clientService.create(request);

        assertThat(result).isEqualTo(response);
        verify(clientRepository).save(client);
    }

    @Test
    void create_shouldThrowDuplicateEmailException_whenEmailAlreadyUsed() {
        when(clientRepository.existsByEmail("ange@example.com")).thenReturn(true);

        assertThatThrownBy(() -> clientService.create(request))
                .isInstanceOf(DuplicateEmailException.class)
                .hasMessageContaining("ange@example.com");

        verify(clientRepository, never()).save(any());
    }

    @Test
    void getById_shouldReturnResponse_whenFound() {
        when(clientRepository.findById(1L)).thenReturn(Optional.of(client));
        when(clientMapper.toResponse(client)).thenReturn(response);

        ClientResponse result = clientService.getById(1L);

        assertThat(result).isEqualTo(response);
    }

    @Test
    void getById_shouldThrowEntityNotFoundException_whenNotFound() {
        when(clientRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> clientService.getById(99L))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessageContaining("99");
    }

    @Test
    void getAll_shouldReturnListOfResponses() {
        List<Client> clients = List.of(client);
        List<ClientResponse> responses = List.of(response);

        when(clientRepository.findAll()).thenReturn(clients);
        when(clientMapper.toResponseList(clients)).thenReturn(responses);

        List<ClientResponse> result = clientService.getAll();

        assertThat(result).containsExactly(response);
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
                null, "Yaoundé", null, "Cameroun", "photo2.png"
        );

        when(clientRepository.findById(1L)).thenReturn(Optional.of(client));
        when(clientRepository.findByEmail("new@example.com")).thenReturn(Optional.empty());
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
        when(clientRepository.findById(99L)).thenReturn(Optional.empty());

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

        when(clientRepository.findById(1L)).thenReturn(Optional.of(client));
        when(clientRepository.findByEmail("other@example.com")).thenReturn(Optional.of(otherClient));

        assertThatThrownBy(() -> clientService.update(1L, updateRequest))
                .isInstanceOf(DuplicateEmailException.class)
                .hasMessageContaining("other@example.com");

        verify(clientRepository, never()).save(any());
    }

    @Test
    void update_shouldAllowSameEmail_whenEmailUnchanged() {
        when(clientRepository.findById(1L)).thenReturn(Optional.of(client));
        when(clientRepository.findByEmail("ange@example.com")).thenReturn(Optional.of(client));
        when(clientRepository.save(client)).thenReturn(client);
        when(clientMapper.toResponse(client)).thenReturn(response);

        ClientResponse result = clientService.update(1L, request);

        assertThat(result).isEqualTo(response);
        verify(clientMapper).updateEntityFromRequest(request, client);
    }

    @Test
    void delete_shouldDeleteClient_whenFound() {
        when(clientRepository.findById(1L)).thenReturn(Optional.of(client));

        clientService.delete(1L);

        verify(clientRepository, times(1)).delete(client);
    }

    @Test
    void delete_shouldThrowEntityNotFoundException_whenNotFound() {
        when(clientRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> clientService.delete(99L))
                .isInstanceOf(EntityNotFoundException.class);

        verify(clientRepository, never()).delete(any());
    }
}