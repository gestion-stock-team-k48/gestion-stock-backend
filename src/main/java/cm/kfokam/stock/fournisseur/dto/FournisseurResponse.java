package cm.kfokam.stock.fournisseur.dto;

public record FournisseurResponse(
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