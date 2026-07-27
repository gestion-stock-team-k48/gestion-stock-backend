package cm.kfokam.stock.entreprise;

import cm.kfokam.stock.entreprise.dto.EntrepriseRequest;
import cm.kfokam.stock.entreprise.dto.EntrepriseResponse;

import java.util.List;

public interface EntrepriseService {

    EntrepriseResponse create(EntrepriseRequest request);

    EntrepriseResponse getById(Long id);

    List<EntrepriseResponse> getAll();

    EntrepriseResponse update(Long id, EntrepriseRequest request);

    void delete(Long id);
}
