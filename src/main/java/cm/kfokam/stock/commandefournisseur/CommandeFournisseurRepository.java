package cm.kfokam.stock.commandefournisseur;

import cm.kfokam.stock.commandefournisseur.model.CommandeFournisseur;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

interface CommandeFournisseurRepository extends JpaRepository<CommandeFournisseur, Long> {

    boolean existsByCodeCommande(String codeCommande);

    Optional<CommandeFournisseur> findByCodeCommande(String codeCommande);

    long countByCodeCommandeStartingWith(String prefix);
}