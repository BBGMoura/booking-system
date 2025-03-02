package com.acs.bookingsystem.common.email;

import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.PropertySource;
import org.springframework.stereotype.Component;

@Component
@Getter
@PropertySource("classpath:email.properties")
public class EmailProperties {
    @Value("${sender.email}")
    private String senderEmail;
    @Value("${password}")
    private String password;
    @Value("${smtp.server}")
    private String smtpServer;
    @Value("${port}")
    private int port;

    @Value("${email.invitation.subject}")
    private String emailInvitationSubject;
    @Value("${email.invitation.body}")
    private String emailInvitationBody;
    public static final String RESET_PASSWORD = "Reset Password";

}
