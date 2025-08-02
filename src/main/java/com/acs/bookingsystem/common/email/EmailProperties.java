package com.acs.bookingsystem.common.email;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.Map;

@Getter
@Setter
@Component
@ConfigurationProperties(prefix = "email")
public class EmailProperties {

    private Sender sender;
    private Smtp smtp;
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
    }

    @Getter
    @Setter
    public static class Template {
        private String subject;
        private String body;
    }
}
