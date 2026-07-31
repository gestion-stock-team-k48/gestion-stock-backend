package cm.kfokam.stock.utilisateur;

import cm.kfokam.stock.utilisateur.dto.ChangePasswordRequest;
import cm.kfokam.stock.utilisateur.dto.UtilisateurRequest;
import cm.kfokam.stock.utilisateur.dto.UtilisateurResponse;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;
import java.util.List;

public interface UtilisateurService {

    UtilisateurResponse create(UtilisateurRequest request);

    // Self-registration: caller's own password is used directly, mustChangePassword stays false (unlike create()).
    UtilisateurResponse createInitialAdmin(Long entrepriseId, String nom, String prenom, String email,
                                            String rawPassword, LocalDate dateDeNaissance);

    UtilisateurResponse getById(Long id);

    List<UtilisateurResponse> getAll();

    UtilisateurResponse update(Long id, UtilisateurRequest request);

    UtilisateurResponse uploadPhoto(Long id, MultipartFile file);

    void delete(Long id);

    void changePassword(Long utilisateurId, ChangePasswordRequest request);
}
