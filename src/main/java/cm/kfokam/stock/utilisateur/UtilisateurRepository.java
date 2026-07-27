package cm.kfokam.stock.utilisateur;

import cm.kfokam.stock.utilisateur.model.Utilisateur;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

interface UtilisateurRepository extends JpaRepository<Utilisateur, Long> {

    boolean existsByEmail(String email);

    Optional<Utilisateur> findByEmail(String email);
}
