package cm.kfokam.stock.commandeclient;

import cm.kfokam.stock.article.ArticleService;
import cm.kfokam.stock.article.dto.ArticleResponse;
import cm.kfokam.stock.article.model.Article;
import cm.kfokam.stock.client.ClientService;
import cm.kfokam.stock.client.dto.ClientResponse;
import cm.kfokam.stock.client.model.Client;
import cm.kfokam.stock.commandeclient.dto.CommandeClientRequest;
import cm.kfokam.stock.commandeclient.dto.CommandeClientResponse;
import cm.kfokam.stock.commandeclient.dto.LigneCommandeClientRequest;
import cm.kfokam.stock.commandeclient.model.CommandeClient;
import cm.kfokam.stock.commandeclient.model.EtatCommande;
import cm.kfokam.stock.commandeclient.model.LigneCommandeClient;
import cm.kfokam.stock.exception.DuplicateCodeException;
import cm.kfokam.stock.exception.EntityNotFoundException;
import cm.kfokam.stock.exception.InvalidStateTransitionException;
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
class CommandeClientServiceImpl implements CommandeClientService {

    private static final String CODE_PREFIX = "CC";

    private static final Map<EtatCommande, Set<EtatCommande>> TRANSITIONS_AUTORISEES = Map.of(
            EtatCommande.EN_PREPARATION, EnumSet.of(EtatCommande.VALIDEE, EtatCommande.ANNULEE),
            EtatCommande.VALIDEE, EnumSet.of(EtatCommande.LIVREE, EtatCommande.ANNULEE),
            EtatCommande.LIVREE, EnumSet.noneOf(EtatCommande.class),
            EtatCommande.ANNULEE, EnumSet.noneOf(EtatCommande.class)
    );

    private final CommandeClientRepository commandeClientRepository;
    private final CommandeClientMapper commandeClientMapper;
    private final ClientService clientService;
    private final ArticleService articleService;
    private final EntityManager entityManager;

    @Override
    public CommandeClientResponse create(CommandeClientRequest request) {
        String codeCommande = resolveCode(request.codeCommande());
        ClientResponse client = clientService.getById(request.idClient());

        CommandeClient commandeClient = commandeClientMapper.toEntity(request);
        commandeClient.setCodeCommande(codeCommande);
        commandeClient.setClient(entityManager.getReference(Client.class, client.id()));
        commandeClient.setEtatCommande(EtatCommande.EN_PREPARATION);

        List<LigneCommandeClient> lignes = buildLignes(request.lignes(), commandeClient);
        commandeClient.setLignes(lignes);
        applyTotaux(commandeClient, lignes);

        CommandeClient saved = commandeClientRepository.save(commandeClient);
        return commandeClientMapper.toResponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public CommandeClientResponse getById(Long id) {
        return commandeClientMapper.toResponse(findCommandeOrThrow(id));
    }

    @Override
    @Transactional(readOnly = true)
    public List<CommandeClientResponse> getAll() {
        return commandeClientMapper.toResponseList(commandeClientRepository.findAll());
    }

    @Override
    public CommandeClientResponse update(Long id, CommandeClientRequest request) {
        CommandeClient commandeClient = findCommandeOrThrow(id);

        ClientResponse client = clientService.getById(request.idClient());
        commandeClient.setClient(entityManager.getReference(Client.class, client.id()));
        commandeClient.setDateCommande(request.dateCommande());

        commandeClient.getLignes().clear();
        commandeClient.getLignes().addAll(buildLignes(request.lignes(), commandeClient));
        applyTotaux(commandeClient, commandeClient.getLignes());

        return commandeClientMapper.toResponse(commandeClientRepository.save(commandeClient));
    }

    @Override
    public void delete(Long id) {
        CommandeClient commandeClient = findCommandeOrThrow(id);
        commandeClientRepository.delete(commandeClient);
    }

    @Override
    public CommandeClientResponse updateEtatCommande(Long id, EtatCommande nouvelEtat) {
        CommandeClient commandeClient = findCommandeOrThrow(id);
        EtatCommande etatActuel = commandeClient.getEtatCommande();

        if (!TRANSITIONS_AUTORISEES.getOrDefault(etatActuel, EnumSet.noneOf(EtatCommande.class)).contains(nouvelEtat)) {
            throw new InvalidStateTransitionException(
                    "Transition invalide de '%s' vers '%s'".formatted(etatActuel, nouvelEtat));
        }

        commandeClient.setEtatCommande(nouvelEtat);
        return commandeClientMapper.toResponse(commandeClientRepository.save(commandeClient));
    }

    private List<LigneCommandeClient> buildLignes(List<LigneCommandeClientRequest> requests, CommandeClient commandeClient) {
        List<LigneCommandeClient> lignes = new ArrayList<>();
        for (LigneCommandeClientRequest ligneRequest : requests) {
            ArticleResponse article = articleService.getById(ligneRequest.articleId());
            lignes.add(LigneCommandeClient.builder()
                    .commandeClient(commandeClient)
                    .article(entityManager.getReference(Article.class, article.id()))
                    .quantite(ligneRequest.quantite())
                    .prixUnitaireHt(article.prixUnitaireHt())
                    .prixUnitaireTtc(article.prixUnitaireTtc())
                    .build());
        }
        return lignes;
    }

    private void applyTotaux(CommandeClient commandeClient, List<LigneCommandeClient> lignes) {
        BigDecimal totalHt = lignes.stream()
                .map(ligne -> ligne.getPrixUnitaireHt().multiply(BigDecimal.valueOf(ligne.getQuantite())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal totalTtc = lignes.stream()
                .map(ligne -> ligne.getPrixUnitaireTtc().multiply(BigDecimal.valueOf(ligne.getQuantite())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        commandeClient.setTotalHt(totalHt);
        commandeClient.setTotalTtc(totalTtc);
        commandeClient.setTotalTva(totalTtc.subtract(totalHt));
    }

    private String resolveCode(String codeCommande) {
        if (codeCommande != null && !codeCommande.isBlank()) {
            if (commandeClientRepository.existsByCodeCommande(codeCommande)) {
                throw new DuplicateCodeException("Le code '%s' est déjà utilisé".formatted(codeCommande));
            }
            return codeCommande;
        }
        return generateCode();
    }

    private String generateCode() {
        String prefix = "%s-%d-".formatted(CODE_PREFIX, Year.now().getValue());
        long sequence = commandeClientRepository.countByCodeCommandeStartingWith(prefix) + 1;
        return "%s%04d".formatted(prefix, sequence);
    }

    private CommandeClient findCommandeOrThrow(Long id) {
        return commandeClientRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Commande client introuvable avec l'id : " + id));
    }
}