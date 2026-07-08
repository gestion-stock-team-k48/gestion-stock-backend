package cm.kfokam.stock.security;

import cm.kfokam.stock.security.model.Utilisateur;
import org.springframework.data.jpa.repository.JpaRepository;

interface UtilisateurRepository extends JpaRepository<Utilisateur, Long> {
}
