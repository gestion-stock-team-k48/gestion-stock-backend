package cm.kfokam.stock.commandeclient;

import cm.kfokam.stock.commandeclient.model.CommandeClient;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

interface CommandeClientRepository extends JpaRepository<CommandeClient, Long> {

    boolean existsByCodeCommande(String codeCommande);

    Optional<CommandeClient> findByCodeCommande(String codeCommande);

    long countByCodeCommandeStartingWith(String prefix);
}