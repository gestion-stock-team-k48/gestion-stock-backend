package cm.kfokam.stock.entreprise.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record EntrepriseRequest(

        @NotBlank(message = "Le nom est obligatoire")
        @Size(max = 150, message = "Le nom ne doit pas dépasser 150 caractères")
        String nom,

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

        String photo,

        @NotBlank(message = "L'email est obligatoire")
        @Email(message = "L'email doit être valide")
        @Size(max = 150, message = "L'email ne doit pas dépasser 150 caractères")
        String email,

        @Size(max = 20, message = "Le numéro de téléphone ne doit pas dépasser 20 caractères")
        String numTel,

        @Size(max = 150, message = "Le site web ne doit pas dépasser 150 caractères")
        String siteWeb
) {
}
