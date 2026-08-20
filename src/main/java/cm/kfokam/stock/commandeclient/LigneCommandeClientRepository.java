package cm.kfokam.stock.commandeclient;

import cm.kfokam.stock.commandeclient.model.LigneCommandeClient;
import org.springframework.data.jpa.repository.JpaRepository;

interface LigneCommandeClientRepository extends JpaRepository<LigneCommandeClient, Long> {
}