package cm.kfokam.stock.fournisseur;

import cm.kfokam.stock.exception.DuplicateEmailException;
import cm.kfokam.stock.exception.EntityNotFoundException;
import cm.kfokam.stock.fournisseur.dto.FournisseurRequest;
import cm.kfokam.stock.fournisseur.dto.FournisseurResponse;
import cm.kfokam.stock.fournisseur.model.Fournisseur;
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

    @Override
    public FournisseurResponse create(FournisseurRequest request) {
        if (fournisseurRepository.existsByEmail(request.email())) {
            throw new DuplicateEmailException("L'email '%s' est déjà utilisé".formatted(request.email()));
        }
        Fournisseur fournisseur = fournisseurMapper.toEntity(request);
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
        return fournisseurMapper.toResponseList(fournisseurRepository.findAll());
    }

    @Override
    public FournisseurResponse update(Long id, FournisseurRequest request) {
        Fournisseur fournisseur = findFournisseurOrThrow(id);

        fournisseurRepository.findByEmail(request.email())
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
        return fournisseurRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Fournisseur introuvable avec l'id : " + id));
    }
}