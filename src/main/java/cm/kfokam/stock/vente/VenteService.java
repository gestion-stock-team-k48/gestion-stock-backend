package cm.kfokam.stock.vente;

import cm.kfokam.stock.vente.dto.VenteRequest;
import cm.kfokam.stock.vente.dto.VenteResponse;

import java.util.List;

public interface VenteService {

    VenteResponse create(VenteRequest request);

    VenteResponse getById(Long id);

    VenteResponse getByCode(String code);

    List<VenteResponse> getAll();

    void delete(Long id);
}