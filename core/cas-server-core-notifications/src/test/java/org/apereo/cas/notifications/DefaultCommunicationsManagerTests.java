package org.apereo.cas.notifications;

import org.apereo.cas.authentication.CoreAuthenticationTestUtils;
import org.apereo.cas.authentication.principal.Principal;
import org.apereo.cas.configuration.model.support.email.EmailProperties;
import org.apereo.cas.notifications.mail.EmailMessageBodyBuilder;
import org.apereo.cas.notifications.mail.EmailMessageRequest;
import org.apereo.cas.notifications.sms.SmsRequest;
import org.apereo.cas.test.CasTestExtension;
import org.apereo.cas.util.CollectionUtils;
import org.apereo.cas.util.junit.EnabledIfListeningOnPort;
import lombok.val;
import org.apache.commons.io.FileUtils;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.List;
import java.util.Map;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * This is {@link DefaultCommunicationsManagerTests}.
 *
 * @author Misagh Moayyed
 * @since 5.3.0
 */
@SpringBootTest(classes = BaseNotificationTests.SharedTestConfiguration.class, properties = {
    "spring.mail.host=localhost",
    "spring.mail.port=25000"
})
@Tag("Mail")
@ExtendWith(CasTestExtension.class)
@EnabledIfListeningOnPort(port = 25000)
class DefaultCommunicationsManagerTests {
    @Autowired
    @Qualifier(CommunicationsManager.BEAN_NAME)
    private CommunicationsManager communicationsManager;

    @Test
    void verifyMailSender() {
        assertTrue(communicationsManager.isMailSenderDefined());

        var props = new EmailProperties();
        props.setText("Test Body");
        props.setSubject("Subject");
        props.setFrom("cas@example.org");
        props.setCc(List.of("cc@example.org"));
        props.setBcc(List.of("bcc@example.org"));
        props.setReplyTo("bcc1@example.org");
        val body = EmailMessageBodyBuilder.builder().properties(props).build().get();
        var emailRequest = EmailMessageRequest.builder().emailProperties(props)
            .to(List.of("sample@example.net")).body(body).build();
        assertTrue(communicationsManager.email(emailRequest).isSuccess());
        val p = mock(Principal.class);
        when(p.getId()).thenReturn("casuser");
        when(p.getAttributes()).thenReturn(CollectionUtils.wrap("email", List.of("cas@example.org")));
        emailRequest = EmailMessageRequest.builder().emailProperties(props)
            .principal(p).attribute("email").body(body).build();
        assertTrue(communicationsManager.email(emailRequest).isSuccess());
    }

    @Test
    void verifyEmailWithLocalizedSubject() {
        val props = new EmailProperties();
        props.setText("Hello World");
        props.setSubject("#{my.subject}");
        props.setFrom("cas@example.org");
        val body = EmailMessageBodyBuilder.builder().properties(props).build().get();
        val emailRequest = EmailMessageRequest.builder().emailProperties(props)
            .principal(CoreAuthenticationTestUtils.getPrincipal())
            .to(List.of("sample@example.org")).body(body).build();
        assertTrue(communicationsManager.email(emailRequest).isSuccess());
    }

    @Test
    void verifyMailSenderWithTemplateBody() throws Throwable {
        assertTrue(communicationsManager.isMailSenderDefined());

        val tempFile = Files.createTempFile("prefix", "postfix").toFile();
        FileUtils.write(tempFile, "This is an example with %s %s", StandardCharsets.UTF_8);

        val props = new EmailProperties();
        props.setText(tempFile.getCanonicalPath());
        props.setSubject("Subject");
        props.setFrom("cas@example.org");
        val body = EmailMessageBodyBuilder.builder().properties(props)
            .parameters(Map.of("k1", "param1", "k2", "param2")).build().get();
        val emailRequest = EmailMessageRequest.builder().emailProperties(props)
            .principal(CoreAuthenticationTestUtils.getPrincipal())
            .to(List.of("sample@example.org")).body(body).build();
        assertTrue(communicationsManager.email(emailRequest).isSuccess());
    }

    @Test
    void verifyMailNoAtr() {
        assertTrue(communicationsManager.isMailSenderDefined());
        val emailRequest = EmailMessageRequest.builder()
            .principal(mock(Principal.class))
            .attribute("bad-attribute")
            .emailProperties(new EmailProperties())
            .build();
        assertFalse(communicationsManager.email(emailRequest).isSuccess());
    }

    @Test
    void verifySmsNoAtr() {
        assertFalse(communicationsManager.isSmsSenderDefined());
        val smsRequest = SmsRequest.builder()
            .principal(mock(Principal.class))
            .attribute("bad-attribute")
            .text("sms text")
            .build();
        assertFalse(communicationsManager.sms(smsRequest));
    }

    @Test
    void verifyNoSmsSender() {
        assertFalse(communicationsManager.isSmsSenderDefined());
        assertFalse(communicationsManager.sms(SmsRequest.builder().build()));
    }

    @Test
    void verifyValidate() {
        assertTrue(communicationsManager.validate());
    }
}
