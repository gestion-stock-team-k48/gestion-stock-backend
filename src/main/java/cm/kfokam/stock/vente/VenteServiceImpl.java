package cm.kfokam.stock.vente;

import cm.kfokam.stock.article.ArticleService;
import cm.kfokam.stock.article.dto.ArticleResponse;
import cm.kfokam.stock.article.model.Article;
import cm.kfokam.stock.auth.CurrentUserService;
import cm.kfokam.stock.exception.DuplicateCodeException;
import cm.kfokam.stock.exception.EntityNotFoundException;
import cm.kfokam.stock.mvtstk.MvtStkService;
import cm.kfokam.stock.mvtstk.dto.MvtStkRequest;
import cm.kfokam.stock.mvtstk.model.SourceMvtStk;
import cm.kfokam.stock.vente.dto.VenteRequest;
import cm.kfokam.stock.vente.dto.VenteResponse;
import cm.kfokam.stock.vente.dto.ligneVente.LigneVenteRequest;
import cm.kfokam.stock.vente.model.LigneVente;
import cm.kfokam.stock.vente.model.Vente;
import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.Year;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
class VenteServiceImpl implements VenteService {

    private static final String CODE_PREFIX = "VT";

    private final VenteRepository venteRepository;
    private final VenteMapper venteMapper;
    private final ArticleService articleService;
    private final MvtStkService mvtStkService;
    private final CurrentUserService currentUserService;
    private final EntityManager entityManager;

    @Override
    public VenteResponse create(VenteRequest request) {
        Long idEntreprise = currentUserService.getCurrentEntrepriseId();
        String code = resolveCode(request.code(), idEntreprise);

        Vente vente = venteMapper.toEntity(request);
        vente.setCode(code);
        vente.setDateVente(Instant.now());
        vente.setIdEntreprise(idEntreprise);

        List<LigneVente> lignes = buildLignes(request.lignes(), vente);
        vente.setLignes(lignes);

        Vente saved = venteRepository.save(vente);

        for (LigneVente ligne : saved.getLignes()) {
            mvtStkService.sortieStock(new MvtStkRequest(
                    ligne.getArticle().getId(),
                    ligne.getQuantite(),
                    SourceMvtStk.VENTE
            ));
        }

        return venteMapper.toResponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public VenteResponse getById(Long id) {
        return venteMapper.toResponse(findVenteOrThrow(id));
    }

    @Override
    @Transactional(readOnly = true)
    public VenteResponse getByCode(String code) {
        Vente vente = venteRepository.findByCodeAndIdEntreprise(code, currentUserService.getCurrentEntrepriseId())
                .orElseThrow(() -> new EntityNotFoundException("Vente introuvable avec le code : " + code));
        return venteMapper.toResponse(vente);
    }

    @Override
    @Transactional(readOnly = true)
    public List<VenteResponse> getAll() {
        return venteMapper.toResponseList(venteRepository.findAllByIdEntreprise(currentUserService.getCurrentEntrepriseId()));
    }

    @Override
    public void delete(Long id) {
        Vente vente = findVenteOrThrow(id);
        venteRepository.delete(vente);
    }

    private List<LigneVente> buildLignes(List<LigneVenteRequest> requests, Vente vente) {
        List<LigneVente> lignes = new ArrayList<>();
        for (LigneVenteRequest ligneRequest : requests) {
            ArticleResponse article = articleService.getById(ligneRequest.articleId());
            lignes.add(LigneVente.builder()
                    .vente(vente)
                    .article(entityManager.getReference(Article.class, article.id()))
                    .quantite(ligneRequest.quantite())
                    .prixUnitaire(article.prixUnitaireTtc())
                    .idEntreprise(vente.getIdEntreprise())
                    .build());
        }
        return lignes;
    }

    private String resolveCode(String code, Long idEntreprise) {
        if (code != null && !code.isBlank()) {
            if (venteRepository.existsByCodeAndIdEntreprise(code, idEntreprise)) {
                throw new DuplicateCodeException("Le code '%s' est déjà utilisé".formatted(code));
            }
            return code;
        }
        return generateCode(idEntreprise);
    }

    private String generateCode(Long idEntreprise) {
        String prefix = "%s-%d-".formatted(CODE_PREFIX, Year.now().getValue());
        long sequence = venteRepository.countByCodeStartingWithAndIdEntreprise(prefix, idEntreprise) + 1;
        return "%s%04d".formatted(prefix, sequence);
    }

    private Vente findVenteOrThrow(Long id) {
        return venteRepository.findByIdAndIdEntreprise(id, currentUserService.getCurrentEntrepriseId())
                .orElseThrow(() -> new EntityNotFoundException("Vente introuvable avec l'id : " + id));
    }
}