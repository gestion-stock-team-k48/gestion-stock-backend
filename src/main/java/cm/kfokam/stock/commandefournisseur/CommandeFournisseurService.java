package cm.kfokam.stock.commandefournisseur;

import cm.kfokam.stock.commandefournisseur.dto.CommandeFournisseurRequest;
import cm.kfokam.stock.commandefournisseur.dto.CommandeFournisseurResponse;
import cm.kfokam.stock.commandefournisseur.model.EtatCommande;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface CommandeFournisseurService {

    CommandeFournisseurResponse create(CommandeFournisseurRequest request);

    CommandeFournisseurResponse getById(Long id);

    Page<CommandeFournisseurResponse> getAll(Pageable pageable);

    List<CommandeFournisseurResponse> getHistoriqueByFournisseur(Long idFournisseur);

    CommandeFournisseurResponse update(Long id, CommandeFournisseurRequest request);

    void delete(Long id);

    CommandeFournisseurResponse updateEtatCommande(Long id, EtatCommande nouvelEtat);
}