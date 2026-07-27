package cm.kfokam.stock.entreprise;

import cm.kfokam.stock.entreprise.dto.EntrepriseRequest;
import cm.kfokam.stock.entreprise.dto.EntrepriseResponse;
import cm.kfokam.stock.entreprise.model.Entreprise;
import cm.kfokam.stock.exception.DuplicateCodeException;
import cm.kfokam.stock.exception.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
class EntrepriseServiceImpl implements EntrepriseService {

    private final EntrepriseRepository entrepriseRepository;
    private final EntrepriseMapper entrepriseMapper;

    @Override
    public EntrepriseResponse create(EntrepriseRequest request) {
        if (entrepriseRepository.existsByCodeFiscal(request.codeFiscal())) {
            throw new DuplicateCodeException("Le code fiscal '%s' est déjà utilisé".formatted(request.codeFiscal()));
        }
        Entreprise entreprise = entrepriseMapper.toEntity(request);
        Entreprise saved = entrepriseRepository.save(entreprise);
        return entrepriseMapper.toResponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public EntrepriseResponse getById(Long id) {
        return entrepriseMapper.toResponse(findEntrepriseOrThrow(id));
    }

    @Override
    @Transactional(readOnly = true)
    public List<EntrepriseResponse> getAll() {
        return entrepriseMapper.toResponseList(entrepriseRepository.findAll());
    }

    @Override
    public EntrepriseResponse update(Long id, EntrepriseRequest request) {
        Entreprise entreprise = findEntrepriseOrThrow(id);

        entrepriseRepository.findByCodeFiscal(request.codeFiscal())
                .filter(existing -> !existing.getId().equals(id))
                .ifPresent(existing -> {
                    throw new DuplicateCodeException("Le code fiscal '%s' est déjà utilisé".formatted(request.codeFiscal()));
                });

        entrepriseMapper.updateEntityFromRequest(request, entreprise);
        return entrepriseMapper.toResponse(entrepriseRepository.save(entreprise));
    }

    @Override
    public void delete(Long id) {
        Entreprise entreprise = findEntrepriseOrThrow(id);
        entrepriseRepository.delete(entreprise);
    }

    private Entreprise findEntrepriseOrThrow(Long id) {
        return entrepriseRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Entreprise introuvable avec l'id : " + id));
    }
}
