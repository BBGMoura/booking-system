package com.acs.bookingsystem.common.email;

import lombok.AllArgsConstructor;
import org.simplejavamail.api.mailer.Mailer;
import org.simplejavamail.api.mailer.config.TransportStrategy;
import org.simplejavamail.mailer.MailerBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@AllArgsConstructor
public class MailConfig {

  private final EmailProperties emailProperties;

  @Bean
  public Mailer mailer() {
    String configuredUsername = emailProperties.getSmtp().getUsername();
    String username =
        configuredUsername == null || configuredUsername.isBlank()
            ? emailProperties.getSender().getAddress()
            : configuredUsername;
    return MailerBuilder.withSMTPServer(
            emailProperties.getSmtp().getServer(),
            emailProperties.getSmtp().getPort(),
            username,
            emailProperties.getSender().getPassword())
        .withTransportStrategy(
            TransportStrategy.valueOf(emailProperties.getSmtp().getTransportStrategy()))
        .buildMailer();
  }
}
