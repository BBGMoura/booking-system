package com.acs.bookingsystem.common.email;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.simplejavamail.api.mailer.Mailer;

class MailConfigTest {

  @Test
  void mailer_usesSenderAddressAsUsernameWhenNoUsernameSet() {
    Mailer mailer = new MailConfig(properties(null)).mailer();

    assertThat(mailer.getServerConfig().getUsername()).isEqualTo("bookings@acsdance.uk");
    assertThat(mailer.getServerConfig().getPassword()).isEqualTo("secret");
  }

  @Test
  void mailer_usesSenderAddressAsUsernameWhenUsernameBlank() {
    Mailer mailer = new MailConfig(properties("  ")).mailer();

    assertThat(mailer.getServerConfig().getUsername()).isEqualTo("bookings@acsdance.uk");
  }

  @Test
  void mailer_usesExplicitUsernameWhenSet() {
    Mailer mailer = new MailConfig(properties("resend")).mailer();

    assertThat(mailer.getServerConfig().getUsername()).isEqualTo("resend");
    assertThat(mailer.getServerConfig().getHost()).isEqualTo("smtp.resend.com");
    assertThat(mailer.getServerConfig().getPort()).isEqualTo(465);
  }

  private static EmailProperties properties(String username) {
    EmailProperties.Sender sender = new EmailProperties.Sender();
    sender.setAddress("bookings@acsdance.uk");
    sender.setPassword("secret");

    EmailProperties.Smtp smtp = new EmailProperties.Smtp();
    smtp.setServer("smtp.resend.com");
    smtp.setPort(465);
    smtp.setUsername(username);

    EmailProperties properties = new EmailProperties();
    properties.setSender(sender);
    properties.setSmtp(smtp);
    return properties;
  }
}
