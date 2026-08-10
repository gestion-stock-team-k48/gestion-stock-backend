package cm.kfokam.stock.client;

import cm.kfokam.stock.client.dto.ClientRequest;
import cm.kfokam.stock.client.dto.ClientResponse;
import cm.kfokam.stock.exception.DuplicateEmailException;
import cm.kfokam.stock.exception.EntityNotFoundException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ClientController.class)
@AutoConfigureMockMvc(addFilters = false)
class ClientControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private ClientService clientService;

    private ClientRequest validRequest() {
        return new ClientRequest(
                "Ngono", "Ange", "ange@example.com", "+237600000000",
                null, "Douala", null, "Cameroun", "photo.png"
        );
    }

    private ClientResponse sampleResponse() {
        return new ClientResponse(
                1L, "Ngono", "Ange", "ange@example.com", "+237600000000",
                null, "Douala", null, "Cameroun", "photo.png",
                null, null, null, null
        );
    }

    @Test
    void create_shouldReturn201_whenValidRequest() throws Exception {
        ClientRequest request = validRequest();
        ClientResponse response = sampleResponse();

        when(clientService.create(request)).thenReturn(response);

        mockMvc.perform(post("/clients")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.email").value("ange@example.com"));
    }

    @Test
    void create_shouldReturn400_whenNomIsBlank() throws Exception {
        ClientRequest invalidRequest = new ClientRequest(
                "", "Ange", "ange@example.com", "+237600000000",
                null, "Douala", null, "Cameroun", "photo.png"
        );

        mockMvc.perform(post("/clients")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest());

        verify(clientService, never()).create(any());
    }

    @Test
    void create_shouldReturn400_whenEmailIsInvalid() throws Exception {
        ClientRequest invalidRequest = new ClientRequest(
                "Ngono", "Ange", "not-an-email", "+237600000000",
                null, "Douala", null, "Cameroun", "photo.png"
        );

        mockMvc.perform(post("/clients")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest());

        verify(clientService, never()).create(any());
    }

    @Test
    void create_shouldReturn409_whenEmailAlreadyUsed() throws Exception {
        ClientRequest request = validRequest();

        when(clientService.create(request))
                .thenThrow(new DuplicateEmailException("L'email 'ange@example.com' est déjà utilisé"));

        mockMvc.perform(post("/clients")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict());
    }

    @Test
    void getById_shouldReturn200_whenFound() throws Exception {
        when(clientService.getById(1L)).thenReturn(sampleResponse());

        mockMvc.perform(get("/clients/{id}", 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L));
    }

    @Test
    void getById_shouldReturn404_whenNotFound() throws Exception {
        when(clientService.getById(99L))
                .thenThrow(new EntityNotFoundException("Client introuvable avec l'id : 99"));

        mockMvc.perform(get("/clients/{id}", 99L))
                .andExpect(status().isNotFound());
    }

    @Test
    void getAll_shouldReturn200WithList() throws Exception {
        List<ClientResponse> responses = List.of(
                sampleResponse(),
                new ClientResponse(2L, "Kamga", "Alice", "alice@example.com", null,
                        null, "Yaoundé", null, "Cameroun", null,
                        null, null, null, null)
        );
        Page<ClientResponse> page = new PageImpl<>(responses);
        when(clientService.getAll(any(Pageable.class))).thenReturn(page);

        mockMvc.perform(get("/clients"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(2))
                .andExpect(jsonPath("$.content[0].email").value("ange@example.com"))
                .andExpect(jsonPath("$.content[1].email").value("alice@example.com"));
    }

    @Test
    void update_shouldReturn200_whenValidRequest() throws Exception {
        ClientRequest request = validRequest();
        ClientResponse response = sampleResponse();

        when(clientService.update(eq(1L), any(ClientRequest.class))).thenReturn(response);

        mockMvc.perform(put("/clients/{id}", 1L)
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value("ange@example.com"));
    }

    @Test
    void update_shouldReturn404_whenClientNotFound() throws Exception {
        ClientRequest request = validRequest();

        when(clientService.update(eq(99L), any(ClientRequest.class)))
                .thenThrow(new EntityNotFoundException("Client introuvable avec l'id : 99"));

        mockMvc.perform(put("/clients/{id}", 99L)
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound());
    }

    @Test
    void update_shouldReturn409_whenEmailAlreadyUsedByAnotherClient() throws Exception {
        ClientRequest request = validRequest();

        when(clientService.update(eq(1L), any(ClientRequest.class)))
                .thenThrow(new DuplicateEmailException("L'email 'ange@example.com' est déjà utilisé"));

        mockMvc.perform(put("/clients/{id}", 1L)
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict());
    }

    @Test
    void delete_shouldReturn204_whenFound() throws Exception {
        mockMvc.perform(delete("/clients/{id}", 1L))
                .andExpect(status().isNoContent())
                .andExpect(content().string(""));

        verify(clientService).delete(1L);
    }

    @Test
    void delete_shouldReturn404_whenNotFound() throws Exception {
        org.mockito.Mockito.doThrow(new EntityNotFoundException("Client introuvable avec l'id : 99"))
                .when(clientService).delete(99L);

        mockMvc.perform(delete("/clients/{id}", 99L))
                .andExpect(status().isNotFound());
    }

    @Test
    void uploadPhoto_shouldReturn200_whenValidFile() throws Exception {
        MockMultipartFile file = new MockMultipartFile("file", "test.txt", "text/plain", "test".getBytes());

        when(clientService.uploadPhoto(eq(1L), any())).thenReturn(sampleResponse());

        mockMvc.perform(multipart("/clients/{id}/photo", 1L).file(file))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L));
    }

    @Test
    void uploadPhoto_shouldReturn404_whenClientNotFound() throws Exception {
        MockMultipartFile file = new MockMultipartFile("file", "test.txt", "text/plain", "test".getBytes());

        when(clientService.uploadPhoto(eq(99L), any()))
                .thenThrow(new EntityNotFoundException("Client introuvable avec l'id : 99"));

        mockMvc.perform(multipart("/clients/{id}/photo", 99L).file(file))
                .andExpect(status().isNotFound());
    }

}