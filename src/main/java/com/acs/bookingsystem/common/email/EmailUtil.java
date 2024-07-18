package com.acs.bookingsystem.common.email;

import lombok.RequiredArgsConstructor;
import org.simplejavamail.api.email.Email;
import org.simplejavamail.api.mailer.Mailer;
import org.simplejavamail.api.mailer.config.TransportStrategy;
import org.simplejavamail.email.EmailBuilder;
import org.simplejavamail.mailer.MailerBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class EmailUtil {
    private final EmailConfig emailConfig;
    private static final Logger LOG = LoggerFactory.getLogger(EmailUtil.class);
    private static final String RESET_PASSWORD = "Reset Password";
    @Value("${email.invitation.subject}")
    private String emailInvitationSubject;
    @Value("${email.invitation.body}")
    private String emailInvitationBody;


    public void sendPasswordResetEmail(String recipientEmail, String password) {
        sendEmail(recipientEmail, RESET_PASSWORD, password);
    }

    public void sendInvitationEmail(String recipientEmail) {
        sendEmail(recipientEmail, emailInvitationSubject, emailInvitationBody);
    }

    public void sendEmail(String recipientEmail, String subject, String content) {
        try (Mailer mailer = MailerBuilder.withSMTPServer(emailConfig.getSmtpServer(),
                                                          emailConfig.getPort(),
                                                          emailConfig.getSenderEmail(),
                                                          emailConfig.getPassword())
                .withTransportStrategy(TransportStrategy.SMTPS)
                                          .buildMailer()) {
            Email email = EmailBuilder.startingBlank()
                                      .from(emailConfig.getSenderEmail())
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
