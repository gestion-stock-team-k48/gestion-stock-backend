package cm.kfokam.stock.utilisateur;

import cm.kfokam.stock.utilisateur.model.Utilisateur;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

interface UtilisateurRepository extends JpaRepository<Utilisateur, Long> {

    boolean existsByEmail(String email);

    Optional<Utilisateur> findByEmail(String email);

    Optional<Utilisateur> findByIdAndEntrepriseId(Long id, Long entrepriseId);

    List<Utilisateur> findAllByEntrepriseId(Long entrepriseId);
}
