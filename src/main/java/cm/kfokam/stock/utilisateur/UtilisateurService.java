package cm.kfokam.stock.utilisateur;

import cm.kfokam.stock.utilisateur.dto.ChangePasswordRequest;
import cm.kfokam.stock.utilisateur.dto.UtilisateurMeRequest;
import cm.kfokam.stock.utilisateur.dto.UtilisateurRequest;
import cm.kfokam.stock.utilisateur.dto.UtilisateurResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;

public interface UtilisateurService {

    UtilisateurResponse create(UtilisateurRequest request);

    // Self-registration: caller's own password is used directly, mustChangePassword stays false (unlike create()).
    UtilisateurResponse createInitialAdmin(Long entrepriseId, String nom, String prenom, String email,
                                            String rawPassword, LocalDate dateDeNaissance,
                                            String rue, String ville, String codePostal, String pays);

    UtilisateurResponse getById(Long id);

    Page<UtilisateurResponse> getAll(Pageable pageable);

    UtilisateurResponse update(Long id, UtilisateurRequest request);

    // Self-service: no email/roles change allowed here, unlike update() (ADMIN-only, full profile).
    UtilisateurResponse updateMine(Long id, UtilisateurMeRequest request);

    UtilisateurResponse uploadPhoto(Long id, MultipartFile file);

    void delete(Long id);

    void changePassword(Long utilisateurId, ChangePasswordRequest request);

    // Forgot-password flow: no old-password check (the caller isn't authenticated), unlike changePassword().
    // Not entreprise-scoped either: the caller already resolved this exact user via a single-use reset token.
    void resetPassword(Long utilisateurId, String newRawPassword);
}
