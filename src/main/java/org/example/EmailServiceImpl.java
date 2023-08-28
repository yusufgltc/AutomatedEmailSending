package org.example;

import org.springframework.stereotype.Component;

import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

@Component
public class EmailServiceImpl implements EmailService{

    private final Session session;
    private final String senderEmail;
    private final EmailCreator emailCreator;

    public EmailServiceImpl(Session session, String senderEmail, EmailCreator emailCreator) {
        this.session = session;
        this.senderEmail = senderEmail;
        this.emailCreator = emailCreator;
    }

    @Override
    public void sendEmail(Mail mail) {
        var message = new MimeMessage(session);

        try {
            message.setFrom(new InternetAddress(senderEmail));
            for (String address: mail.recipients()) {
                message.addRecipient(Message.RecipientType.TO, new InternetAddress(address));
            }
            message.setSubject(mail.subject());
            message.setContent(emailCreator.getMimeMultiPart(mail.body(), mail.attachedFiles()));
            Transport.send(message, message.getAllRecipients());
        } catch (MessagingException messagingException) {
            throw new RuntimeException(messagingException);
        }
    }
}
