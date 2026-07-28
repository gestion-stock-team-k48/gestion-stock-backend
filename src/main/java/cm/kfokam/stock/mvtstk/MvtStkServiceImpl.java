package cm.kfokam.stock.mvtstk;

import cm.kfokam.stock.article.ArticleService;
import cm.kfokam.stock.article.dto.ArticleResponse;
import cm.kfokam.stock.article.model.Article;
import cm.kfokam.stock.auth.CurrentUserService;
import cm.kfokam.stock.exception.StockInsuffisantException;
import cm.kfokam.stock.mvtstk.dto.AlerteStockResponse;
import cm.kfokam.stock.mvtstk.dto.MvtStkCorrectionRequest;
import cm.kfokam.stock.mvtstk.dto.MvtStkRequest;
import cm.kfokam.stock.mvtstk.dto.MvtStkResponse;
import cm.kfokam.stock.mvtstk.model.MvtStk;
import cm.kfokam.stock.mvtstk.model.SourceMvtStk;
import cm.kfokam.stock.mvtstk.model.TypeMvtStk;
import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
class MvtStkServiceImpl implements MvtStkService {

    private final MvtStkRepository mvtStkRepository;
    private final MvtStkMapper mvtStkMapper;
    private final ArticleService articleService;
    private final CurrentUserService currentUserService;
    private final EntityManager entityManager;

    @Override
    @Transactional(readOnly = true)
    public BigDecimal stockReelArticle(Long idArticle) {
        articleService.getById(idArticle);
        return calculerStockReel(idArticle, currentUserService.getCurrentEntrepriseId());
    }

    @Override
    @Transactional(readOnly = true)
    public List<AlerteStockResponse> articlesEnAlerte() {
        Long idEntreprise = currentUserService.getCurrentEntrepriseId();

        return articleService.getAll().stream()
                .map(article -> new AlerteStockResponse(
                        article.id(), article.code(), article.designation(),
                        calculerStockReel(article.id(), idEntreprise), article.seuilMinimum()))
                .filter(alerte -> alerte.quantiteStock().compareTo(alerte.seuilMinimum()) <= 0)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<MvtStkResponse> mvtStkArticle(Long idArticle) {
        articleService.getById(idArticle);
        Long idEntreprise = currentUserService.getCurrentEntrepriseId();

        return mvtStkMapper.toResponseList(
                mvtStkRepository.findByArticleIdAndIdEntrepriseOrderByDateMvtAsc(idArticle, idEntreprise));
    }

    @Override
    public MvtStkResponse entreeStock(MvtStkRequest request) {
        return enregistrerMouvement(request.articleId(), request.quantite(), request.sourceMvt(), null, TypeMvtStk.ENTREE);
    }

    @Override
    public MvtStkResponse sortieStock(MvtStkRequest request) {
        return enregistrerMouvement(request.articleId(), request.quantite(), request.sourceMvt(), null, TypeMvtStk.SORTIE);
    }

    @Override
    public MvtStkResponse correctionStockPos(MvtStkCorrectionRequest request) {
        return enregistrerMouvement(request.articleId(), request.quantite(), SourceMvtStk.CORRECTION_MANUELLE,
                request.motif(), TypeMvtStk.CORRECTION_POS);
    }

    @Override
    public MvtStkResponse correctionStockNeg(MvtStkCorrectionRequest request) {
        return enregistrerMouvement(request.articleId(), request.quantite(), SourceMvtStk.CORRECTION_MANUELLE,
                request.motif(), TypeMvtStk.CORRECTION_NEG);
    }

    private MvtStkResponse enregistrerMouvement(Long articleId, BigDecimal quantite, SourceMvtStk sourceMvt,
                                                  String motif, TypeMvtStk typeMvt) {
        ArticleResponse article = articleService.getById(articleId);
        Long idEntreprise = currentUserService.getCurrentEntrepriseId();

        if (typeMvt == TypeMvtStk.SORTIE || typeMvt == TypeMvtStk.CORRECTION_NEG) {
            BigDecimal stockActuel = calculerStockReel(article.id(), idEntreprise);
            if (stockActuel.compareTo(quantite) < 0) {
                throw new StockInsuffisantException(
                        "Stock insuffisant pour l'article '%s' : stock actuel %s, quantité demandée %s"
                                .formatted(article.designation(), stockActuel, quantite));
            }
        }

        MvtStk mvtStk = MvtStk.builder()
                .dateMvt(Instant.now())
                .quantite(quantite)
                .article(entityManager.getReference(Article.class, article.id()))
                .typeMvt(typeMvt)
                .sourceMvt(sourceMvt)
                .motif(motif)
                .idEntreprise(idEntreprise)
                .build();

        return mvtStkMapper.toResponse(mvtStkRepository.save(mvtStk));
    }

    private BigDecimal calculerStockReel(Long idArticle, Long idEntreprise) {
        return mvtStkRepository.findByArticleIdAndIdEntrepriseOrderByDateMvtAsc(idArticle, idEntreprise).stream()
                .map(this::quantiteSignee)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private BigDecimal quantiteSignee(MvtStk mvtStk) {
        return switch (mvtStk.getTypeMvt()) {
            case ENTREE, CORRECTION_POS -> mvtStk.getQuantite();
            case SORTIE, CORRECTION_NEG -> mvtStk.getQuantite().negate();
        };
    }
}