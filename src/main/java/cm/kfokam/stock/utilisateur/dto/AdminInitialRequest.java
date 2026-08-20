package cm.kfokam.stock.utilisateur.dto;

import java.time.LocalDate;

/**
 * Le premier administrateur d'une entreprise, à sa création.
 *
 * Dix paramètres positionnels se lisaient encore à l'écriture et plus du tout à la relecture :
 * quatre chaînes d'adresse à la suite, dans un ordre que rien ne rappelait, et qu'un appelant
 * pouvait intervertir sans que le compilateur ne bronche — un utilisateur créé à Douala,
 * code postal « Cameroun ». Nommer les champs rend l'erreur impossible.
 *
 * Ce n'est pas un DTO d'API : il ne traverse aucun contrôleur, il ne sert qu'entre le service
 * d'authentification et celui des utilisateurs, à l'inscription.
 *
 * @param entrepriseId    entreprise que ce compte administre
 * @param rawPassword     mot de passe choisi par la personne, chiffré par le service
 * @param dateDeNaissance telle que saisie à l'inscription
 */
public record AdminInitialRequest(
        Long entrepriseId,
        String nom,
        String prenom,
        String email,
        String rawPassword,
        LocalDate dateDeNaissance,
        String rue,
        String ville,
        String codePostal,
        String pays
) {
}
