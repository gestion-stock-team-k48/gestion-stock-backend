package cm.kfokam.stock.fournisseur.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record FournisseurRequest(

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

        @Size(max = 20, message = "Le numéro de téléphone ne doit pas dépasser 20 caractères")
        String numTel,

        @Size(max = 150, message = "La rue ne doit pas dépasser 150 caractères")
        String rue,

        @Size(max = 100, message = "La ville ne doit pas dépasser 100 caractères")
        String ville,

        @Size(max = 20, message = "Le code postal ne doit pas dépasser 20 caractères")
        String codePostal,

        @Size(max = 100, message = "Le pays ne doit pas dépasser 100 caractères")
        String pays,

        String photo
) {
}