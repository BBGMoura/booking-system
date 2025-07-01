package com.acs.bookingsystem.common.email;

import lombok.AllArgsConstructor;
import org.simplejavamail.api.email.Email;
import org.simplejavamail.api.mailer.Mailer;
import org.simplejavamail.email.EmailBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import static com.acs.bookingsystem.common.email.TemplateType.INVITATION;
import static com.acs.bookingsystem.common.email.TemplateType.RESET_PASSWORD;

@Component
@AllArgsConstructor
public class EmailUtil {
    private final EmailProperties emailProperties;
    private final Mailer mailer;
    private static final Logger LOG = LoggerFactory.getLogger(EmailUtil.class);

    public void sendPasswordResetEmail(String recipientEmail, String newPassword) {
        EmailProperties.Template template = emailProperties.getTemplate().get(RESET_PASSWORD.getKey());
        String subject = template.getSubject();
        String body = template.getBody().replace("{password}", newPassword);
        sendEmail(recipientEmail, subject, body);
    }

    public void sendInvitationEmail(qString recipientEmail) {
        EmailProperties.Template template = emailProperties.getTemplate().get(INVITATION.getKey());
        sendEmail(recipientEmail, template.getSubject(), template.getBody());
    }

    private void sendEmail(String recipientEmail, String subject, String content) {
        try {
            Email email = EmailBuilder.startingBlank()
                                      .from(emailProperties.getSender().getAddress())
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
