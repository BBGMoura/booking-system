package com.acs.bookingsystem.common.email;

import java.util.Map;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Getter
@Setter
@Component
@ConfigurationProperties(prefix = "email")
public class EmailProperties {

  private Sender sender;
  private Smtp smtp;

  /** Optional Reply-To address added to every email; blank means no Reply-To header. */
  private String replyTo;

  private Map<String, Template> template;

  @Getter
  @Setter
  public static class Sender {
    private String address;
    private String password;
  }

  @Getter
  @Setter
  public static class Smtp {
    private String server;
    private int port;

    /**
     * SMTP login name. Blank falls back to the sender address, which is what Gmail expects;
     * providers such as Resend use a fixed username that differs from the From address.
     */
    private String username;

    /**
     * Name of a {@link org.simplejavamail.api.mailer.config.TransportStrategy} constant (e.g. SMTP,
     * SMTP_TLS, SMTPS). Defaults to SMTPS (implicit TLS, as used by Gmail and Resend on port 465);
     * local dev overrides this to plain SMTP for Mailpit.
     */
    private String transportStrategy = "SMTPS";
  }

  @Getter
  @Setter
  public static class Template {
    private String subject;
    private String body;
  }
}
