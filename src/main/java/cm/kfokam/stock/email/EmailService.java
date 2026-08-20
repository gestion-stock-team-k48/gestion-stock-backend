package cm.kfokam.stock.email;

import cm.kfokam.stock.commandeclient.dto.CommandeClientResponse;
import cm.kfokam.stock.commandefournisseur.dto.CommandeFournisseurResponse;

public interface EmailService {

    void envoyerConfirmationCommandeClient(String destinataire, CommandeClientResponse commande);

    void envoyerOrdreCommandeFournisseur(String destinataire, CommandeFournisseurResponse commande);

    void envoyerResetMotDePasse(String destinataire, String token, long expirationMinutes);

    void envoyerMotDePasseTemporaire(String destinataire, String prenom, String temporaryPassword);

    // Notification déclenchée sur passage à un état terminal (LIVREE ou ANNULEE) uniquement.
    void envoyerNotificationEtatCommandeClient(String destinataire, CommandeClientResponse commande);

    void envoyerNotificationEtatCommandeFournisseur(String destinataire, CommandeFournisseurResponse commande);
}
