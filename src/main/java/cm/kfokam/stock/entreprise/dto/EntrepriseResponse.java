package cm.kfokam.stock.entreprise.dto;

public record EntrepriseResponse(
        Long id,
        String nom,
        String description,
        String rue,
        String ville,
        String codePostal,
        String pays,
        String codeFiscal,
        String photo,
        String email,
        String numTel,
        String siteWeb
) {
}
