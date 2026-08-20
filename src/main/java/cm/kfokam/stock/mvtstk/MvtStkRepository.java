package cm.kfokam.stock.mvtstk;

import cm.kfokam.stock.mvtstk.model.MvtStk;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

interface MvtStkRepository extends JpaRepository<MvtStk, Long> {

    Page<MvtStk> findByArticleIdAndIdEntrepriseOrderByDateMvtAsc(Long articleId, Long idEntreprise, Pageable pageable);
}