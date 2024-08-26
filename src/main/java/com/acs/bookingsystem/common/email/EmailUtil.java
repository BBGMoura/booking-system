package com.acs.bookingsystem.common.email;

import lombok.AllArgsConstructor;
import org.simplejavamail.api.email.Email;
import org.simplejavamail.api.mailer.Mailer;
import org.simplejavamail.api.mailer.config.TransportStrategy;
import org.simplejavamail.email.EmailBuilder;
import org.simplejavamail.mailer.MailerBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
@AllArgsConstructor
public class EmailUtil {
    private final EmailProperties emailProperties;
    private static final Logger LOG = LoggerFactory.getLogger(EmailUtil.class);

    public void sendPasswordResetEmail(String recipientEmail, String password) {
        sendEmail(recipientEmail, EmailProperties.RESET_PASSWORD, password);
    }

    public void sendInvitationEmail(String recipientEmail) {
        sendEmail(recipientEmail, emailProperties.getEmailInvitationSubject(), emailProperties.getEmailInvitationBody());
    }

    private void sendEmail(String recipientEmail, String subject, String content) {
        try (Mailer mailer = MailerBuilder.withSMTPServer(emailProperties.getSmtpServer(),
                                                          emailProperties.getPort(),
                                                          emailProperties.getSenderEmail(),
                                                          emailProperties.getPassword())
                                          .withTransportStrategy(TransportStrategy.SMTPS)
                                          .buildMailer()) {
            Email email = EmailBuilder.startingBlank()
                                      .from(emailProperties.getSenderEmail())
                                      .to(recipientEmail)
                                      .withSubject(subject)
                                      .withPlainText(content)
                                      .buildEmail();
            mailer.sendMail(email);

            LOG.info("Email sent successfully to: {}", email);
        } catch (Exception ex) {
            LOG.error("Failed to send email to {}: {}", recipientEmail, ex.getMessage(), ex);
        }
    }
}
