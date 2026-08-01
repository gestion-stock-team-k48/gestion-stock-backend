package cm.kfokam.stock.commandeclient;

import cm.kfokam.stock.commandeclient.model.CommandeClient;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

interface CommandeClientRepository extends JpaRepository<CommandeClient, Long> {

    boolean existsByCodeCommandeAndEntrepriseId(String codeCommande, Long entrepriseId);

    Optional<CommandeClient> findByCodeCommandeAndEntrepriseId(String codeCommande, Long entrepriseId);

    Optional<CommandeClient> findByIdAndEntrepriseId(Long id, Long entrepriseId);

    Page<CommandeClient> findAllByEntrepriseId(Long entrepriseId, Pageable pageable);

    List<CommandeClient> findAllByClientIdAndEntrepriseIdOrderByDateCommandeDesc(Long clientId, Long entrepriseId);

    long countByCodeCommandeStartingWithAndEntrepriseId(String prefix, Long entrepriseId);
}