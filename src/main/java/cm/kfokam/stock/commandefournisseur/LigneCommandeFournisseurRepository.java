package cm.kfokam.stock.commandefournisseur;

import cm.kfokam.stock.commandefournisseur.model.LigneCommandeFournisseur;
import org.springframework.data.jpa.repository.JpaRepository;

interface LigneCommandeFournisseurRepository extends JpaRepository<LigneCommandeFournisseur, Long> {
}