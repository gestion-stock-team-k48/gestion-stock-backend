package cm.kfokam.stock.mvtstock;

import cm.kfokam.stock.mvtstock.model.MouvementStock;
import org.springframework.data.jpa.repository.JpaRepository;

interface MouvementStockRepository extends JpaRepository<MouvementStock, Long> {
}