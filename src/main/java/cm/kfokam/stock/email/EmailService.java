package cm.kfokam.stock.email;

import cm.kfokam.stock.commandeclient.dto.CommandeClientResponse;
import cm.kfokam.stock.commandefournisseur.dto.CommandeFournisseurResponse;

public interface EmailService {

    void envoyerConfirmationCommandeClient(String destinataire, CommandeClientResponse commande);

    void envoyerOrdreCommandeFournisseur(String destinataire, CommandeFournisseurResponse commande);
}
