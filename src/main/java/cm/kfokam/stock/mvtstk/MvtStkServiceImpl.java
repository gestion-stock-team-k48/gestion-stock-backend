package cm.kfokam.stock.mvtstk;

import cm.kfokam.stock.article.ArticleService;
import cm.kfokam.stock.article.dto.ArticleResponse;
import cm.kfokam.stock.article.model.Article;
import cm.kfokam.stock.mvtstk.dto.MvtStkRequest;
import cm.kfokam.stock.mvtstk.dto.MvtStkResponse;
import cm.kfokam.stock.mvtstk.model.MvtStk;
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
    private final EntityManager entityManager;

    @Override
    @Transactional(readOnly = true)
    public BigDecimal stockReelArticle(Long idArticle) {
        articleService.getById(idArticle);

        return mvtStkRepository.findByArticleIdOrderByDateMvtAsc(idArticle).stream()
                .map(this::quantiteSignee)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    @Override
    @Transactional(readOnly = true)
    public List<MvtStkResponse> mvtStkArticle(Long idArticle) {
        articleService.getById(idArticle);

        return mvtStkMapper.toResponseList(mvtStkRepository.findByArticleIdOrderByDateMvtAsc(idArticle));
    }

    @Override
    public MvtStkResponse entreeStock(MvtStkRequest request) {
        return enregistrerMouvement(request, TypeMvtStk.ENTREE);
    }

    @Override
    public MvtStkResponse sortieStock(MvtStkRequest request) {
        return enregistrerMouvement(request, TypeMvtStk.SORTIE);
    }

    @Override
    public MvtStkResponse correctionStockPos(MvtStkRequest request) {
        return enregistrerMouvement(request, TypeMvtStk.CORRECTION_POS);
    }

    @Override
    public MvtStkResponse correctionStockNeg(MvtStkRequest request) {
        return enregistrerMouvement(request, TypeMvtStk.CORRECTION_NEG);
    }

    private MvtStkResponse enregistrerMouvement(MvtStkRequest request, TypeMvtStk typeMvt) {
        ArticleResponse article = articleService.getById(request.articleId());

        MvtStk mvtStk = mvtStkMapper.toEntity(request);
        mvtStk.setDateMvt(Instant.now());
        mvtStk.setTypeMvt(typeMvt);
        mvtStk.setArticle(entityManager.getReference(Article.class, article.id()));

        return mvtStkMapper.toResponse(mvtStkRepository.save(mvtStk));
    }

    private BigDecimal quantiteSignee(MvtStk mvtStk) {
        return switch (mvtStk.getTypeMvt()) {
            case ENTREE, CORRECTION_POS -> mvtStk.getQuantite();
            case SORTIE, CORRECTION_NEG -> mvtStk.getQuantite().negate();
        };
    }
}