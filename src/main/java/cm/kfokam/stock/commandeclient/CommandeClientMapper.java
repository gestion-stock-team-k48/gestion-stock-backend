package cm.kfokam.stock.commandeclient;

import cm.kfokam.stock.commandeclient.dto.CommandeClientRequest;
import cm.kfokam.stock.commandeclient.dto.CommandeClientResponse;
import cm.kfokam.stock.commandeclient.dto.LigneCommandeClientResponse;
import cm.kfokam.stock.commandeclient.model.CommandeClient;
import cm.kfokam.stock.commandeclient.model.LigneCommandeClient;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring")
interface CommandeClientMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "codeCommande", ignore = true)
    @Mapping(target = "etatCommande", ignore = true)
    @Mapping(target = "client", ignore = true)
    @Mapping(target = "totalHt", ignore = true)
    @Mapping(target = "totalTva", ignore = true)
    @Mapping(target = "totalTtc", ignore = true)
    @Mapping(target = "lignes", ignore = true)
    @Mapping(target = "entreprise", ignore = true)
    CommandeClient toEntity(CommandeClientRequest request);

    @Mapping(target = "idClient", source = "client.id")
    @Mapping(target = "clientNom", source = "client.nom")
    @Mapping(target = "clientPrenom", source = "client.prenom")
    CommandeClientResponse toResponse(CommandeClient commandeClient);

    List<CommandeClientResponse> toResponseList(List<CommandeClient> commandes);

    @Mapping(target = "articleId", source = "article.id")
    @Mapping(target = "articleDesignation", source = "article.designation")
    LigneCommandeClientResponse toLigneResponse(LigneCommandeClient ligne);

    List<LigneCommandeClientResponse> toLigneResponseList(List<LigneCommandeClient> lignes);
}