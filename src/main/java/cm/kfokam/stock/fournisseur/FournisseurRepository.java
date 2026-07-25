package cm.kfokam.stock.fournisseur;

import cm.kfokam.stock.fournisseur.model.Fournisseur;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

interface FournisseurRepository extends JpaRepository<Fournisseur, Long> {

    boolean existsByEmail(String email);

    Optional<Fournisseur> findByEmail(String email);
}