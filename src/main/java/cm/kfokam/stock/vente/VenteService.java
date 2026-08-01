package cm.kfokam.stock.vente;

import cm.kfokam.stock.vente.dto.VenteRequest;
import cm.kfokam.stock.vente.dto.VenteResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface VenteService {

    VenteResponse create(VenteRequest request);

    VenteResponse getById(Long id);

    VenteResponse getByCode(String code);

    Page<VenteResponse> getAll(Pageable pageable);

    void delete(Long id);
}