package cm.kfokam.stock.vente;

import cm.kfokam.stock.vente.dto.VenteRequest;
import cm.kfokam.stock.vente.dto.VenteResponse;
import cm.kfokam.stock.vente.dto.ligneVente.LigneVenteResponse;
import cm.kfokam.stock.vente.model.LigneVente;
import cm.kfokam.stock.vente.model.Vente;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring")
interface VenteMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "code", ignore = true)
    @Mapping(target = "dateVente", ignore = true)
    @Mapping(target = "lignes", ignore = true)
    Vente toEntity(VenteRequest request);

    VenteResponse toResponse(Vente vente);

    List<VenteResponse> toResponseList(List<Vente> ventes);

    @Mapping(target = "articleId", source = "article.id")
    @Mapping(target = "articleDesignation", source = "article.designation")
    LigneVenteResponse toLigneResponse(LigneVente ligneVente);

    List<LigneVenteResponse> toLigneResponseList(List<LigneVente> lignes);
}