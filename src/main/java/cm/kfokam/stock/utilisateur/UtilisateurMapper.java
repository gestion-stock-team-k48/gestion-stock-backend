package cm.kfokam.stock.utilisateur;

import cm.kfokam.stock.utilisateur.dto.UtilisateurRequest;
import cm.kfokam.stock.utilisateur.dto.UtilisateurResponse;
import cm.kfokam.stock.utilisateur.model.Utilisateur;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

import java.util.List;

@Mapper(componentModel = "spring")
interface UtilisateurMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "motDePasse", ignore = true)
    @Mapping(target = "mustChangePassword", ignore = true)
    @Mapping(target = "entreprise", ignore = true)
    @Mapping(target = "adresse.adresse1", source = "rue")
    @Mapping(target = "adresse.ville", source = "ville")
    @Mapping(target = "adresse.codePostal", source = "codePostal")
    @Mapping(target = "adresse.pays", source = "pays")
    Utilisateur toEntity(UtilisateurRequest request);

    @Mapping(target = "entrepriseId", source = "entreprise.id")
    @Mapping(target = "entrepriseNom", source = "entreprise.nom")
    @Mapping(target = "rue", source = "adresse.adresse1")
    @Mapping(target = "ville", source = "adresse.ville")
    @Mapping(target = "codePostal", source = "adresse.codePostal")
    @Mapping(target = "pays", source = "adresse.pays")
    UtilisateurResponse toResponse(Utilisateur utilisateur);

    List<UtilisateurResponse> toResponseList(List<Utilisateur> utilisateurs);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "motDePasse", ignore = true)
    @Mapping(target = "mustChangePassword", ignore = true)
    @Mapping(target = "entreprise", ignore = true)
    @Mapping(target = "adresse.adresse1", source = "rue")
    @Mapping(target = "adresse.ville", source = "ville")
    @Mapping(target = "adresse.codePostal", source = "codePostal")
    @Mapping(target = "adresse.pays", source = "pays")
    void updateEntityFromRequest(UtilisateurRequest request, @MappingTarget Utilisateur utilisateur);
}
