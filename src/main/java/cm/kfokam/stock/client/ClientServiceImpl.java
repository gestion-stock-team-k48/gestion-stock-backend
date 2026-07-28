package cm.kfokam.stock.client;

import cm.kfokam.stock.auth.CurrentUserService;
import cm.kfokam.stock.client.dto.ClientRequest;
import cm.kfokam.stock.client.dto.ClientResponse;
import cm.kfokam.stock.client.model.Client;
import cm.kfokam.stock.entreprise.model.Entreprise;
import cm.kfokam.stock.exception.DuplicateEmailException;
import cm.kfokam.stock.exception.EntityNotFoundException;
import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
class ClientServiceImpl implements ClientService {

    private final ClientRepository clientRepository;
    private final ClientMapper clientMapper;
    private final CurrentUserService currentUserService;
    private final EntityManager entityManager;

    @Override
    public ClientResponse create(ClientRequest request) {
        Long idEntreprise = currentUserService.getCurrentEntrepriseId();
        if (clientRepository.existsByEmailAndEntrepriseId(request.email(), idEntreprise)) {
            throw new DuplicateEmailException("L'email '%s' est déjà utilisé".formatted(request.email()));
        }
        Client client = clientMapper.toEntity(request);
        client.setEntreprise(entityManager.getReference(Entreprise.class, idEntreprise));
        Client saved = clientRepository.save(client);
        return clientMapper.toResponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public ClientResponse getById(Long id) {
        return clientMapper.toResponse(findClientOrThrow(id));
    }

    @Override
    @Transactional(readOnly = true)
    public List<ClientResponse> getAll() {
        return clientMapper.toResponseList(
                clientRepository.findAllByEntrepriseId(currentUserService.getCurrentEntrepriseId()));
    }

    @Override
    public ClientResponse update(Long id, ClientRequest request) {
        Client client = findClientOrThrow(id);

        clientRepository.findByEmailAndEntrepriseId(request.email(), currentUserService.getCurrentEntrepriseId())
                .filter(existing -> !existing.getId().equals(id))
                .ifPresent(existing -> {
                    throw new DuplicateEmailException("L'email '%s' est déjà utilisé".formatted(request.email()));
                });

        clientMapper.updateEntityFromRequest(request, client);
        return clientMapper.toResponse(clientRepository.save(client));
    }

    @Override
    public void delete(Long id) {
        Client client = findClientOrThrow(id);
        clientRepository.delete(client);
    }

    private Client findClientOrThrow(Long id) {
        return clientRepository.findByIdAndEntrepriseId(id, currentUserService.getCurrentEntrepriseId())
                .orElseThrow(() -> new EntityNotFoundException("Client introuvable avec l'id : " + id));
    }
}