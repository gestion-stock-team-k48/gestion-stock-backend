package cm.kfokam.stock.vente;

import cm.kfokam.stock.vente.model.Vente;
import org.springframework.data.jpa.repository.JpaRepository;

interface VenteRepository extends JpaRepository<Vente, Long> {
}