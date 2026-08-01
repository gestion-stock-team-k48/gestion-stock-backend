package cm.kfokam.stock.fournisseur;

import cm.kfokam.stock.fournisseur.dto.FournisseurRequest;
import cm.kfokam.stock.fournisseur.dto.FournisseurResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.multipart.MultipartFile;

public interface FournisseurService {

    FournisseurResponse create(FournisseurRequest request);

    FournisseurResponse getById(Long id);

    Page<FournisseurResponse> getAll(Pageable pageable);

    FournisseurResponse update(Long id, FournisseurRequest request);

    FournisseurResponse uploadPhoto(Long id, MultipartFile file);

    void delete(Long id);
}