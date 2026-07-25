package cm.kfokam.stock.client.dto;

public record ClientResponse(
        Long id,
        String nom,
        String prenom,
        String email,
        String numTel,
        String rue,
        String ville,
        String codePostal,
        String pays,
        String photo
) {
}