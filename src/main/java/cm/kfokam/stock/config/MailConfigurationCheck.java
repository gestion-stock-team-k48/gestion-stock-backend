package cm.kfokam.stock.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.event.EventListener;
import org.springframework.util.StringUtils;

// Sans ce contrôle, un MAIL_USERNAME/MAIL_PASSWORD manquant ne se découvre qu'au premier envoi
// tenté en production (ex: un utilisateur qui ne reçoit jamais son jeton de réinitialisation),
// et ce silencieusement puisque les emails non critiques avalent déjà leurs erreurs (voir
// EmailServiceImpl.envoyer). Ce check rend le problème visible dès le démarrage de l'application.
@Slf4j
@Configuration
public class MailConfigurationCheck {

    @Value("${spring.mail.username:}")
    private String mailUsername;

    @Value("${spring.mail.password:}")
    private String mailPassword;

    @EventListener(ApplicationReadyEvent.class)
    public void verifierIdentifiantsMail() {
        if (!StringUtils.hasText(mailUsername) || !StringUtils.hasText(mailPassword)) {
            log.warn("Configuration SMTP incomplète (MAIL_USERNAME/MAIL_PASSWORD absents) : "
                    + "aucun email ne pourra être envoyé tant que ces variables ne sont pas renseignées.");
        }
    }
}
