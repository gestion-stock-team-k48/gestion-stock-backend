package cm.kfokam.stock.utilisateur.dto;

import cm.kfokam.stock.utilisateur.model.Role;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Past;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;
import java.util.Set;

public record UtilisateurRequest(

        @NotBlank(message = "Le nom est obligatoire")
        @Size(max = 100, message = "Le nom ne doit pas dépasser 100 caractères")
        String nom,

        @NotBlank(message = "Le prénom est obligatoire")
        @Size(max = 100, message = "Le prénom ne doit pas dépasser 100 caractères")
        String prenom,

        @NotBlank(message = "L'email est obligatoire")
        @Email(message = "L'email doit être valide")
        @Size(max = 150, message = "L'email ne doit pas dépasser 150 caractères")
        String email,

        @Past(message = "La date de naissance doit être dans le passé")
        LocalDate dateDeNaissance,

        String photo,

        @Size(max = 150, message = "La rue ne doit pas dépasser 150 caractères")
        String rue,

        @Size(max = 100, message = "La ville ne doit pas dépasser 100 caractères")
        String ville,

        @Size(max = 20, message = "Le code postal ne doit pas dépasser 20 caractères")
        String codePostal,

        @Size(max = 100, message = "Le pays ne doit pas dépasser 100 caractères")
        String pays,

        @NotEmpty(message = "Au moins un rôle est obligatoire")
        Set<Role> roles
) {
}
