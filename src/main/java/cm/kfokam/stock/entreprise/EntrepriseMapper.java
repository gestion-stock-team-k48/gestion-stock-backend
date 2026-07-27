package cm.kfokam.stock.entreprise;

import cm.kfokam.stock.entreprise.dto.EntrepriseRequest;
import cm.kfokam.stock.entreprise.dto.EntrepriseResponse;
import cm.kfokam.stock.entreprise.model.Entreprise;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

import java.util.List;

@Mapper(componentModel = "spring")
interface EntrepriseMapper {

    @Mapping(target = "adresse.rue", source = "rue")
    @Mapping(target = "adresse.ville", source = "ville")
    @Mapping(target = "adresse.codePostal", source = "codePostal")
    @Mapping(target = "adresse.pays", source = "pays")
    Entreprise toEntity(EntrepriseRequest request);

    @Mapping(target = "rue", source = "adresse.rue")
    @Mapping(target = "ville", source = "adresse.ville")
    @Mapping(target = "codePostal", source = "adresse.codePostal")
    @Mapping(target = "pays", source = "adresse.pays")
    EntrepriseResponse toResponse(Entreprise entreprise);

    List<EntrepriseResponse> toResponseList(List<Entreprise> entreprises);

    @Mapping(target = "adresse.rue", source = "rue")
    @Mapping(target = "adresse.ville", source = "ville")
    @Mapping(target = "adresse.codePostal", source = "codePostal")
    @Mapping(target = "adresse.pays", source = "pays")
    void updateEntityFromRequest(EntrepriseRequest request, @MappingTarget Entreprise entreprise);
}