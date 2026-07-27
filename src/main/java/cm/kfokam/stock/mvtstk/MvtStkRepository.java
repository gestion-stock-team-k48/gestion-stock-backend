package cm.kfokam.stock.mvtstk;

import cm.kfokam.stock.mvtstk.model.MvtStk;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

interface MvtStkRepository extends JpaRepository<MvtStk, Long> {

    List<MvtStk> findByArticleIdOrderByDateMvtAsc(Long articleId);
}