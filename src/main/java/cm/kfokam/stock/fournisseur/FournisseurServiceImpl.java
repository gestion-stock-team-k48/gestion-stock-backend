package cm.kfokam.stock.fournisseur;

import cm.kfokam.stock.auth.CurrentUserService;
import cm.kfokam.stock.entreprise.model.Entreprise;
import cm.kfokam.stock.exception.DuplicateEmailException;
import cm.kfokam.stock.exception.EntityNotFoundException;
import cm.kfokam.stock.fournisseur.dto.FournisseurRequest;
import cm.kfokam.stock.fournisseur.dto.FournisseurResponse;
import cm.kfokam.stock.fournisseur.model.Fournisseur;
import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
class FournisseurServiceImpl implements FournisseurService {

    private final FournisseurRepository fournisseurRepository;
    private final FournisseurMapper fournisseurMapper;
    private final CurrentUserService currentUserService;
    private final EntityManager entityManager;

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
    public List<FournisseurResponse> getAll() {
        return fournisseurMapper.toResponseList(
                fournisseurRepository.findAllByEntrepriseId(currentUserService.getCurrentEntrepriseId()));
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
    public void delete(Long id) {
        Fournisseur fournisseur = findFournisseurOrThrow(id);
        fournisseurRepository.delete(fournisseur);
    }

    private Fournisseur findFournisseurOrThrow(Long id) {
        return fournisseurRepository.findByIdAndEntrepriseId(id, currentUserService.getCurrentEntrepriseId())
                .orElseThrow(() -> new EntityNotFoundException("Fournisseur introuvable avec l'id : " + id));
    }
}