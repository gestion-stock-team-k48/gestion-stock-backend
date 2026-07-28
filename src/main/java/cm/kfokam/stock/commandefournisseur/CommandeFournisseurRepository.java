package cm.kfokam.stock.commandefournisseur;

import cm.kfokam.stock.commandefournisseur.model.CommandeFournisseur;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

interface CommandeFournisseurRepository extends JpaRepository<CommandeFournisseur, Long> {

    boolean existsByCodeCommandeAndEntrepriseId(String codeCommande, Long entrepriseId);

    Optional<CommandeFournisseur> findByCodeCommandeAndEntrepriseId(String codeCommande, Long entrepriseId);

    Optional<CommandeFournisseur> findByIdAndEntrepriseId(Long id, Long entrepriseId);

    List<CommandeFournisseur> findAllByEntrepriseId(Long entrepriseId);

    List<CommandeFournisseur> findAllByFournisseurIdAndEntrepriseIdOrderByDateCommandeDesc(Long fournisseurId, Long entrepriseId);

    long countByCodeCommandeStartingWithAndEntrepriseId(String prefix, Long entrepriseId);
}