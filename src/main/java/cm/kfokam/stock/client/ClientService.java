package cm.kfokam.stock.client;

import cm.kfokam.stock.client.dto.ClientRequest;
import cm.kfokam.stock.client.dto.ClientResponse;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface ClientService {

    ClientResponse create(ClientRequest request);

    ClientResponse getById(Long id);

    List<ClientResponse> getAll();

    ClientResponse update(Long id, ClientRequest request);

    ClientResponse uploadPhoto(Long id, MultipartFile file);

    void delete(Long id);
}