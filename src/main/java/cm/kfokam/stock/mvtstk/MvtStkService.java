package cm.kfokam.stock.mvtstk;

import cm.kfokam.stock.mvtstk.dto.AlerteStockResponse;
import cm.kfokam.stock.mvtstk.dto.MvtStkCorrectionRequest;
import cm.kfokam.stock.mvtstk.dto.MvtStkRequest;
import cm.kfokam.stock.mvtstk.dto.MvtStkResponse;

import java.math.BigDecimal;
import java.util.List;

public interface MvtStkService {

    BigDecimal stockReelArticle(Long idArticle);

    List<MvtStkResponse> mvtStkArticle(Long idArticle);

    List<AlerteStockResponse> articlesEnAlerte();

    MvtStkResponse entreeStock(MvtStkRequest request);

    MvtStkResponse sortieStock(MvtStkRequest request);

    MvtStkResponse correctionStockPos(MvtStkCorrectionRequest request);

    MvtStkResponse correctionStockNeg(MvtStkCorrectionRequest request);
}