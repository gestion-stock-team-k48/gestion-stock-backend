package cm.kfokam.stock.commande.fournisseur;

import cm.kfokam.stock.commande.fournisseur.model.LigneCommandeFournisseur;
import org.springframework.data.jpa.repository.JpaRepository;

interface LigneCommandeFournisseurRepository extends JpaRepository<LigneCommandeFournisseur, Long> {
}