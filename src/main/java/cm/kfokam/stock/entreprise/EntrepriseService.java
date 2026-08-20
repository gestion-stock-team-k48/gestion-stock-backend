package cm.kfokam.stock.entreprise;

import cm.kfokam.stock.entreprise.dto.EntrepriseRequest;
import cm.kfokam.stock.entreprise.dto.EntrepriseResponse;

public interface EntrepriseService {

    EntrepriseResponse create(EntrepriseRequest request);

    EntrepriseResponse getById(Long id);

    EntrepriseResponse update(Long id, EntrepriseRequest request);
}
