package cm.kfokam.stock.commandefournisseur;

import cm.kfokam.stock.commandefournisseur.dto.CommandeFournisseurRequest;
import cm.kfokam.stock.commandefournisseur.dto.CommandeFournisseurResponse;
import cm.kfokam.stock.commandefournisseur.dto.LigneCommandeFournisseurResponse;
import cm.kfokam.stock.commandefournisseur.model.CommandeFournisseur;
import cm.kfokam.stock.commandefournisseur.model.LigneCommandeFournisseur;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring")
interface CommandeFournisseurMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "codeCommande", ignore = true)
    @Mapping(target = "etatCommande", ignore = true)
    @Mapping(target = "fournisseur", ignore = true)
    @Mapping(target = "totalHt", ignore = true)
    @Mapping(target = "totalTva", ignore = true)
    @Mapping(target = "totalTtc", ignore = true)
    @Mapping(target = "lignes", ignore = true)
    @Mapping(target = "entreprise", ignore = true)
    CommandeFournisseur toEntity(CommandeFournisseurRequest request);

    @Mapping(target = "idFournisseur", source = "fournisseur.id")
    @Mapping(target = "fournisseurNom", source = "fournisseur.nom")
    @Mapping(target = "fournisseurPrenom", source = "fournisseur.prenom")
    CommandeFournisseurResponse toResponse(CommandeFournisseur commandeFournisseur);

    List<CommandeFournisseurResponse> toResponseList(List<CommandeFournisseur> commandes);

    @Mapping(target = "articleId", source = "article.id")
    @Mapping(target = "articleDesignation", source = "article.designation")
    LigneCommandeFournisseurResponse toLigneResponse(LigneCommandeFournisseur ligne);

    List<LigneCommandeFournisseurResponse> toLigneResponseList(List<LigneCommandeFournisseur> lignes);
}