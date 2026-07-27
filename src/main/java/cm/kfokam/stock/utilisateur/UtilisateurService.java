package cm.kfokam.stock.utilisateur;

import cm.kfokam.stock.utilisateur.dto.ChangePasswordRequest;
import cm.kfokam.stock.utilisateur.dto.UtilisateurRequest;
import cm.kfokam.stock.utilisateur.dto.UtilisateurResponse;

import java.util.List;

public interface UtilisateurService {

    UtilisateurResponse create(UtilisateurRequest request);

    UtilisateurResponse getById(Long id);

    List<UtilisateurResponse> getAll();

    UtilisateurResponse update(Long id, UtilisateurRequest request);

    void delete(Long id);

    void changePassword(Long utilisateurId, ChangePasswordRequest request);
}
