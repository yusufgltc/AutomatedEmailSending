package org.example;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import javax.mail.MessagingException;
import java.io.File;
import java.io.IOException;

public class EmailCreatorTest {
    private final EmailCreator emailCreator = new EmailCreator();

    @Test
    public void shouldCreateEmailWithoutAttachment() throws MessagingException, IOException {
        var result = emailCreator.getMimeMultiPart("dummy");
        Assertions.assertEquals(result.getBodyPart(0).getContent(), "dummy");
    }

    @Test
    public void shouldCreateEmailWithAttachment() throws MessagingException, IOException {
        var file = new File("dummy_prefix.txt");
        var result = emailCreator.getMimeMultiPart("dummy", file);

        Assertions.assertEquals(result.getBodyPart(0).getContent(), "dummy");
        Assertions.assertEquals(result.getBodyPart(1).getDataHandler().getName(), "dummy_prefix.txt");
    }
}
