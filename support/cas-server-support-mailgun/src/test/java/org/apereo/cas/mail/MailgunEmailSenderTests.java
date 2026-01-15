package org.apereo.cas.mail;

import module java.base;
import org.apereo.cas.config.CasCoreWebAutoConfiguration;
import org.apereo.cas.config.CasMailgunEmailSenderAutoConfiguration;
import org.apereo.cas.configuration.model.support.email.EmailProperties;
import org.apereo.cas.notifications.mail.EmailMessageRequest;
import org.apereo.cas.notifications.mail.EmailSender;
import org.apereo.cas.services.RegisteredServiceTestUtils;
import org.apereo.cas.test.CasTestExtension;
import org.apereo.cas.util.spring.boot.SpringBootTestAutoConfigurations;
import lombok.Getter;
import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link MailgunEmailSenderTests}.
 *
 * @author Misagh Moayyed
 * @since 7.2.0
 */
@SpringBootTestAutoConfigurations
@SpringBootTest(classes = {
    CasCoreWebAutoConfiguration.class,
    CasMailgunEmailSenderAutoConfiguration.class
}, properties = {
    "cas.email-provider.mailgun.test-mode=true",
    "cas.email-provider.mailgun.api-key=59887ca85c4512f2d612ad7678fd2587-da554c25-123456",
    "cas.email-provider.mailgun.domain=sandboxa47ca2002901479296b3b508e922ff9c.mailgun.org"
})
@Tag("Mail")
@ExtendWith(CasTestExtension.class)
@Getter
class MailgunEmailSenderTests {
    @Autowired
    @Qualifier(EmailSender.BEAN_NAME)
    private EmailSender emailSender;

    @ParameterizedTest
    @ValueSource(booleans = {true, false})
    void verifyOperation(final boolean html) throws Exception {
        val principal = RegisteredServiceTestUtils.getPrincipal("casuser",
            Map.of("email", List.of("mm1844@gmail.com")));
        val emailRequest = EmailMessageRequest.builder()
            .locale(Locale.GERMANY)
            .body("This is the email body")
            .emailProperties(new EmailProperties()
                .setHtml(html)
                .setCc(List.of("mm1844@gmail.com"))
                .setBcc(List.of("mm1844@gmail.com"))
                .setSubject("This is the subject")
                .setFrom("mm1844@gmail.com"))
            .attribute("email")
            .principal(principal)
            .build();
        assertFalse(emailSender.send(emailRequest).isSuccess());
    }
}
