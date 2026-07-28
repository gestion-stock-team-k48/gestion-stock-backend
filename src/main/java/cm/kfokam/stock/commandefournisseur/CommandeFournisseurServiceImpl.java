package cm.kfokam.stock.commandefournisseur;

import cm.kfokam.stock.article.ArticleService;
import cm.kfokam.stock.article.dto.ArticleResponse;
import cm.kfokam.stock.article.model.Article;
import cm.kfokam.stock.auth.CurrentUserService;
import cm.kfokam.stock.commandefournisseur.dto.CommandeFournisseurRequest;
import cm.kfokam.stock.commandefournisseur.dto.CommandeFournisseurResponse;
import cm.kfokam.stock.commandefournisseur.dto.LigneCommandeFournisseurRequest;
import cm.kfokam.stock.commandefournisseur.model.CommandeFournisseur;
import cm.kfokam.stock.commandefournisseur.model.EtatCommande;
import cm.kfokam.stock.commandefournisseur.model.LigneCommandeFournisseur;
import cm.kfokam.stock.entreprise.model.Entreprise;
import cm.kfokam.stock.exception.DuplicateCodeException;
import cm.kfokam.stock.exception.EntityNotFoundException;
import cm.kfokam.stock.exception.InvalidStateTransitionException;
import cm.kfokam.stock.fournisseur.FournisseurService;
import cm.kfokam.stock.fournisseur.dto.FournisseurResponse;
import cm.kfokam.stock.fournisseur.model.Fournisseur;
import cm.kfokam.stock.mvtstk.MvtStkService;
import cm.kfokam.stock.mvtstk.dto.MvtStkRequest;
import cm.kfokam.stock.mvtstk.model.SourceMvtStk;
import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Year;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Service
@RequiredArgsConstructor
@Transactional
class CommandeFournisseurServiceImpl implements CommandeFournisseurService {

    private static final String CODE_PREFIX = "CF";

    private static final Map<EtatCommande, Set<EtatCommande>> TRANSITIONS_AUTORISEES = Map.of(
            EtatCommande.EN_PREPARATION, EnumSet.of(EtatCommande.VALIDEE, EtatCommande.ANNULEE),
            EtatCommande.VALIDEE, EnumSet.of(EtatCommande.LIVREE, EtatCommande.ANNULEE),
            EtatCommande.LIVREE, EnumSet.noneOf(EtatCommande.class),
            EtatCommande.ANNULEE, EnumSet.noneOf(EtatCommande.class)
    );

    private final CommandeFournisseurRepository commandeFournisseurRepository;
    private final CommandeFournisseurMapper commandeFournisseurMapper;
    private final FournisseurService fournisseurService;
    private final ArticleService articleService;
    private final MvtStkService mvtStkService;
    private final CurrentUserService currentUserService;
    private final EntityManager entityManager;

    @Override
    public CommandeFournisseurResponse create(CommandeFournisseurRequest request) {
        Long idEntreprise = currentUserService.getCurrentEntrepriseId();
        String codeCommande = resolveCode(request.codeCommande(), idEntreprise);
        FournisseurResponse fournisseur = fournisseurService.getById(request.idFournisseur());

        CommandeFournisseur commandeFournisseur = commandeFournisseurMapper.toEntity(request);
        commandeFournisseur.setCodeCommande(codeCommande);
        commandeFournisseur.setFournisseur(entityManager.getReference(Fournisseur.class, fournisseur.id()));
        commandeFournisseur.setEtatCommande(EtatCommande.EN_PREPARATION);
        commandeFournisseur.setEntreprise(entityManager.getReference(Entreprise.class, idEntreprise));

        List<LigneCommandeFournisseur> lignes = buildLignes(request.lignes(), commandeFournisseur);
        commandeFournisseur.setLignes(lignes);
        applyTotaux(commandeFournisseur, lignes);

        CommandeFournisseur saved = commandeFournisseurRepository.save(commandeFournisseur);
        return commandeFournisseurMapper.toResponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public CommandeFournisseurResponse getById(Long id) {
        return commandeFournisseurMapper.toResponse(findCommandeOrThrow(id));
    }

    @Override
    @Transactional(readOnly = true)
    public List<CommandeFournisseurResponse> getAll() {
        return commandeFournisseurMapper.toResponseList(
                commandeFournisseurRepository.findAllByEntrepriseId(currentUserService.getCurrentEntrepriseId()));
    }

    @Override
    public CommandeFournisseurResponse update(Long id, CommandeFournisseurRequest request) {
        CommandeFournisseur commandeFournisseur = findCommandeOrThrow(id);

        FournisseurResponse fournisseur = fournisseurService.getById(request.idFournisseur());
        commandeFournisseur.setFournisseur(entityManager.getReference(Fournisseur.class, fournisseur.id()));
        commandeFournisseur.setDateCommande(request.dateCommande());

        commandeFournisseur.getLignes().clear();
        commandeFournisseur.getLignes().addAll(buildLignes(request.lignes(), commandeFournisseur));
        applyTotaux(commandeFournisseur, commandeFournisseur.getLignes());

        return commandeFournisseurMapper.toResponse(commandeFournisseurRepository.save(commandeFournisseur));
    }

    @Override
    public void delete(Long id) {
        CommandeFournisseur commandeFournisseur = findCommandeOrThrow(id);
        commandeFournisseurRepository.delete(commandeFournisseur);
    }

    @Override
    public CommandeFournisseurResponse updateEtatCommande(Long id, EtatCommande nouvelEtat) {
        CommandeFournisseur commandeFournisseur = findCommandeOrThrow(id);
        EtatCommande etatActuel = commandeFournisseur.getEtatCommande();

        if (!TRANSITIONS_AUTORISEES.getOrDefault(etatActuel, EnumSet.noneOf(EtatCommande.class)).contains(nouvelEtat)) {
            throw new InvalidStateTransitionException(
                    "Transition invalide de '%s' vers '%s'".formatted(etatActuel, nouvelEtat));
        }

        commandeFournisseur.setEtatCommande(nouvelEtat);
        CommandeFournisseur saved = commandeFournisseurRepository.save(commandeFournisseur);

        if (nouvelEtat == EtatCommande.LIVREE) {
            for (LigneCommandeFournisseur ligne : saved.getLignes()) {
                mvtStkService.entreeStock(new MvtStkRequest(
                        ligne.getArticle().getId(),
                        BigDecimal.valueOf(ligne.getQuantite()),
                        SourceMvtStk.COMMANDE_FOURNISSEUR
                ));
            }
        }

        return commandeFournisseurMapper.toResponse(saved);
    }

    private List<LigneCommandeFournisseur> buildLignes(List<LigneCommandeFournisseurRequest> requests, CommandeFournisseur commandeFournisseur) {
        List<LigneCommandeFournisseur> lignes = new ArrayList<>();
        for (LigneCommandeFournisseurRequest ligneRequest : requests) {
            ArticleResponse article = articleService.getById(ligneRequest.articleId());
            lignes.add(LigneCommandeFournisseur.builder()
                    .commandeFournisseur(commandeFournisseur)
                    .article(entityManager.getReference(Article.class, article.id()))
                    .quantite(ligneRequest.quantite())
                    .prixUnitaireHt(article.prixUnitaireHt())
                    .prixUnitaireTtc(article.prixUnitaireTtc())
                    .build());
        }
        return lignes;
    }

    private void applyTotaux(CommandeFournisseur commandeFournisseur, List<LigneCommandeFournisseur> lignes) {
        BigDecimal totalHt = lignes.stream()
                .map(ligne -> ligne.getPrixUnitaireHt().multiply(BigDecimal.valueOf(ligne.getQuantite())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal totalTtc = lignes.stream()
                .map(ligne -> ligne.getPrixUnitaireTtc().multiply(BigDecimal.valueOf(ligne.getQuantite())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        commandeFournisseur.setTotalHt(totalHt);
        commandeFournisseur.setTotalTtc(totalTtc);
        commandeFournisseur.setTotalTva(totalTtc.subtract(totalHt));
    }

    private String resolveCode(String codeCommande, Long idEntreprise) {
        if (codeCommande != null && !codeCommande.isBlank()) {
            if (commandeFournisseurRepository.existsByCodeCommandeAndEntrepriseId(codeCommande, idEntreprise)) {
                throw new DuplicateCodeException("Le code '%s' est déjà utilisé".formatted(codeCommande));
            }
            return codeCommande;
        }
        return generateCode(idEntreprise);
    }

    private String generateCode(Long idEntreprise) {
        String prefix = "%s-%d-".formatted(CODE_PREFIX, Year.now().getValue());
        long sequence = commandeFournisseurRepository.countByCodeCommandeStartingWithAndEntrepriseId(prefix, idEntreprise) + 1;
        return "%s%04d".formatted(prefix, sequence);
    }

    private CommandeFournisseur findCommandeOrThrow(Long id) {
        return commandeFournisseurRepository.findByIdAndEntrepriseId(id, currentUserService.getCurrentEntrepriseId())
                .orElseThrow(() -> new EntityNotFoundException("Commande fournisseur introuvable avec l'id : " + id));
    }
}