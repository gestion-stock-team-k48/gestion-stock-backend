package cm.kfokam.stock.commande.fournisseur;

import cm.kfokam.stock.commande.fournisseur.model.CommandeFournisseur;
import org.springframework.data.jpa.repository.JpaRepository;

interface CommandeFournisseurRepository extends JpaRepository<CommandeFournisseur, Long> {
}