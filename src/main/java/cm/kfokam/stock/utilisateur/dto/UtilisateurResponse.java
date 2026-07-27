package cm.kfokam.stock.utilisateur.dto;

import cm.kfokam.stock.utilisateur.model.Role;

import java.time.LocalDate;
import java.util.Set;

public record UtilisateurResponse(
        Long id,
        String nom,
        String prenom,
        String email,
        LocalDate dateDeNaissance,
        String photo,
        String rue,
        String ville,
        String codePostal,
        String pays,
        Long entrepriseId,
        String entrepriseNom,
        Set<Role> roles,
        boolean mustChangePassword
) {
}
