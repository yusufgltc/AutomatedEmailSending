package org.example;

import org.assertj.core.api.SoftAssertions;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.stubbing.Answer;

import javax.mail.*;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;
import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Properties;
import java.util.stream.Stream;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
public class EmailServiceImplTest {
    private static final String senderEmail = "senderEmail@example.org";
    @Mock
    private Session session;
    private EmailCreator emailCreator;
    private EmailServiceImpl emailServiceImpl;

    private static @Mock File fileMock;
    static List<String> recipients = List.of("oneAddress@example.org", "secondAddress@example.org");
    static String defaultBody = "dummy body";
    static String defaultSubject = "dummy subject";

    @BeforeEach
    public void setupEmailService() {
        this.session = Mockito.mock(Session.class);
        this.emailCreator = Mockito.mock(EmailCreator.class);
        var propertiesMock = Mockito.mock(Properties.class);
        given(session.getProperties()).willReturn(propertiesMock);
        this.emailServiceImpl = new EmailServiceImpl(session, senderEmail, emailCreator);
    }

    public static Stream<Arguments> mailInput() {
        return Stream.of(Arguments.of(new Mail(recipients, defaultSubject, defaultBody), "without attachment"), Arguments.of(new Mail(recipients, defaultSubject, defaultBody, fileMock), "with attachment"), Arguments.of(new Mail(recipients, "", defaultBody, fileMock), "with empty subject"), Arguments.of(new Mail(recipients, defaultSubject, "", fileMock), "with empty subject"), Arguments.of(new Mail(recipients.subList(0, 1), defaultSubject, defaultBody, fileMock), "with one recipient"));
    }

    @Test
    public void emailShouldContainSender() throws MessagingException {
        var mail = (Mail) mailInput().findFirst().get().get()[0];
        //given
        mockEmailCreator(mail);
        try (var staticTransport = Mockito.mockStatic(Transport.class)) {
            var message = ArgumentCaptor.forClass(MimeMessage.class);
            var address = ArgumentCaptor.forClass(Address[].class);
            staticTransport.when(() -> Transport.send(message.capture(), address.capture())).thenAnswer((Answer<Void>) invocation -> null);
            //when
            emailServiceImpl.sendEmail(mail);
            //then
            Assertions.assertEquals(message.getValue().getHeader("From")[0], senderEmail);
        }
    }

    @ParameterizedTest(name = "[{index}] Should send email {1} and assert address")
    @MethodSource("mailInput")
    public void emailShouldContainRecipients(Mail mail, String label) throws MessagingException {
        //given
        var softly = new SoftAssertions();
        mockEmailCreator(mail);
        try (var staticTransport = Mockito.mockStatic(Transport.class)) {
            var message = ArgumentCaptor.forClass(MimeMessage.class);
            var address = ArgumentCaptor.forClass(Address[].class);
            staticTransport.when(() -> Transport.send(message.capture(), address.capture())).thenAnswer((Answer<Void>) invocation -> null);
            //when
            emailServiceImpl.sendEmail(mail);
            //then
            softly.assertThat(address.getValue()).hasSize(mail.recipients().size());
            for (int i = 0; i<mail.recipients().size(); i++) {
                softly.assertThat(address.getValue()[i].toString()).isEqualTo(mail.recipients().get(i));
            }
            softly.assertAll();
        }
    }

    @ParameterizedTest(name = "[{index}] Should send email {1} and assert body")
    @MethodSource("mailInput")
    public void emailShouldContainBody(Mail mail, String label) throws MessagingException, IOException {
        //given
        mockEmailCreator(mail);
        try (var staticTransport = Mockito.mockStatic(Transport.class)) {
            var message = ArgumentCaptor.forClass(MimeMessage.class);
            var address = ArgumentCaptor.forClass(Address[].class);
            staticTransport.when(() -> Transport.send(message.capture(), address.capture())).thenAnswer((Answer<Void>) invocation -> null);
            //when
            emailServiceImpl.sendEmail(mail);
            //then
            Assertions.assertEquals(((Multipart) message.getValue().getContent()).getBodyPart(0).getContent(), mail.body());
        }
    }

    @ParameterizedTest(name = "[{index}] Should send email {1} and assert subject")
    @MethodSource("mailInput")
    public void emailShouldContainSubject(Mail mail, String label) throws MessagingException {
        //given
        mockEmailCreator(mail);
        try (var staticTransport = Mockito.mockStatic(Transport.class)) {
            var message = ArgumentCaptor.forClass(MimeMessage.class);
            var address = ArgumentCaptor.forClass(Address[].class);
            staticTransport.when(() -> Transport.send(message.capture(), address.capture())).thenAnswer((Answer<Void>) invocation -> null);
            //when
            emailServiceImpl.sendEmail(mail);
            //then
            Assertions.assertEquals(message.getValue().getHeader("Subject")[0], mail.subject());
        }
    }

    private void mockEmailCreator(Mail mail) throws MessagingException {
        var messageBodyPart = new MimeBodyPart();
        messageBodyPart.setText(mail.body());
        var mockMultipart = new MimeMultipart();
        mockMultipart.addBodyPart(messageBodyPart);

        given(emailCreator.getMimeMultiPart(eq(mail.body()), any())).willReturn(mockMultipart);
    }
}
