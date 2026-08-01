package cm.kfokam.stock.fournisseur;

import cm.kfokam.stock.auth.CurrentUserService;
import cm.kfokam.stock.entreprise.model.Entreprise;
import cm.kfokam.stock.exception.DuplicateEmailException;
import cm.kfokam.stock.exception.EntityNotFoundException;
import cm.kfokam.stock.fournisseur.dto.FournisseurRequest;
import cm.kfokam.stock.fournisseur.dto.FournisseurResponse;
import cm.kfokam.stock.fournisseur.model.Fournisseur;
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
class FournisseurServiceImpl implements FournisseurService {

    private static final String PHOTO_FOLDER = "fournisseurs";

    private final FournisseurRepository fournisseurRepository;
    private final FournisseurMapper fournisseurMapper;
    private final CurrentUserService currentUserService;
    private final EntityManager entityManager;
    private final FileStorageService fileStorageService;

    @Override
    public FournisseurResponse create(FournisseurRequest request) {
        Long idEntreprise = currentUserService.getCurrentEntrepriseId();
        if (fournisseurRepository.existsByEmailAndEntrepriseId(request.email(), idEntreprise)) {
            throw new DuplicateEmailException("L'email '%s' est déjà utilisé".formatted(request.email()));
        }
        Fournisseur fournisseur = fournisseurMapper.toEntity(request);
        fournisseur.setEntreprise(entityManager.getReference(Entreprise.class, idEntreprise));
        Fournisseur saved = fournisseurRepository.save(fournisseur);
        return fournisseurMapper.toResponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public FournisseurResponse getById(Long id) {
        return fournisseurMapper.toResponse(findFournisseurOrThrow(id));
    }

    @Override
    @Transactional(readOnly = true)
    public Page<FournisseurResponse> getAll(Pageable pageable) {
        return fournisseurRepository.findAllByEntrepriseId(currentUserService.getCurrentEntrepriseId(), pageable)
                .map(fournisseurMapper::toResponse);
    }

    @Override
    public FournisseurResponse update(Long id, FournisseurRequest request) {
        Fournisseur fournisseur = findFournisseurOrThrow(id);

        fournisseurRepository.findByEmailAndEntrepriseId(request.email(), currentUserService.getCurrentEntrepriseId())
                .filter(existing -> !existing.getId().equals(id))
                .ifPresent(existing -> {
                    throw new DuplicateEmailException("L'email '%s' est déjà utilisé".formatted(request.email()));
                });

        fournisseurMapper.updateEntityFromRequest(request, fournisseur);
        return fournisseurMapper.toResponse(fournisseurRepository.save(fournisseur));
    }

    @Override
    public FournisseurResponse uploadPhoto(Long id, MultipartFile file) {
        Fournisseur fournisseur = findFournisseurOrThrow(id);
        String previousPhoto = fournisseur.getPhoto();

        String objectName = fileStorageService.uploadFile(file, PHOTO_FOLDER);
        fournisseur.setPhoto(objectName);
        Fournisseur saved = fournisseurRepository.save(fournisseur);

        if (previousPhoto != null && !previousPhoto.isBlank()) {
            fileStorageService.deleteFile(previousPhoto);
        }

        return fournisseurMapper.toResponse(saved);
    }

    @Override
    public void delete(Long id) {
        Fournisseur fournisseur = findFournisseurOrThrow(id);
        String photo = fournisseur.getPhoto();
        fournisseurRepository.delete(fournisseur);
        if (photo != null && !photo.isBlank()) {
            fileStorageService.deleteFile(photo);
        }
    }

    private Fournisseur findFournisseurOrThrow(Long id) {
        return fournisseurRepository.findByIdAndEntrepriseId(id, currentUserService.getCurrentEntrepriseId())
                .orElseThrow(() -> new EntityNotFoundException("Fournisseur introuvable avec l'id : " + id));
    }
}