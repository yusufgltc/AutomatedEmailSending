package org.example;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

import javax.mail.Authenticator;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import java.util.Properties;

@Configuration
@EnableConfigurationProperties
@Import(EmailProperties.class)
public class EmailConfiguration {

    @Bean
    public Session session(EmailProperties emailProperties) {
        Properties props = System.getProperties();
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", "true");
        props.put("mail.smtp.ssl.trust", emailProperties.getHost());
        props.put("mail.smtp.port", emailProperties.getPort());
        props.put("mail.smtp.host", emailProperties.getHost());

        Authenticator auth = new Authenticator() {
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(emailProperties.getUser().getMail(),emailProperties.getUser().getPassword());
            }
        };
        return Session.getInstance(props, auth);
    }

    @Bean
    public EmailCreator emailCreator() {
        return new EmailCreator();
    }

    @Bean
    public EmailService emailService(Session session, EmailProperties emailProperties, EmailCreator emailCreator) {
        return new EmailServiceImpl(session, emailProperties.getUser().getMail(), emailCreator);
    }
}
