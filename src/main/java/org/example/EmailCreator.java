package org.example;

import javax.activation.DataHandler;
import javax.activation.FileDataSource;
import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMultipart;
import java.io.File;

public class EmailCreator {
    public Multipart getMimeMultiPart(String bodyText, File... attachedFiles) throws MessagingException {
        var multipart = new MimeMultipart();
        var messageBodyPart = new MimeBodyPart();
        messageBodyPart.setText(bodyText);
        multipart.addBodyPart(messageBodyPart);

        for (File file : attachedFiles) {
            multipart.addBodyPart(createAttachment(file));
        }
        return multipart;
    }

    public MimeBodyPart createAttachment(File attachedFile) {
        var attachmentPart = new MimeBodyPart();
        var source = new FileDataSource(attachedFile);
        try {
            attachmentPart.setDataHandler(new DataHandler(source));
            attachmentPart.setFileName(attachedFile.getName());
        }catch (MessagingException e) {
            throw new RuntimeException(e);
        }
        return attachmentPart;
    }
}
