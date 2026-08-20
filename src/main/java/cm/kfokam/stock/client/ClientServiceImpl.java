package cm.kfokam.stock.client;

import cm.kfokam.stock.auth.CurrentUserService;
import cm.kfokam.stock.client.dto.ClientRequest;
import cm.kfokam.stock.client.dto.ClientResponse;
import cm.kfokam.stock.client.model.Client;
import cm.kfokam.stock.entreprise.model.Entreprise;
import cm.kfokam.stock.exception.DuplicateEmailException;
import cm.kfokam.stock.exception.EntityNotFoundException;
import cm.kfokam.stock.storage.FileStorageService;
import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

@Service
@RequiredArgsConstructor
@Transactional
class ClientServiceImpl implements ClientService {

    private static final String PHOTO_FOLDER = "clients";

    private final ClientRepository clientRepository;
    private final ClientMapper clientMapper;
    private final CurrentUserService currentUserService;
    private final EntityManager entityManager;
    private final FileStorageService fileStorageService;

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
    public Page<ClientResponse> getAll(Pageable pageable) {
        return clientRepository.findAllByEntrepriseId(currentUserService.getCurrentEntrepriseId(), pageable)
                .map(clientMapper::toResponse);
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
    public ClientResponse uploadPhoto(Long id, MultipartFile file) {
        Client client = findClientOrThrow(id);
        String previousPhoto = client.getPhoto();

        String objectName = fileStorageService.uploadFile(file, PHOTO_FOLDER);
        client.setPhoto(objectName);
        Client saved = clientRepository.save(client);

        if (previousPhoto != null && !previousPhoto.isBlank()) {
            fileStorageService.deleteFile(previousPhoto);
        }

        return clientMapper.toResponse(saved);
    }

    @Override
    public void delete(Long id) {
        Client client = findClientOrThrow(id);
        String photo = client.getPhoto();
        clientRepository.delete(client);
        if (photo != null && !photo.isBlank()) {
            fileStorageService.deleteFile(photo);
        }
    }

    private Client findClientOrThrow(Long id) {
        return clientRepository.findByIdAndEntrepriseId(id, currentUserService.getCurrentEntrepriseId())
                .orElseThrow(() -> new EntityNotFoundException("Client introuvable avec l'id : " + id));
    }
}