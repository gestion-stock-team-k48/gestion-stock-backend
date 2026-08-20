package cm.kfokam.stock.auth;

import cm.kfokam.stock.utilisateur.model.Utilisateur;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

@Service
public class CurrentUserService {

    public Utilisateur getCurrentUtilisateur() {
        return (Utilisateur) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
    }

    public Long getCurrentEntrepriseId() {
        return getCurrentUtilisateur().getEntreprise().getId();
    }
}
