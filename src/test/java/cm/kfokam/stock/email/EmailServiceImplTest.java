package cm.kfokam.stock.email;

import cm.kfokam.stock.commandeclient.dto.CommandeClientResponse;
import cm.kfokam.stock.commandefournisseur.dto.CommandeFournisseurResponse;
import jakarta.mail.Session;
import jakarta.mail.internet.MimeMessage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mail.MailSendException;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.test.util.ReflectionTestUtils;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Properties;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class EmailServiceImplTest {

    @Mock
    private JavaMailSender mailSender;

    @Mock
    private TemplateEngine templateEngine;

    private EmailServiceImpl emailService;

    @BeforeEach
    void setUp() {
        emailService = new EmailServiceImpl(mailSender, templateEngine);
        ReflectionTestUtils.setField(emailService, "fromAddress", "notif@gestion-stock.cm");
        ReflectionTestUtils.setField(emailService, "fromName", "Gestion Stock");
    }

    private MimeMessage newMimeMessage() {
        return new MimeMessage(Session.getDefaultInstance(new Properties()));
    }

    private CommandeClientResponse commandeClient() {
        return new CommandeClientResponse(1L, "CC-2026-0001", LocalDate.now(),
                cm.kfokam.stock.commandeclient.model.EtatCommande.EN_PREPARATION,
                1L, "Doe", "John", BigDecimal.TEN, BigDecimal.ONE, BigDecimal.TEN, List.of());
    }

    private CommandeFournisseurResponse commandeFournisseur() {
        return new CommandeFournisseurResponse(1L, "CF-2026-0001", LocalDate.now(),
                cm.kfokam.stock.commandefournisseur.model.EtatCommande.EN_PREPARATION,
                1L, "Martin", "Paul", BigDecimal.TEN, BigDecimal.ONE, BigDecimal.TEN, List.of());
    }

    @Test
    void envoyerConfirmationCommandeClient_shouldRenderTemplateAndSend() {
        when(mailSender.createMimeMessage()).thenReturn(newMimeMessage());
        when(templateEngine.process(eq("email/commande-client-confirmation"), any(Context.class)))
                .thenReturn("<html>confirmation</html>");

        emailService.envoyerConfirmationCommandeClient("john@doe.com", commandeClient());

        verify(mailSender).send(any(MimeMessage.class));
    }

    @Test
    void envoyerOrdreCommandeFournisseur_shouldRenderTemplateAndSend() {
        when(mailSender.createMimeMessage()).thenReturn(newMimeMessage());
        when(templateEngine.process(eq("email/commande-fournisseur-ordre"), any(Context.class)))
                .thenReturn("<html>ordre</html>");

        emailService.envoyerOrdreCommandeFournisseur("paul@martin.com", commandeFournisseur());

        verify(mailSender).send(any(MimeMessage.class));
    }

    @Test
    void envoyerConfirmationCommandeClient_shouldNotThrow_whenSmtpUnreachable() {
        when(mailSender.createMimeMessage()).thenReturn(newMimeMessage());
        when(templateEngine.process(eq("email/commande-client-confirmation"), any(Context.class)))
                .thenReturn("<html>confirmation</html>");
        doThrow(new MailSendException("SMTP indisponible")).when(mailSender).send(any(MimeMessage.class));

        assertThatCode(() -> emailService.envoyerConfirmationCommandeClient("john@doe.com", commandeClient()))
                .doesNotThrowAnyException();
    }

    @Test
    void envoyerOrdreCommandeFournisseur_shouldNotThrow_whenSmtpUnreachable() {
        when(mailSender.createMimeMessage()).thenReturn(newMimeMessage());
        when(templateEngine.process(eq("email/commande-fournisseur-ordre"), any(Context.class)))
                .thenReturn("<html>ordre</html>");
        doThrow(new MailSendException("SMTP indisponible")).when(mailSender).send(any(MimeMessage.class));

        assertThatCode(() -> emailService.envoyerOrdreCommandeFournisseur("paul@martin.com", commandeFournisseur()))
                .doesNotThrowAnyException();
    }

    @Test
    void envoyerResetMotDePasse_shouldRenderTemplateAndSend() {
        when(mailSender.createMimeMessage()).thenReturn(newMimeMessage());
        when(templateEngine.process(eq("email/reset-password"), any(Context.class)))
                .thenReturn("<html>reset</html>");

        emailService.envoyerResetMotDePasse("john@doe.com", "some-token", 30L);

        verify(mailSender).send(any(MimeMessage.class));
    }

    @Test
    void envoyerResetMotDePasse_shouldNotThrow_whenSmtpUnreachable() {
        when(mailSender.createMimeMessage()).thenReturn(newMimeMessage());
        when(templateEngine.process(eq("email/reset-password"), any(Context.class)))
                .thenReturn("<html>reset</html>");
        doThrow(new MailSendException("SMTP indisponible")).when(mailSender).send(any(MimeMessage.class));

        assertThatCode(() -> emailService.envoyerResetMotDePasse("john@doe.com", "some-token", 30L))
                .doesNotThrowAnyException();
    }

    @Test
    void envoyerConfirmationCommandeClient_shouldNotSend_whenTemplateRenderingFails() {
        when(templateEngine.process(eq("email/commande-client-confirmation"), any(Context.class)))
                .thenThrow(new RuntimeException("Template invalide"));

        assertThatCode(() -> emailService.envoyerConfirmationCommandeClient("john@doe.com", commandeClient()))
                .doesNotThrowAnyException();

        verify(mailSender, never()).send(any(MimeMessage.class));
    }
}
