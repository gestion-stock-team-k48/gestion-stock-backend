package cm.kfokam.stock.client;

import cm.kfokam.stock.client.dto.ClientRequest;
import cm.kfokam.stock.client.dto.ClientResponse;
import cm.kfokam.stock.client.model.Client;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

import java.util.List;

@Mapper(componentModel = "spring")
interface ClientMapper {

    @Mapping(target = "adresse.rue", source = "rue")
    @Mapping(target = "adresse.ville", source = "ville")
    @Mapping(target = "adresse.codePostal", source = "codePostal")
    @Mapping(target = "adresse.pays", source = "pays")
    @Mapping(target = "entreprise", ignore = true)
    Client toEntity(ClientRequest request);

    @Mapping(target = "rue", source = "adresse.rue")
    @Mapping(target = "ville", source = "adresse.ville")
    @Mapping(target = "codePostal", source = "adresse.codePostal")
    @Mapping(target = "pays", source = "adresse.pays")
    ClientResponse toResponse(Client client);

    List<ClientResponse> toResponseList(List<Client> clients);

    @Mapping(target = "adresse.rue", source = "rue")
    @Mapping(target = "adresse.ville", source = "ville")
    @Mapping(target = "adresse.codePostal", source = "codePostal")
    @Mapping(target = "adresse.pays", source = "pays")
    void updateEntityFromRequest(ClientRequest request, @MappingTarget Client client);
}