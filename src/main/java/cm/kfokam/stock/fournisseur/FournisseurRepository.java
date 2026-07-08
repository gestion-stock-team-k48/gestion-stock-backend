package cm.kfokam.stock.fournisseur;

import cm.kfokam.stock.fournisseur.model.Fournisseur;
import org.springframework.data.jpa.repository.JpaRepository;

interface FournisseurRepository extends JpaRepository<Fournisseur, Long> {
}
