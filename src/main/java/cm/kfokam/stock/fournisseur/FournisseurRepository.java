package cm.kfokam.stock.fournisseur;

import cm.kfokam.stock.fournisseur.model.Fournisseur;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

interface FournisseurRepository extends JpaRepository<Fournisseur, Long> {

    boolean existsByEmailAndEntrepriseId(String email, Long entrepriseId);

    Optional<Fournisseur> findByEmailAndEntrepriseId(String email, Long entrepriseId);

    Optional<Fournisseur> findByIdAndEntrepriseId(Long id, Long entrepriseId);

    List<Fournisseur> findAllByEntrepriseId(Long entrepriseId);
}