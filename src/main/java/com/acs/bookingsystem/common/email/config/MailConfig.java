package com.acs.bookingsystem.common.email.config;

import com.acs.bookingsystem.common.email.EmailProperties;
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
        return MailerBuilder
                .withSMTPServer(
                        emailProperties.getSmtp().getServer(),
                        emailProperties.getSmtp().getPort(),
                        emailProperties.getSender().getAddress(),
                        emailProperties.getSender().getPassword())
                .withTransportStrategy(TransportStrategy.SMTPS)
                .buildMailer();
    }
}