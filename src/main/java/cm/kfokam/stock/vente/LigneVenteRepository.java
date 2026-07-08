package cm.kfokam.stock.vente;

import cm.kfokam.stock.vente.model.LigneVente;
import org.springframework.data.jpa.repository.JpaRepository;

interface LigneVenteRepository extends JpaRepository<LigneVente, Long> {
}