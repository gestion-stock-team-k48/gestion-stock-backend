package cm.kfokam.stock.email;

import cm.kfokam.stock.commandeclient.dto.CommandeClientResponse;
import cm.kfokam.stock.commandefournisseur.dto.CommandeFournisseurResponse;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.nio.charset.StandardCharsets;

@Slf4j
@Service
@RequiredArgsConstructor
class EmailServiceImpl implements EmailService {

    private static final String TEMPLATE_CONFIRMATION_CLIENT = "email/commande-client-confirmation";
    private static final String TEMPLATE_ORDRE_FOURNISSEUR = "email/commande-fournisseur-ordre";
    private static final String TEMPLATE_RESET_PASSWORD = "email/reset-password";
    private static final String TEMPLATE_NOUVEL_UTILISATEUR = "email/nouvel-utilisateur";
    private static final String TEMPLATE_ETAT_COMMANDE_CLIENT = "email/commande-client-etat";
    private static final String TEMPLATE_ETAT_COMMANDE_FOURNISSEUR = "email/commande-fournisseur-etat";

    private final JavaMailSender mailSender;
    private final TemplateEngine templateEngine;

    @Value("${application.mail.from}")
    private String fromAddress;

    @Value("${application.mail.from-name}")
    private String fromName;

    @Override
    public void envoyerConfirmationCommandeClient(String destinataire, CommandeClientResponse commande) {
        Context context = new Context();
        context.setVariable("clientNom", commande.clientNom());
        context.setVariable("clientPrenom", commande.clientPrenom());
        context.setVariable("codeCommande", commande.codeCommande());
        context.setVariable("dateCommande", commande.dateCommande());
        context.setVariable("lignes", commande.lignes());
        context.setVariable("totalTtc", commande.totalTtc());

        envoyer(destinataire, "Confirmation de votre commande %s".formatted(commande.codeCommande()),
                TEMPLATE_CONFIRMATION_CLIENT, context);
    }

    @Override
    public void envoyerOrdreCommandeFournisseur(String destinataire, CommandeFournisseurResponse commande) {
        Context context = new Context();
        context.setVariable("fournisseurNom", commande.fournisseurNom());
        context.setVariable("fournisseurPrenom", commande.fournisseurPrenom());
        context.setVariable("codeCommande", commande.codeCommande());
        context.setVariable("dateCommande", commande.dateCommande());
        context.setVariable("lignes", commande.lignes());
        context.setVariable("totalTtc", commande.totalTtc());

        envoyer(destinataire, "Nouvel ordre de commande %s".formatted(commande.codeCommande()),
                TEMPLATE_ORDRE_FOURNISSEUR, context);
    }

    @Override
    public void envoyerResetMotDePasse(String destinataire, String token, long expirationMinutes) {
        Context context = new Context();
        context.setVariable("token", token);
        context.setVariable("expirationMinutes", expirationMinutes);

        envoyer(destinataire, "Réinitialisation de votre mot de passe", TEMPLATE_RESET_PASSWORD, context);
    }

    @Override
    public void envoyerMotDePasseTemporaire(String destinataire, String prenom, String temporaryPassword) {
        Context context = new Context();
        context.setVariable("prenom", prenom);
        context.setVariable("email", destinataire);
        context.setVariable("temporaryPassword", temporaryPassword);

        envoyer(destinataire, "Votre compte Gestion Stock a été créé", TEMPLATE_NOUVEL_UTILISATEUR, context);
    }

    @Override
    public void envoyerNotificationEtatCommandeClient(String destinataire, CommandeClientResponse commande) {
        Context context = new Context();
        context.setVariable("clientNom", commande.clientNom());
        context.setVariable("clientPrenom", commande.clientPrenom());
        context.setVariable("codeCommande", commande.codeCommande());
        context.setVariable("etatCommande", commande.etatCommande().name());

        envoyer(destinataire, "Mise à jour de votre commande %s".formatted(commande.codeCommande()),
                TEMPLATE_ETAT_COMMANDE_CLIENT, context);
    }

    @Override
    public void envoyerNotificationEtatCommandeFournisseur(String destinataire, CommandeFournisseurResponse commande) {
        Context context = new Context();
        context.setVariable("fournisseurNom", commande.fournisseurNom());
        context.setVariable("fournisseurPrenom", commande.fournisseurPrenom());
        context.setVariable("codeCommande", commande.codeCommande());
        context.setVariable("etatCommande", commande.etatCommande().name());

        envoyer(destinataire, "Mise à jour de votre commande %s".formatted(commande.codeCommande()),
                TEMPLATE_ETAT_COMMANDE_FOURNISSEUR, context);
    }

    // Never lets a mail failure (SMTP down, bad address, broken template, ...) propagate — a commande
    // must not fail to save just because the confirmation email couldn't be built or sent. Catching
    // Exception is deliberate here: this is the single fire-and-forget boundary for every failure mode
    // (checked MessagingException from the helper, unchecked MailException from the send, unchecked
    // Thymeleaf exceptions from template rendering).
    private void envoyer(String destinataire, String sujet, String template, Context context) {
        try {
            String contenu = templateEngine.process(template, context);

            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, StandardCharsets.UTF_8.name());
            helper.setFrom(fromAddress, fromName);
            helper.setTo(destinataire);
            helper.setSubject(sujet);
            helper.setText(contenu, true);

            mailSender.send(message);
        } catch (Exception e) {
            log.warn("Échec de l'envoi de l'email '{}' à {} : {}", sujet, destinataire, e.getMessage());
        }
    }
}
