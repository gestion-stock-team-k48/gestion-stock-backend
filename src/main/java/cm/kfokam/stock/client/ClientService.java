package cm.kfokam.stock.client;

import cm.kfokam.stock.client.dto.ClientRequest;
import cm.kfokam.stock.client.dto.ClientResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.multipart.MultipartFile;

public interface ClientService {

    ClientResponse create(ClientRequest request);

    ClientResponse getById(Long id);

    Page<ClientResponse> getAll(Pageable pageable);

    ClientResponse update(Long id, ClientRequest request);

    ClientResponse uploadPhoto(Long id, MultipartFile file);

    void delete(Long id);
}