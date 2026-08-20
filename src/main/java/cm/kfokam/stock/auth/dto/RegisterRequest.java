package cm.kfokam.stock.auth.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Past;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

public record RegisterRequest(

        @NotBlank(message = "Le nom de l'entreprise est obligatoire")
        @Size(max = 150, message = "Le nom de l'entreprise ne doit pas dépasser 150 caractères")
        String nomEntreprise,

        @Size(max = 1000, message = "La description ne doit pas dépasser 1000 caractères")
        String description,

        @Size(max = 150, message = "La rue ne doit pas dépasser 150 caractères")
        String rue,

        @Size(max = 100, message = "La ville ne doit pas dépasser 100 caractères")
        String ville,

        @Size(max = 20, message = "Le code postal ne doit pas dépasser 20 caractères")
        String codePostal,

        @Size(max = 100, message = "Le pays ne doit pas dépasser 100 caractères")
        String pays,

        @NotBlank(message = "Le code fiscal est obligatoire")
        @Size(max = 30, message = "Le code fiscal ne doit pas dépasser 30 caractères")
        String codeFiscal,

        @NotBlank(message = "L'email de l'entreprise est obligatoire")
        @Email(message = "L'email de l'entreprise doit être valide")
        @Size(max = 150, message = "L'email de l'entreprise ne doit pas dépasser 150 caractères")
        String email,

        @Size(max = 20, message = "Le numéro de téléphone ne doit pas dépasser 20 caractères")
        String numTel,

        @Size(max = 150, message = "Le site web ne doit pas dépasser 150 caractères")
        String siteWeb,

        @NotBlank(message = "Le nom de l'administrateur est obligatoire")
        @Size(max = 100, message = "Le nom de l'administrateur ne doit pas dépasser 100 caractères")
        String nomAdmin,

        @NotBlank(message = "Le prénom de l'administrateur est obligatoire")
        @Size(max = 100, message = "Le prénom de l'administrateur ne doit pas dépasser 100 caractères")
        String prenomAdmin,

        @NotBlank(message = "L'email de l'administrateur est obligatoire")
        @Email(message = "L'email de l'administrateur doit être valide")
        @Size(max = 150, message = "L'email de l'administrateur ne doit pas dépasser 150 caractères")
        String emailAdmin,

        @NotBlank(message = "Le mot de passe est obligatoire")
        @Size(min = 8, max = 100, message = "Le mot de passe doit contenir entre 8 et 100 caractères")
        String motDePasse,

        @Past(message = "La date de naissance doit être dans le passé")
        LocalDate dateDeNaissance,

        @Size(max = 150, message = "La rue ne doit pas dépasser 150 caractères")
        String rueAdmin,

        @Size(max = 100, message = "La ville ne doit pas dépasser 100 caractères")
        String villeAdmin,

        @Size(max = 20, message = "Le code postal ne doit pas dépasser 20 caractères")
        String codePostalAdmin,

        @Size(max = 100, message = "Le pays ne doit pas dépasser 100 caractères")
        String paysAdmin
) {
}
