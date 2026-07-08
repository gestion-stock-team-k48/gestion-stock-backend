package cm.kfokam.stock.commande.client;

import cm.kfokam.stock.commande.client.model.LigneCommandeClient;
import org.springframework.data.jpa.repository.JpaRepository;

interface LigneCommandeClientRepository extends JpaRepository<LigneCommandeClient, Long> {
}