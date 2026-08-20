package cm.kfokam.stock.commandeclient;

import cm.kfokam.stock.commandeclient.dto.CommandeClientRequest;
import cm.kfokam.stock.commandeclient.dto.CommandeClientResponse;
import cm.kfokam.stock.commandeclient.model.EtatCommande;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface CommandeClientService {

    CommandeClientResponse create(CommandeClientRequest request);

    CommandeClientResponse getById(Long id);

    Page<CommandeClientResponse> getAll(Pageable pageable);

    List<CommandeClientResponse> getHistoriqueByClient(Long idClient);

    CommandeClientResponse update(Long id, CommandeClientRequest request);

    void delete(Long id);

    CommandeClientResponse updateEtatCommande(Long id, EtatCommande nouvelEtat);
}