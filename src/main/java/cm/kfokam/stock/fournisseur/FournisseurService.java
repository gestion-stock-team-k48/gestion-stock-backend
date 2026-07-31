package cm.kfokam.stock.fournisseur;

import cm.kfokam.stock.fournisseur.dto.FournisseurRequest;
import cm.kfokam.stock.fournisseur.dto.FournisseurResponse;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface FournisseurService {

    FournisseurResponse create(FournisseurRequest request);

    FournisseurResponse getById(Long id);

    List<FournisseurResponse> getAll();

    FournisseurResponse update(Long id, FournisseurRequest request);

    FournisseurResponse uploadPhoto(Long id, MultipartFile file);

    void delete(Long id);
}