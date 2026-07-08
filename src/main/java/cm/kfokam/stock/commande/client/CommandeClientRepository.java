package cm.kfokam.stock.commande.client;

import cm.kfokam.stock.commande.client.model.CommandeClient;
import org.springframework.data.jpa.repository.JpaRepository;

interface CommandeClientRepository extends JpaRepository<CommandeClient, Long> {
}