package com.acs.bookingsystem.config;

import static org.assertj.core.api.Assertions.assertThat;

import com.acs.bookingsystem.common.email.EmailProperties;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;
import org.junit.jupiter.api.Test;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.ConfigDataApplicationContextInitializer;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.context.annotation.Configuration;

class ProfileConfigTest {

  private static final Path RESOURCES = Path.of("src/main/resources");
  private static final Pattern PLACEHOLDER = Pattern.compile("\\$\\{([A-Z][A-Z0-9_]*)[:}]");

  private static final String[] REQUIRED_EVERYWHERE = {
    "email.sender.address=a@b.c", "email.sender.password=x"
  };

  @Configuration
  @EnableConfigurationProperties(EmailProperties.class)
  static class EmailOnly {}

  private ApplicationContextRunner runner(String profile) {
    return new ApplicationContextRunner()
        .withInitializer(new ConfigDataApplicationContextInitializer())
        .withUserConfiguration(EmailOnly.class)
        .withPropertyValues("spring.profiles.active=" + profile);
  }

  @Test
  void devWithoutSmtpVariables_failsToStart() {
    runner("dev")
        .withPropertyValues(REQUIRED_EVERYWHERE)
        .run(context -> assertThat(context).hasFailed());
  }

  @Test
  void prodWithoutSmtpVariables_failsToStart() {
    runner("prod")
        .withPropertyValues(REQUIRED_EVERYWHERE)
        .run(context -> assertThat(context).hasFailed());
  }

  @Test
  void devWithSmtpVariables_starts() {
    runner("dev")
        .withPropertyValues(REQUIRED_EVERYWHERE)
        .withPropertyValues(
            "EMAIL_SMTP_SERVER=smtp.resend.com",
            "EMAIL_SMTP_PORT=465",
            "EMAIL_SMTP_TRANSPORT_STRATEGY=SMTPS")
        .run(
            context -> {
              assertThat(context).hasNotFailed();
              assertThat(context.getBean(EmailProperties.class).getSmtp().getServer())
                  .isEqualTo("smtp.resend.com");
            });
  }

  @Test
  void local_usesMailpitWithoutAnyEmailVariables() {
    runner("local")
        .run(
            context -> {
              EmailProperties.Smtp smtp = context.getBean(EmailProperties.class).getSmtp();
              assertThat(smtp.getServer()).isEqualTo("localhost");
              assertThat(smtp.getPort()).isEqualTo(1025);
              assertThat(smtp.getTransportStrategy()).isEqualTo("SMTP");
            });
  }

  @Test
  void prod_hidesErrorDetailsAndExposesOnlyHealth() {
    runner("prod")
        .withPropertyValues(REQUIRED_EVERYWHERE)
        .withPropertyValues(
            "EMAIL_SMTP_SERVER=s", "EMAIL_SMTP_PORT=465", "EMAIL_SMTP_TRANSPORT_STRATEGY=SMTPS")
        .run(
            context -> {
              var env = context.getEnvironment();
              assertThat(env.getProperty("server.error.include-stacktrace")).isEqualTo("never");
              assertThat(env.getProperty("management.endpoints.web.exposure.include"))
                  .isEqualTo("health");
              assertThat(env.getProperty("spring.jpa.show-sql")).isEqualTo("false");
            });
  }

  @Test
  void everyVariableInTheYamlFilesIsListedInConfigMd() throws IOException {
    String configMd = Files.readString(Path.of("../CONFIG.md"));
    Set<String> missing = new TreeSet<>();
    try (Stream<Path> files = Files.list(RESOURCES)) {
      for (Path file : files.filter(f -> f.toString().endsWith(".yaml")).toList()) {
        Matcher m = PLACEHOLDER.matcher(Files.readString(file));
        while (m.find()) {
          if (!configMd.contains("`" + m.group(1) + "`")) {
            missing.add(m.group(1));
          }
        }
      }
    }
    assertThat(missing)
        .as("variables used in application*.yaml but missing from CONFIG.md")
        .isEmpty();
  }

  @Test
  void noProfileFileContainsAHardCodedSecret() throws IOException {
    List<String> secretKeys = Arrays.asList("password:", "secret:", "key:", "token:");
    try (Stream<Path> files = Files.list(RESOURCES)) {
      for (Path file : files.filter(f -> f.toString().endsWith(".yaml")).toList()) {
        for (String line : Files.readAllLines(file)) {
          String t = line.strip();
          boolean secretKey = secretKeys.stream().anyMatch(t::startsWith);
          if (secretKey && !t.endsWith(":")) {
            assertThat(t).as(file + ": " + t).contains("${");
          }
        }
      }
    }
  }
}
