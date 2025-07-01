package com.acs.bookingsystem.common.email;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.simplejavamail.api.email.Email;
import org.simplejavamail.api.mailer.Mailer;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.HashMap;
import java.util.Map;

class EmailUtilTest {

    private EmailProperties emailProperties;
    private Mailer mailerMock;
    private EmailUtil emailUtil;

    @BeforeEach
    void setUp() {
        // Setup EmailProperties with dummy data
        emailProperties = new EmailProperties();

        EmailProperties.Sender sender = new EmailProperties.Sender();
        sender.setAddress("sender@test.com");
        sender.setPassword("secret");
        emailProperties.setSender(sender);

        EmailProperties.Smtp smtp = new EmailProperties.Smtp();
        smtp.setServer("smtp.test.com");
        smtp.setPort(587);
        emailProperties.setSmtp(smtp);

        Map<String, EmailProperties.Template> templates = new HashMap<>();

        EmailProperties.Template resetTemplate = new EmailProperties.Template();
        resetTemplate.setSubject("Reset Password");
        resetTemplate.setBody("Your new password is: {password}");
        templates.put("reset-password", resetTemplate);

        EmailProperties.Template invitationTemplate = new EmailProperties.Template();
        invitationTemplate.setSubject("Invitation");
        invitationTemplate.setBody("Welcome! Please join us.");
        templates.put("invitation", invitationTemplate);

        emailProperties.setTemplate(templates);

        // Create a Mailer mock
        mailerMock = mock(Mailer.class);

        // Create EmailUtil with mocked Mailer (override the mailer instance)
        emailUtil = new EmailUtil(emailProperties) {
            // Override the mailer instance with the mock
            @Override
            protected Mailer getMailer() {
                return mailerMock;
            }
        };
    }

    @Test
    void testSendPasswordResetEmail_ReplacesPasswordInBody() {
        String recipient = "user@test.com";
        String newPassword = "abc123";

        emailUtil.sendPasswordResetEmail(recipient, newPassword);

        ArgumentCaptor<Email> emailCaptor = ArgumentCaptor.forClass(Email.class);
        verify(mailerMock).sendMail(emailCaptor.capture());

        Email sentEmail = emailCaptor.getValue();

        assertEquals(recipient, sentEmail.getRecipients().get(0).getAddress());
        assertEquals("Reset Password", sentEmail.getSubject());
        assertTrue(sentEmail.getPlainText().contains(newPassword));
    }

    @Test
    void testSendInvitationEmail_UsesInvitationTemplate() {
        String recipient = "user@test.com";

        emailUtil.sendInvitationEmail(recipient);

        ArgumentCaptor<Email> emailCaptor = ArgumentCaptor.forClass(Email.class);
        verify(mailerMock).sendMail(emailCaptor.capture());

        Email sentEmail = emailCaptor.getValue();

        assertEquals(recipient, sentEmail.getRecipients().get(0).getAddress());
        assertEquals("Invitation", sentEmail.getSubject());
        assertEquals("Welcome! Please join us.", sentEmail.getPlainText());
    }

}
