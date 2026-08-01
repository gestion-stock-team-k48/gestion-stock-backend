package cm.kfokam.stock.mvtstk;

import cm.kfokam.stock.mvtstk.dto.AlerteStockResponse;
import cm.kfokam.stock.mvtstk.dto.MvtStkCorrectionRequest;
import cm.kfokam.stock.mvtstk.dto.MvtStkRequest;
import cm.kfokam.stock.mvtstk.dto.MvtStkResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.util.List;

public interface MvtStkService {

    BigDecimal stockReelArticle(Long idArticle);

    Page<MvtStkResponse> mvtStkArticle(Long idArticle, Pageable pageable);

    List<AlerteStockResponse> articlesEnAlerte();

    MvtStkResponse entreeStock(MvtStkRequest request);

    MvtStkResponse sortieStock(MvtStkRequest request);

    MvtStkResponse correctionStockPos(MvtStkCorrectionRequest request);

    MvtStkResponse correctionStockNeg(MvtStkCorrectionRequest request);
}