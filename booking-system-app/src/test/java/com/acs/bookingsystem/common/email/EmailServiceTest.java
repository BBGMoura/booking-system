package com.acs.bookingsystem.common.email;

import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Map;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.simplejavamail.api.mailer.Mailer;

@ExtendWith(MockitoExtension.class)
class EmailServiceTest {

  @Mock private EmailProperties emailProperties;
  @Mock private Mailer mailer;

  @InjectMocks private EmailService emailService;

  @Test
  void sendPasswordResetEmail_replacesLinkPlaceholderAndSendsEmail() {
    EmailProperties.Sender sender = new EmailProperties.Sender();
    sender.setAddress("noreply@example.com");

    EmailProperties.Template template = new EmailProperties.Template();
    template.setSubject("Reset your password");
    template.setBody("Click here: {link}");

    when(emailProperties.getSender()).thenReturn(sender);
    when(emailProperties.getTemplate())
        .thenReturn(Map.of(TemplateType.RESET_PASSWORD.getKey(), template));

    emailService.sendPasswordResetEmail("user@example.com", "https://example.com/reset?token=abc");

    verify(mailer)
        .sendMail(
            argThat(
                email ->
                    email.getPlainText().contains("https://example.com/reset?token=abc")
                        && !email.getPlainText().contains("{link}")));
  }

  @Test
  void sendInvitationEmail_sendsEmailWithCorrectSubjectAndBody() {
    EmailProperties.Sender sender = new EmailProperties.Sender();
    sender.setAddress("noreply@example.com");

    EmailProperties.Template template = new EmailProperties.Template();
    template.setSubject("You're invited!");
    template.setBody("Welcome to the system.");

    when(emailProperties.getSender()).thenReturn(sender);
    when(emailProperties.getTemplate())
        .thenReturn(Map.of(TemplateType.INVITATION.getKey(), template));

    emailService.sendInvitationEmail("user@example.com");

    verify(mailer)
        .sendMail(
            argThat(
                email ->
                    email.getSubject().equals("You're invited!")
                        && email.getPlainText().equals("Welcome to the system.")));
  }
}
