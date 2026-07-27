package cm.kfokam.stock.vente.dto;

import cm.kfokam.stock.vente.dto.ligneVente.LigneVenteResponse;

import java.time.Instant;
import java.util.List;

public record VenteResponse(
        Long id,
        String code,
        Instant dateVente,
        String commentaire,
        Long idEntreprise,
        List<LigneVenteResponse> lignes
) {
}