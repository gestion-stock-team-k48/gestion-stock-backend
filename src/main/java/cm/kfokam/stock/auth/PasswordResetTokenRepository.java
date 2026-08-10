package cm.kfokam.stock.auth;

import cm.kfokam.stock.auth.model.PasswordResetToken;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

interface PasswordResetTokenRepository extends JpaRepository<PasswordResetToken, Long> {

    Optional<PasswordResetToken> findByToken(String token);

    // Chargées (et non supprimées) pour rester dans le cycle de vie JPA standard : la mutation
    // used=true passe par les entités managées afin que l'auditing (@LastModifiedDate/@LastModifiedBy
    // sur AbstractEntity) s'applique normalement, ce qu'une requête @Modifying en masse contournerait.
    List<PasswordResetToken> findAllByUtilisateurIdAndUsedFalse(Long utilisateurId);
}
