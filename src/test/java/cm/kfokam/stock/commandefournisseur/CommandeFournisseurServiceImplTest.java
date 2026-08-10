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
import cm.kfokam.stock.email.EmailService;
import cm.kfokam.stock.entreprise.model.Entreprise;
import cm.kfokam.stock.exception.DuplicateCodeException;
import cm.kfokam.stock.exception.EntityNotFoundException;
import cm.kfokam.stock.exception.InvalidOperationException;
import cm.kfokam.stock.exception.InvalidStateTransitionException;
import cm.kfokam.stock.fournisseur.FournisseurService;
import cm.kfokam.stock.fournisseur.dto.FournisseurResponse;
import cm.kfokam.stock.fournisseur.model.Fournisseur;
import cm.kfokam.stock.mvtstk.MvtStkService;
import cm.kfokam.stock.mvtstk.dto.MvtStkRequest;
import cm.kfokam.stock.mvtstk.model.SourceMvtStk;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.Year;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CommandeFournisseurServiceImplTest {

    private static final Long ENTREPRISE_ID = 1L;

    @Mock
    private CommandeFournisseurRepository commandeFournisseurRepository;

    @Mock
    private CommandeFournisseurMapper commandeFournisseurMapper;

    @Mock
    private FournisseurService fournisseurService;

    @Mock
    private ArticleService articleService;

    @Mock
    private MvtStkService mvtStkService;

    @Mock
    private EmailService emailService;

    @Mock
    private CurrentUserService currentUserService;

    @Mock
    private EntityManager entityManager;

    @InjectMocks
    private CommandeFournisseurServiceImpl commandeFournisseurService;

    private Fournisseur fournisseur;
    private FournisseurResponse fournisseurResponse;
    private Article article;
    private ArticleResponse articleResponse;
    private CommandeFournisseur commandeFournisseur;
    private LigneCommandeFournisseur ligne;
    private CommandeFournisseurRequest request;
    private CommandeFournisseurResponse response;

    @BeforeEach
    void setUp() {
        lenient().when(currentUserService.getCurrentEntrepriseId()).thenReturn(ENTREPRISE_ID);

        Entreprise entreprise = Entreprise.builder().id(ENTREPRISE_ID).nom("Kfokam SARL").build();
        lenient().when(entityManager.getReference(Entreprise.class, ENTREPRISE_ID)).thenReturn(entreprise);

        fournisseur = Fournisseur.builder().id(1L).nom("Martin").prenom("Paul").email("paul@martin.com").build();
        fournisseurResponse = new FournisseurResponse(1L, "Martin", "Paul", "paul@martin.com", null, null, null, null, null, null,
                null, null, null, null);

        article = Article.builder().id(1L).code("ART-01").designation("Ordinateur portable").build();
        articleResponse = new ArticleResponse(1L, "ART-01", "Ordinateur portable",
                new BigDecimal("500.00"), new BigDecimal("19.25"), new BigDecimal("596.25"),
                null, new BigDecimal("5"), 1L, "Informatique",
                null, null, null, null);

        ligne = LigneCommandeFournisseur.builder()
                .id(1L)
                .article(article)
                .quantite(2)
                .prixUnitaireHt(new BigDecimal("500.00"))
                .prixUnitaireTtc(new BigDecimal("596.25"))
                .build();

        commandeFournisseur = CommandeFournisseur.builder()
                .id(1L)
                .codeCommande("CF-%d-0001".formatted(Year.now().getValue()))
                .dateCommande(LocalDate.of(2026, 7, 27))
                .etatCommande(EtatCommande.EN_PREPARATION)
                .fournisseur(fournisseur)
                .totalHt(new BigDecimal("1000.00"))
                .totalTva(new BigDecimal("192.50"))
                .totalTtc(new BigDecimal("1192.50"))
                .lignes(new ArrayList<>(List.of(ligne)))
                .build();

        request = new CommandeFournisseurRequest(
                null,
                LocalDate.of(2026, 7, 27),
                1L,
                List.of(new LigneCommandeFournisseurRequest(1L, 2))
        );

        response = new CommandeFournisseurResponse(
                1L,
                "CF-%d-0001".formatted(Year.now().getValue()),
                LocalDate.of(2026, 7, 27),
                EtatCommande.EN_PREPARATION,
                1L, "Martin", "Paul",
                new BigDecimal("1000.00"), new BigDecimal("192.50"), new BigDecimal("1192.50"),
                List.of(),
                null, null, null, null
        );
    }

    @Test
    void create_shouldReturnResponse_whenValid() {
        when(commandeFournisseurRepository.countByCodeCommandeStartingWithAndEntrepriseId(anyString(), any())).thenReturn(0L);
        when(fournisseurService.getById(1L)).thenReturn(fournisseurResponse);
        when(commandeFournisseurMapper.toEntity(request)).thenReturn(new CommandeFournisseur());
        when(entityManager.getReference(Fournisseur.class, 1L)).thenReturn(fournisseur);
        when(articleService.getById(1L)).thenReturn(articleResponse);
        when(entityManager.getReference(Article.class, 1L)).thenReturn(article);
        when(commandeFournisseurRepository.save(any(CommandeFournisseur.class))).thenReturn(commandeFournisseur);
        when(commandeFournisseurMapper.toResponse(commandeFournisseur)).thenReturn(response);

        CommandeFournisseurResponse result = commandeFournisseurService.create(request);

        assertThat(result).isEqualTo(response);
        verify(commandeFournisseurRepository).save(any(CommandeFournisseur.class));
    }

    @Test
    void create_shouldSendOrdreEmail_toFournisseur() {
        when(commandeFournisseurRepository.countByCodeCommandeStartingWithAndEntrepriseId(anyString(), any())).thenReturn(0L);
        when(fournisseurService.getById(1L)).thenReturn(fournisseurResponse);
        when(commandeFournisseurMapper.toEntity(request)).thenReturn(new CommandeFournisseur());
        when(entityManager.getReference(Fournisseur.class, 1L)).thenReturn(fournisseur);
        when(articleService.getById(1L)).thenReturn(articleResponse);
        when(entityManager.getReference(Article.class, 1L)).thenReturn(article);
        when(commandeFournisseurRepository.save(any(CommandeFournisseur.class))).thenReturn(commandeFournisseur);
        when(commandeFournisseurMapper.toResponse(commandeFournisseur)).thenReturn(response);

        commandeFournisseurService.create(request);

        verify(emailService).envoyerOrdreCommandeFournisseur("paul@martin.com", response);
    }

    @Test
    void create_shouldGenerateCode_whenCodeNotProvided() {
        when(commandeFournisseurRepository.countByCodeCommandeStartingWithAndEntrepriseId(anyString(), any())).thenReturn(4L);
        when(fournisseurService.getById(1L)).thenReturn(fournisseurResponse);
        when(commandeFournisseurMapper.toEntity(request)).thenReturn(new CommandeFournisseur());
        when(entityManager.getReference(Fournisseur.class, 1L)).thenReturn(fournisseur);
        when(articleService.getById(1L)).thenReturn(articleResponse);
        when(entityManager.getReference(Article.class, 1L)).thenReturn(article);
        when(commandeFournisseurRepository.save(any(CommandeFournisseur.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(commandeFournisseurMapper.toResponse(any(CommandeFournisseur.class))).thenReturn(response);

        commandeFournisseurService.create(request);

        String expectedCode = "CF-%d-0005".formatted(Year.now().getValue());
        verify(commandeFournisseurMapper).toResponse(argThatCodeEquals(expectedCode));
    }

    private CommandeFournisseur argThatCodeEquals(String expectedCode) {
        return org.mockito.ArgumentMatchers.argThat(cf -> cf.getCodeCommande().equals(expectedCode));
    }

    @Test
    void create_shouldUseProvidedCode_whenGivenAndUnique() {
        CommandeFournisseurRequest requestWithCode = new CommandeFournisseurRequest(
                "CF-CUSTOM-01", LocalDate.of(2026, 7, 27), 1L,
                List.of(new LigneCommandeFournisseurRequest(1L, 2))
        );

        when(commandeFournisseurRepository.existsByCodeCommandeAndEntrepriseId("CF-CUSTOM-01", ENTREPRISE_ID)).thenReturn(false);
        when(fournisseurService.getById(1L)).thenReturn(fournisseurResponse);
        when(commandeFournisseurMapper.toEntity(requestWithCode)).thenReturn(new CommandeFournisseur());
        when(entityManager.getReference(Fournisseur.class, 1L)).thenReturn(fournisseur);
        when(articleService.getById(1L)).thenReturn(articleResponse);
        when(entityManager.getReference(Article.class, 1L)).thenReturn(article);
        when(commandeFournisseurRepository.save(any(CommandeFournisseur.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(commandeFournisseurMapper.toResponse(any(CommandeFournisseur.class))).thenReturn(response);

        commandeFournisseurService.create(requestWithCode);

        verify(commandeFournisseurMapper).toResponse(argThatCodeEquals("CF-CUSTOM-01"));
        verify(commandeFournisseurRepository, never()).countByCodeCommandeStartingWithAndEntrepriseId(anyString(), any());
    }

    @Test
    void create_shouldThrowDuplicateCodeException_whenCodeAlreadyUsed() {
        CommandeFournisseurRequest requestWithCode = new CommandeFournisseurRequest(
                "CF-CUSTOM-01", LocalDate.of(2026, 7, 27), 1L,
                List.of(new LigneCommandeFournisseurRequest(1L, 2))
        );

        when(commandeFournisseurRepository.existsByCodeCommandeAndEntrepriseId("CF-CUSTOM-01", ENTREPRISE_ID)).thenReturn(true);

        assertThatThrownBy(() -> commandeFournisseurService.create(requestWithCode))
                .isInstanceOf(DuplicateCodeException.class)
                .hasMessageContaining("CF-CUSTOM-01");

        verify(fournisseurService, never()).getById(any());
        verify(commandeFournisseurRepository, never()).save(any());
    }

    @Test
    void create_shouldThrowEntityNotFoundException_whenFournisseurNotFound() {
        when(commandeFournisseurRepository.countByCodeCommandeStartingWithAndEntrepriseId(anyString(), any())).thenReturn(0L);
        when(fournisseurService.getById(1L)).thenThrow(new EntityNotFoundException("Fournisseur introuvable avec l'id : 1"));

        assertThatThrownBy(() -> commandeFournisseurService.create(request))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessageContaining("1");

        verify(commandeFournisseurRepository, never()).save(any());
    }

    @Test
    void create_shouldThrowEntityNotFoundException_whenArticleNotFound() {
        when(commandeFournisseurRepository.countByCodeCommandeStartingWithAndEntrepriseId(anyString(), any())).thenReturn(0L);
        when(fournisseurService.getById(1L)).thenReturn(fournisseurResponse);
        when(commandeFournisseurMapper.toEntity(request)).thenReturn(new CommandeFournisseur());
        when(entityManager.getReference(Fournisseur.class, 1L)).thenReturn(fournisseur);
        when(articleService.getById(1L)).thenThrow(new EntityNotFoundException("Article introuvable avec l'id : 1"));

        assertThatThrownBy(() -> commandeFournisseurService.create(request))
                .isInstanceOf(EntityNotFoundException.class);

        verify(commandeFournisseurRepository, never()).save(any());
    }

    @Test
    void create_shouldCalculateTotals_fromLignes() {
        when(commandeFournisseurRepository.countByCodeCommandeStartingWithAndEntrepriseId(anyString(), any())).thenReturn(0L);
        when(fournisseurService.getById(1L)).thenReturn(fournisseurResponse);
        when(commandeFournisseurMapper.toEntity(request)).thenReturn(new CommandeFournisseur());
        when(entityManager.getReference(Fournisseur.class, 1L)).thenReturn(fournisseur);
        when(articleService.getById(1L)).thenReturn(articleResponse);
        when(entityManager.getReference(Article.class, 1L)).thenReturn(article);
        when(commandeFournisseurRepository.save(any(CommandeFournisseur.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(commandeFournisseurMapper.toResponse(any(CommandeFournisseur.class))).thenReturn(response);

        commandeFournisseurService.create(request);

        verify(commandeFournisseurRepository).save(org.mockito.ArgumentMatchers.argThat(cf ->
                cf.getTotalHt().compareTo(new BigDecimal("1000.00")) == 0
                        && cf.getTotalTtc().compareTo(new BigDecimal("1192.50")) == 0
                        && cf.getTotalTva().compareTo(new BigDecimal("192.50")) == 0
        ));
    }

    @Test
    void getById_shouldReturnResponse_whenFound() {
        when(commandeFournisseurRepository.findByIdAndEntrepriseId(1L, ENTREPRISE_ID)).thenReturn(Optional.of(commandeFournisseur));
        when(commandeFournisseurMapper.toResponse(commandeFournisseur)).thenReturn(response);

        CommandeFournisseurResponse result = commandeFournisseurService.getById(1L);

        assertThat(result).isEqualTo(response);
    }

    @Test
    void getById_shouldThrowEntityNotFoundException_whenNotFound() {
        when(commandeFournisseurRepository.findByIdAndEntrepriseId(99L, ENTREPRISE_ID)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> commandeFournisseurService.getById(99L))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessageContaining("99");
    }

    @Test
    void getAll_shouldReturnPageOfResponses() {
        Pageable pageable = PageRequest.of(0, 20);
        Page<CommandeFournisseur> commandePage = new PageImpl<>(List.of(commandeFournisseur));

        when(commandeFournisseurRepository.findAllByEntrepriseId(ENTREPRISE_ID, pageable)).thenReturn(commandePage);
        when(commandeFournisseurMapper.toResponse(commandeFournisseur)).thenReturn(response);

        Page<CommandeFournisseurResponse> result = commandeFournisseurService.getAll(pageable);

        assertThat(result.getContent()).containsExactly(response);
    }

    @Test
    void getHistoriqueByFournisseur_shouldReturnCommandesForThatFournisseur() {
        List<CommandeFournisseur> commandes = List.of(commandeFournisseur);
        List<CommandeFournisseurResponse> responses = List.of(response);

        when(fournisseurService.getById(1L)).thenReturn(fournisseurResponse);
        when(commandeFournisseurRepository.findAllByFournisseurIdAndEntrepriseIdOrderByDateCommandeDesc(1L, ENTREPRISE_ID))
                .thenReturn(commandes);
        when(commandeFournisseurMapper.toResponseList(commandes)).thenReturn(responses);

        List<CommandeFournisseurResponse> result = commandeFournisseurService.getHistoriqueByFournisseur(1L);

        assertThat(result).containsExactly(response);
    }

    @Test
    void getHistoriqueByFournisseur_shouldThrowEntityNotFoundException_whenFournisseurNotFound() {
        when(fournisseurService.getById(99L)).thenThrow(new EntityNotFoundException("Fournisseur introuvable avec l'id : 99"));

        assertThatThrownBy(() -> commandeFournisseurService.getHistoriqueByFournisseur(99L))
                .isInstanceOf(EntityNotFoundException.class);

        verify(commandeFournisseurRepository, never()).findAllByFournisseurIdAndEntrepriseIdOrderByDateCommandeDesc(any(), any());
    }

    @Test
    void update_shouldReturnUpdatedResponse_whenValid() {
        CommandeFournisseurRequest updateRequest = new CommandeFournisseurRequest(
                null, LocalDate.of(2026, 8, 1), 1L,
                List.of(new LigneCommandeFournisseurRequest(1L, 3))
        );

        when(commandeFournisseurRepository.findByIdAndEntrepriseId(1L, ENTREPRISE_ID)).thenReturn(Optional.of(commandeFournisseur));
        when(fournisseurService.getById(1L)).thenReturn(fournisseurResponse);
        when(entityManager.getReference(Fournisseur.class, 1L)).thenReturn(fournisseur);
        when(articleService.getById(1L)).thenReturn(articleResponse);
        when(entityManager.getReference(Article.class, 1L)).thenReturn(article);
        when(commandeFournisseurRepository.save(commandeFournisseur)).thenReturn(commandeFournisseur);
        when(commandeFournisseurMapper.toResponse(commandeFournisseur)).thenReturn(response);

        CommandeFournisseurResponse result = commandeFournisseurService.update(1L, updateRequest);

        assertThat(result).isEqualTo(response);
        assertThat(commandeFournisseur.getDateCommande()).isEqualTo(LocalDate.of(2026, 8, 1));
        assertThat(commandeFournisseur.getLignes()).hasSize(1);
        assertThat(commandeFournisseur.getTotalHt()).isEqualByComparingTo(new BigDecimal("1500.00"));
    }

    @Test
    void update_shouldThrowEntityNotFoundException_whenCommandeNotFound() {
        when(commandeFournisseurRepository.findByIdAndEntrepriseId(99L, ENTREPRISE_ID)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> commandeFournisseurService.update(99L, request))
                .isInstanceOf(EntityNotFoundException.class);

        verify(commandeFournisseurRepository, never()).save(any());
    }

    @Test
    void update_shouldThrowEntityNotFoundException_whenFournisseurNotFound() {
        when(commandeFournisseurRepository.findByIdAndEntrepriseId(1L, ENTREPRISE_ID)).thenReturn(Optional.of(commandeFournisseur));
        when(fournisseurService.getById(1L)).thenThrow(new EntityNotFoundException("Fournisseur introuvable avec l'id : 1"));

        assertThatThrownBy(() -> commandeFournisseurService.update(1L, request))
                .isInstanceOf(EntityNotFoundException.class);

        verify(commandeFournisseurRepository, never()).save(any());
    }

    @Test
    void delete_shouldDeleteCommande_whenFoundAndNotLivree() {
        when(commandeFournisseurRepository.findByIdAndEntrepriseId(1L, ENTREPRISE_ID)).thenReturn(Optional.of(commandeFournisseur));

        commandeFournisseurService.delete(1L);

        verify(commandeFournisseurRepository, times(1)).delete(commandeFournisseur);
    }

    @Test
    void delete_shouldThrowInvalidOperationException_whenCommandeIsLivree() {
        commandeFournisseur.setEtatCommande(EtatCommande.LIVREE);
        when(commandeFournisseurRepository.findByIdAndEntrepriseId(1L, ENTREPRISE_ID)).thenReturn(Optional.of(commandeFournisseur));

        assertThatThrownBy(() -> commandeFournisseurService.delete(1L))
                .isInstanceOf(InvalidOperationException.class)
                .hasMessageContaining("Impossible de supprimer une commande fournisseur à l'état LIVREE afin de préserver l'intégrité des mouvements de stock.");

        verify(commandeFournisseurRepository, never()).delete(any());
    }

    @Test
    void delete_shouldThrowEntityNotFoundException_whenNotFound() {
        when(commandeFournisseurRepository.findByIdAndEntrepriseId(99L, ENTREPRISE_ID)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> commandeFournisseurService.delete(99L))
                .isInstanceOf(EntityNotFoundException.class);

        verify(commandeFournisseurRepository, never()).delete(any());
    }

    @Test
    void updateEtatCommande_shouldTransitionToValidee_whenEnPreparation() {
        when(commandeFournisseurRepository.findByIdAndEntrepriseId(1L, ENTREPRISE_ID)).thenReturn(Optional.of(commandeFournisseur));
        when(commandeFournisseurRepository.save(commandeFournisseur)).thenReturn(commandeFournisseur);
        when(commandeFournisseurMapper.toResponse(commandeFournisseur)).thenReturn(response);

        CommandeFournisseurResponse result = commandeFournisseurService.updateEtatCommande(1L, EtatCommande.VALIDEE);

        assertThat(result).isEqualTo(response);
        assertThat(commandeFournisseur.getEtatCommande()).isEqualTo(EtatCommande.VALIDEE);
    }

    @Test
    void updateEtatCommande_shouldTransitionToLivree_whenValidee() {
        commandeFournisseur.setEtatCommande(EtatCommande.VALIDEE);
        when(commandeFournisseurRepository.findByIdAndEntrepriseId(1L, ENTREPRISE_ID)).thenReturn(Optional.of(commandeFournisseur));
        when(commandeFournisseurRepository.save(commandeFournisseur)).thenReturn(commandeFournisseur);
        when(commandeFournisseurMapper.toResponse(commandeFournisseur)).thenReturn(response);

        commandeFournisseurService.updateEtatCommande(1L, EtatCommande.LIVREE);

        assertThat(commandeFournisseur.getEtatCommande()).isEqualTo(EtatCommande.LIVREE);
    }

    @Test
    void updateEtatCommande_shouldTriggerEntreeStock_whenTransitioningToLivree() {
        commandeFournisseur.setEtatCommande(EtatCommande.VALIDEE);
        when(commandeFournisseurRepository.findByIdAndEntrepriseId(1L, ENTREPRISE_ID)).thenReturn(Optional.of(commandeFournisseur));
        when(commandeFournisseurRepository.save(commandeFournisseur)).thenReturn(commandeFournisseur);
        when(commandeFournisseurMapper.toResponse(commandeFournisseur)).thenReturn(response);

        commandeFournisseurService.updateEtatCommande(1L, EtatCommande.LIVREE);

        verify(mvtStkService).entreeStock(new MvtStkRequest(1L, new BigDecimal("2"), SourceMvtStk.COMMANDE_FOURNISSEUR));
    }

    @Test
    void updateEtatCommande_shouldNotTriggerEntreeStock_whenTransitioningToValidee() {
        when(commandeFournisseurRepository.findByIdAndEntrepriseId(1L, ENTREPRISE_ID)).thenReturn(Optional.of(commandeFournisseur));
        when(commandeFournisseurRepository.save(commandeFournisseur)).thenReturn(commandeFournisseur);
        when(commandeFournisseurMapper.toResponse(commandeFournisseur)).thenReturn(response);

        commandeFournisseurService.updateEtatCommande(1L, EtatCommande.VALIDEE);

        verify(mvtStkService, never()).entreeStock(any());
    }

    @Test
    void updateEtatCommande_shouldThrowInvalidStateTransitionException_whenTransitionNotAllowed() {
        commandeFournisseur.setEtatCommande(EtatCommande.LIVREE);
        when(commandeFournisseurRepository.findByIdAndEntrepriseId(1L, ENTREPRISE_ID)).thenReturn(Optional.of(commandeFournisseur));

        assertThatThrownBy(() -> commandeFournisseurService.updateEtatCommande(1L, EtatCommande.VALIDEE))
                .isInstanceOf(InvalidStateTransitionException.class)
                .hasMessageContaining("LIVREE")
                .hasMessageContaining("VALIDEE");

        verify(commandeFournisseurRepository, never()).save(any());
    }

    @Test
    void updateEtatCommande_shouldThrowInvalidStateTransitionException_whenSkippingSteps() {
        when(commandeFournisseurRepository.findByIdAndEntrepriseId(1L, ENTREPRISE_ID)).thenReturn(Optional.of(commandeFournisseur));

        assertThatThrownBy(() -> commandeFournisseurService.updateEtatCommande(1L, EtatCommande.LIVREE))
                .isInstanceOf(InvalidStateTransitionException.class);

        verify(commandeFournisseurRepository, never()).save(any());
    }

    @Test
    void updateEtatCommande_shouldThrowEntityNotFoundException_whenNotFound() {
        when(commandeFournisseurRepository.findByIdAndEntrepriseId(99L, ENTREPRISE_ID)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> commandeFournisseurService.updateEtatCommande(99L, EtatCommande.VALIDEE))
                .isInstanceOf(EntityNotFoundException.class);
    }
}
