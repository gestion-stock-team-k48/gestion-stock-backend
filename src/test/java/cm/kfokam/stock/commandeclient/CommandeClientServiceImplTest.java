package cm.kfokam.stock.commandeclient;

import cm.kfokam.stock.article.ArticleService;
import cm.kfokam.stock.article.dto.ArticleResponse;
import cm.kfokam.stock.article.model.Article;
import cm.kfokam.stock.auth.CurrentUserService;
import cm.kfokam.stock.client.ClientService;
import cm.kfokam.stock.client.dto.ClientResponse;
import cm.kfokam.stock.client.model.Client;
import cm.kfokam.stock.commandeclient.dto.CommandeClientRequest;
import cm.kfokam.stock.commandeclient.dto.CommandeClientResponse;
import cm.kfokam.stock.commandeclient.dto.LigneCommandeClientRequest;
import cm.kfokam.stock.commandeclient.model.CommandeClient;
import cm.kfokam.stock.commandeclient.model.EtatCommande;
import cm.kfokam.stock.commandeclient.model.LigneCommandeClient;
import cm.kfokam.stock.email.EmailService;
import cm.kfokam.stock.entreprise.model.Entreprise;
import cm.kfokam.stock.exception.DuplicateCodeException;
import cm.kfokam.stock.exception.EntityNotFoundException;
import cm.kfokam.stock.exception.InvalidOperationException;
import cm.kfokam.stock.exception.InvalidStateTransitionException;
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
class CommandeClientServiceImplTest {

    private static final Long ENTREPRISE_ID = 1L;

    @Mock
    private CommandeClientRepository commandeClientRepository;

    @Mock
    private CommandeClientMapper commandeClientMapper;

    @Mock
    private ClientService clientService;

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
    private CommandeClientServiceImpl commandeClientService;

    private Client client;
    private ClientResponse clientResponse;
    private Article article;
    private ArticleResponse articleResponse;
    private CommandeClient commandeClient;
    private LigneCommandeClient ligne;
    private CommandeClientRequest request;
    private CommandeClientResponse response;

    @BeforeEach
    void setUp() {
        lenient().when(currentUserService.getCurrentEntrepriseId()).thenReturn(ENTREPRISE_ID);

        Entreprise entreprise = Entreprise.builder().id(ENTREPRISE_ID).nom("Kfokam SARL").build();
        lenient().when(entityManager.getReference(Entreprise.class, ENTREPRISE_ID)).thenReturn(entreprise);

        client = Client.builder().id(1L).nom("Doe").prenom("John").email("john@doe.com").build();
        clientResponse = new ClientResponse(1L, "Doe", "John", "john@doe.com", null, null, null, null, null, null,
                null, null, null, null);

        article = Article.builder().id(1L).code("ART-01").designation("Ordinateur portable").build();
        articleResponse = new ArticleResponse(1L, "ART-01", "Ordinateur portable",
                new BigDecimal("500.00"), new BigDecimal("19.25"), new BigDecimal("596.25"),
                null, new BigDecimal("5"), 1L, "Informatique",
                null, null, null, null);

        ligne = LigneCommandeClient.builder()
                .id(1L)
                .article(article)
                .quantite(2)
                .prixUnitaireHt(new BigDecimal("500.00"))
                .prixUnitaireTtc(new BigDecimal("596.25"))
                .build();

        commandeClient = CommandeClient.builder()
                .id(1L)
                .codeCommande("CC-%d-0001".formatted(Year.now().getValue()))
                .dateCommande(LocalDate.of(2026, 7, 27))
                .etatCommande(EtatCommande.EN_PREPARATION)
                .client(client)
                .totalHt(new BigDecimal("1000.00"))
                .totalTva(new BigDecimal("192.50"))
                .totalTtc(new BigDecimal("1192.50"))
                .lignes(new ArrayList<>(List.of(ligne)))
                .build();

        request = new CommandeClientRequest(
                null,
                LocalDate.of(2026, 7, 27),
                1L,
                List.of(new LigneCommandeClientRequest(1L, 2))
        );

        response = new CommandeClientResponse(
                1L,
                "CC-%d-0001".formatted(Year.now().getValue()),
                LocalDate.of(2026, 7, 27),
                EtatCommande.EN_PREPARATION,
                1L, "Doe", "John",
                new BigDecimal("1000.00"), new BigDecimal("192.50"), new BigDecimal("1192.50"),
                List.of(),
                null, null, null, null
        );
    }

    @Test
    void create_shouldReturnResponse_whenValid() {
        when(commandeClientRepository.countByCodeCommandeStartingWithAndEntrepriseId(anyString(), any())).thenReturn(0L);
        when(clientService.getById(1L)).thenReturn(clientResponse);
        when(commandeClientMapper.toEntity(request)).thenReturn(new CommandeClient());
        when(entityManager.getReference(Client.class, 1L)).thenReturn(client);
        when(articleService.getById(1L)).thenReturn(articleResponse);
        when(entityManager.getReference(Article.class, 1L)).thenReturn(article);
        when(commandeClientRepository.save(any(CommandeClient.class))).thenReturn(commandeClient);
        when(commandeClientMapper.toResponse(commandeClient)).thenReturn(response);

        CommandeClientResponse result = commandeClientService.create(request);

        assertThat(result).isEqualTo(response);
        verify(commandeClientRepository).save(any(CommandeClient.class));
    }

    @Test
    void create_shouldSendConfirmationEmail_toClient() {
        when(commandeClientRepository.countByCodeCommandeStartingWithAndEntrepriseId(anyString(), any())).thenReturn(0L);
        when(clientService.getById(1L)).thenReturn(clientResponse);
        when(commandeClientMapper.toEntity(request)).thenReturn(new CommandeClient());
        when(entityManager.getReference(Client.class, 1L)).thenReturn(client);
        when(articleService.getById(1L)).thenReturn(articleResponse);
        when(entityManager.getReference(Article.class, 1L)).thenReturn(article);
        when(commandeClientRepository.save(any(CommandeClient.class))).thenReturn(commandeClient);
        when(commandeClientMapper.toResponse(commandeClient)).thenReturn(response);

        commandeClientService.create(request);

        verify(emailService).envoyerConfirmationCommandeClient("john@doe.com", response);
    }

    @Test
    void create_shouldGenerateCode_whenCodeNotProvided() {
        when(commandeClientRepository.countByCodeCommandeStartingWithAndEntrepriseId(anyString(), any())).thenReturn(4L);
        when(clientService.getById(1L)).thenReturn(clientResponse);
        when(commandeClientMapper.toEntity(request)).thenReturn(new CommandeClient());
        when(entityManager.getReference(Client.class, 1L)).thenReturn(client);
        when(articleService.getById(1L)).thenReturn(articleResponse);
        when(entityManager.getReference(Article.class, 1L)).thenReturn(article);
        when(commandeClientRepository.save(any(CommandeClient.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(commandeClientMapper.toResponse(any(CommandeClient.class))).thenReturn(response);

        commandeClientService.create(request);

        String expectedCode = "CC-%d-0005".formatted(Year.now().getValue());
        verify(commandeClientMapper).toResponse(argThatCodeEquals(expectedCode));
    }

    private CommandeClient argThatCodeEquals(String expectedCode) {
        return org.mockito.ArgumentMatchers.argThat(cc -> cc.getCodeCommande().equals(expectedCode));
    }

    @Test
    void create_shouldUseProvidedCode_whenGivenAndUnique() {
        CommandeClientRequest requestWithCode = new CommandeClientRequest(
                "CC-CUSTOM-01", LocalDate.of(2026, 7, 27), 1L,
                List.of(new LigneCommandeClientRequest(1L, 2))
        );

        when(commandeClientRepository.existsByCodeCommandeAndEntrepriseId("CC-CUSTOM-01", ENTREPRISE_ID)).thenReturn(false);
        when(clientService.getById(1L)).thenReturn(clientResponse);
        when(commandeClientMapper.toEntity(requestWithCode)).thenReturn(new CommandeClient());
        when(entityManager.getReference(Client.class, 1L)).thenReturn(client);
        when(articleService.getById(1L)).thenReturn(articleResponse);
        when(entityManager.getReference(Article.class, 1L)).thenReturn(article);
        when(commandeClientRepository.save(any(CommandeClient.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(commandeClientMapper.toResponse(any(CommandeClient.class))).thenReturn(response);

        commandeClientService.create(requestWithCode);

        verify(commandeClientMapper).toResponse(argThatCodeEquals("CC-CUSTOM-01"));
        verify(commandeClientRepository, never()).countByCodeCommandeStartingWithAndEntrepriseId(anyString(), any());
    }

    @Test
    void create_shouldThrowDuplicateCodeException_whenCodeAlreadyUsed() {
        CommandeClientRequest requestWithCode = new CommandeClientRequest(
                "CC-CUSTOM-01", LocalDate.of(2026, 7, 27), 1L,
                List.of(new LigneCommandeClientRequest(1L, 2))
        );

        when(commandeClientRepository.existsByCodeCommandeAndEntrepriseId("CC-CUSTOM-01", ENTREPRISE_ID)).thenReturn(true);

        assertThatThrownBy(() -> commandeClientService.create(requestWithCode))
                .isInstanceOf(DuplicateCodeException.class)
                .hasMessageContaining("CC-CUSTOM-01");

        verify(clientService, never()).getById(any());
        verify(commandeClientRepository, never()).save(any());
    }

    @Test
    void create_shouldThrowEntityNotFoundException_whenClientNotFound() {
        when(commandeClientRepository.countByCodeCommandeStartingWithAndEntrepriseId(anyString(), any())).thenReturn(0L);
        when(clientService.getById(1L)).thenThrow(new EntityNotFoundException("Client introuvable avec l'id : 1"));

        assertThatThrownBy(() -> commandeClientService.create(request))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessageContaining("1");

        verify(commandeClientRepository, never()).save(any());
    }

    @Test
    void create_shouldThrowEntityNotFoundException_whenArticleNotFound() {
        when(commandeClientRepository.countByCodeCommandeStartingWithAndEntrepriseId(anyString(), any())).thenReturn(0L);
        when(clientService.getById(1L)).thenReturn(clientResponse);
        when(commandeClientMapper.toEntity(request)).thenReturn(new CommandeClient());
        when(entityManager.getReference(Client.class, 1L)).thenReturn(client);
        when(articleService.getById(1L)).thenThrow(new EntityNotFoundException("Article introuvable avec l'id : 1"));

        assertThatThrownBy(() -> commandeClientService.create(request))
                .isInstanceOf(EntityNotFoundException.class);

        verify(commandeClientRepository, never()).save(any());
    }

    @Test
    void create_shouldCalculateTotals_fromLignes() {
        when(commandeClientRepository.countByCodeCommandeStartingWithAndEntrepriseId(anyString(), any())).thenReturn(0L);
        when(clientService.getById(1L)).thenReturn(clientResponse);
        when(commandeClientMapper.toEntity(request)).thenReturn(new CommandeClient());
        when(entityManager.getReference(Client.class, 1L)).thenReturn(client);
        when(articleService.getById(1L)).thenReturn(articleResponse);
        when(entityManager.getReference(Article.class, 1L)).thenReturn(article);
        when(commandeClientRepository.save(any(CommandeClient.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(commandeClientMapper.toResponse(any(CommandeClient.class))).thenReturn(response);

        commandeClientService.create(request);

        verify(commandeClientRepository).save(org.mockito.ArgumentMatchers.argThat(cc ->
                cc.getTotalHt().compareTo(new BigDecimal("1000.00")) == 0
                        && cc.getTotalTtc().compareTo(new BigDecimal("1192.50")) == 0
                        && cc.getTotalTva().compareTo(new BigDecimal("192.50")) == 0
        ));
    }

    @Test
    void getById_shouldReturnResponse_whenFound() {
        when(commandeClientRepository.findByIdAndEntrepriseId(1L, ENTREPRISE_ID)).thenReturn(Optional.of(commandeClient));
        when(commandeClientMapper.toResponse(commandeClient)).thenReturn(response);

        CommandeClientResponse result = commandeClientService.getById(1L);

        assertThat(result).isEqualTo(response);
    }

    @Test
    void getById_shouldThrowEntityNotFoundException_whenNotFound() {
        when(commandeClientRepository.findByIdAndEntrepriseId(99L, ENTREPRISE_ID)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> commandeClientService.getById(99L))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessageContaining("99");
    }

    @Test
    void getAll_shouldReturnPageOfResponses() {
        Pageable pageable = PageRequest.of(0, 20);
        Page<CommandeClient> commandePage = new PageImpl<>(List.of(commandeClient));

        when(commandeClientRepository.findAllByEntrepriseId(ENTREPRISE_ID, pageable)).thenReturn(commandePage);
        when(commandeClientMapper.toResponse(commandeClient)).thenReturn(response);

        Page<CommandeClientResponse> result = commandeClientService.getAll(pageable);

        assertThat(result.getContent()).containsExactly(response);
    }

    @Test
    void getHistoriqueByClient_shouldReturnCommandesForThatClient() {
        List<CommandeClient> commandes = List.of(commandeClient);
        List<CommandeClientResponse> responses = List.of(response);

        when(clientService.getById(1L)).thenReturn(clientResponse);
        when(commandeClientRepository.findAllByClientIdAndEntrepriseIdOrderByDateCommandeDesc(1L, ENTREPRISE_ID))
                .thenReturn(commandes);
        when(commandeClientMapper.toResponseList(commandes)).thenReturn(responses);

        List<CommandeClientResponse> result = commandeClientService.getHistoriqueByClient(1L);

        assertThat(result).containsExactly(response);
    }

    @Test
    void getHistoriqueByClient_shouldThrowEntityNotFoundException_whenClientNotFound() {
        when(clientService.getById(99L)).thenThrow(new EntityNotFoundException("Client introuvable avec l'id : 99"));

        assertThatThrownBy(() -> commandeClientService.getHistoriqueByClient(99L))
                .isInstanceOf(EntityNotFoundException.class);

        verify(commandeClientRepository, never()).findAllByClientIdAndEntrepriseIdOrderByDateCommandeDesc(any(), any());
    }

    @Test
    void update_shouldReturnUpdatedResponse_whenValid() {
        CommandeClientRequest updateRequest = new CommandeClientRequest(
                null, LocalDate.of(2026, 8, 1), 1L,
                List.of(new LigneCommandeClientRequest(1L, 3))
        );

        when(commandeClientRepository.findByIdAndEntrepriseId(1L, ENTREPRISE_ID)).thenReturn(Optional.of(commandeClient));
        when(clientService.getById(1L)).thenReturn(clientResponse);
        when(entityManager.getReference(Client.class, 1L)).thenReturn(client);
        when(articleService.getById(1L)).thenReturn(articleResponse);
        when(entityManager.getReference(Article.class, 1L)).thenReturn(article);
        when(commandeClientRepository.save(commandeClient)).thenReturn(commandeClient);
        when(commandeClientMapper.toResponse(commandeClient)).thenReturn(response);

        CommandeClientResponse result = commandeClientService.update(1L, updateRequest);

        assertThat(result).isEqualTo(response);
        assertThat(commandeClient.getDateCommande()).isEqualTo(LocalDate.of(2026, 8, 1));
        assertThat(commandeClient.getLignes()).hasSize(1);
        assertThat(commandeClient.getTotalHt()).isEqualByComparingTo(new BigDecimal("1500.00"));
    }

    @Test
    void update_shouldThrowEntityNotFoundException_whenCommandeNotFound() {
        when(commandeClientRepository.findByIdAndEntrepriseId(99L, ENTREPRISE_ID)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> commandeClientService.update(99L, request))
                .isInstanceOf(EntityNotFoundException.class);

        verify(commandeClientRepository, never()).save(any());
    }

    @Test
    void update_shouldThrowEntityNotFoundException_whenClientNotFound() {
        when(commandeClientRepository.findByIdAndEntrepriseId(1L, ENTREPRISE_ID)).thenReturn(Optional.of(commandeClient));
        when(clientService.getById(1L)).thenThrow(new EntityNotFoundException("Client introuvable avec l'id : 1"));

        assertThatThrownBy(() -> commandeClientService.update(1L, request))
                .isInstanceOf(EntityNotFoundException.class);

        verify(commandeClientRepository, never()).save(any());
    }

    @Test
    void delete_shouldDeleteCommande_whenFoundAndNotLivree() {
        when(commandeClientRepository.findByIdAndEntrepriseId(1L, ENTREPRISE_ID)).thenReturn(Optional.of(commandeClient));

        commandeClientService.delete(1L);

        verify(commandeClientRepository, times(1)).delete(commandeClient);
    }

    @Test
    void delete_shouldThrowInvalidOperationException_whenCommandeIsLivree() {
        commandeClient.setEtatCommande(EtatCommande.LIVREE);
        when(commandeClientRepository.findByIdAndEntrepriseId(1L, ENTREPRISE_ID)).thenReturn(Optional.of(commandeClient));

        assertThatThrownBy(() -> commandeClientService.delete(1L))
                .isInstanceOf(InvalidOperationException.class)
                .hasMessageContaining("Impossible de supprimer une commande client à l'état LIVREE afin de préserver l'intégrité des mouvements de stock.");

        verify(commandeClientRepository, never()).delete(any());
    }

    @Test
    void delete_shouldThrowEntityNotFoundException_whenNotFound() {
        when(commandeClientRepository.findByIdAndEntrepriseId(99L, ENTREPRISE_ID)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> commandeClientService.delete(99L))
                .isInstanceOf(EntityNotFoundException.class);

        verify(commandeClientRepository, never()).delete(any());
    }

    @Test
    void updateEtatCommande_shouldTransitionToValidee_whenEnPreparation() {
        when(commandeClientRepository.findByIdAndEntrepriseId(1L, ENTREPRISE_ID)).thenReturn(Optional.of(commandeClient));
        when(commandeClientRepository.save(commandeClient)).thenReturn(commandeClient);
        when(commandeClientMapper.toResponse(commandeClient)).thenReturn(response);

        CommandeClientResponse result = commandeClientService.updateEtatCommande(1L, EtatCommande.VALIDEE);

        assertThat(result).isEqualTo(response);
        assertThat(commandeClient.getEtatCommande()).isEqualTo(EtatCommande.VALIDEE);
    }

    @Test
    void updateEtatCommande_shouldTransitionToLivree_whenValidee() {
        commandeClient.setEtatCommande(EtatCommande.VALIDEE);
        when(commandeClientRepository.findByIdAndEntrepriseId(1L, ENTREPRISE_ID)).thenReturn(Optional.of(commandeClient));
        when(commandeClientRepository.save(commandeClient)).thenReturn(commandeClient);
        when(commandeClientMapper.toResponse(commandeClient)).thenReturn(response);

        commandeClientService.updateEtatCommande(1L, EtatCommande.LIVREE);

        assertThat(commandeClient.getEtatCommande()).isEqualTo(EtatCommande.LIVREE);
    }

    @Test
    void updateEtatCommande_shouldTriggerSortieStock_whenTransitioningToLivree() {
        commandeClient.setEtatCommande(EtatCommande.VALIDEE);
        when(commandeClientRepository.findByIdAndEntrepriseId(1L, ENTREPRISE_ID)).thenReturn(Optional.of(commandeClient));
        when(commandeClientRepository.save(commandeClient)).thenReturn(commandeClient);
        when(commandeClientMapper.toResponse(commandeClient)).thenReturn(response);

        commandeClientService.updateEtatCommande(1L, EtatCommande.LIVREE);

        verify(mvtStkService).sortieStock(new MvtStkRequest(1L, new BigDecimal("2"), SourceMvtStk.COMMANDE_CLIENT));
    }

    @Test
    void updateEtatCommande_shouldNotTriggerSortieStock_whenTransitioningToValidee() {
        when(commandeClientRepository.findByIdAndEntrepriseId(1L, ENTREPRISE_ID)).thenReturn(Optional.of(commandeClient));
        when(commandeClientRepository.save(commandeClient)).thenReturn(commandeClient);
        when(commandeClientMapper.toResponse(commandeClient)).thenReturn(response);

        commandeClientService.updateEtatCommande(1L, EtatCommande.VALIDEE);

        verify(mvtStkService, never()).sortieStock(any());
    }

    @Test
    void updateEtatCommande_shouldThrowInvalidStateTransitionException_whenTransitionNotAllowed() {
        commandeClient.setEtatCommande(EtatCommande.LIVREE);
        when(commandeClientRepository.findByIdAndEntrepriseId(1L, ENTREPRISE_ID)).thenReturn(Optional.of(commandeClient));

        assertThatThrownBy(() -> commandeClientService.updateEtatCommande(1L, EtatCommande.VALIDEE))
                .isInstanceOf(InvalidStateTransitionException.class)
                .hasMessageContaining("LIVREE")
                .hasMessageContaining("VALIDEE");

        verify(commandeClientRepository, never()).save(any());
    }

    @Test
    void updateEtatCommande_shouldThrowInvalidStateTransitionException_whenSkippingSteps() {
        when(commandeClientRepository.findByIdAndEntrepriseId(1L, ENTREPRISE_ID)).thenReturn(Optional.of(commandeClient));

        assertThatThrownBy(() -> commandeClientService.updateEtatCommande(1L, EtatCommande.LIVREE))
                .isInstanceOf(InvalidStateTransitionException.class);

        verify(commandeClientRepository, never()).save(any());
    }

    @Test
    void updateEtatCommande_shouldThrowEntityNotFoundException_whenNotFound() {
        when(commandeClientRepository.findByIdAndEntrepriseId(99L, ENTREPRISE_ID)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> commandeClientService.updateEtatCommande(99L, EtatCommande.VALIDEE))
                .isInstanceOf(EntityNotFoundException.class);
    }
}
