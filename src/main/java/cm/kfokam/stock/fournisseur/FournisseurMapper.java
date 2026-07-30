package cm.kfokam.stock.fournisseur;

import cm.kfokam.stock.fournisseur.dto.FournisseurRequest;
import cm.kfokam.stock.fournisseur.dto.FournisseurResponse;
import cm.kfokam.stock.fournisseur.model.Fournisseur;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

import java.util.List;

@Mapper(componentModel = "spring")
interface FournisseurMapper {

    @Mapping(target = "adresse.adresse1", source = "rue")
    @Mapping(target = "adresse.ville", source = "ville")
    @Mapping(target = "adresse.codePostal", source = "codePostal")
    @Mapping(target = "adresse.pays", source = "pays")
    @Mapping(target = "entreprise", ignore = true)
    Fournisseur toEntity(FournisseurRequest request);

    @Mapping(target = "rue", source = "adresse.adresse1")
    @Mapping(target = "ville", source = "adresse.ville")
    @Mapping(target = "codePostal", source = "adresse.codePostal")
    @Mapping(target = "pays", source = "adresse.pays")
    FournisseurResponse toResponse(Fournisseur fournisseur);

    List<FournisseurResponse> toResponseList(List<Fournisseur> fournisseurs);

    @Mapping(target = "adresse.adresse1", source = "rue")
    @Mapping(target = "adresse.ville", source = "ville")
    @Mapping(target = "adresse.codePostal", source = "codePostal")
    @Mapping(target = "adresse.pays", source = "pays")
    void updateEntityFromRequest(FournisseurRequest request, @MappingTarget Fournisseur fournisseur);
}