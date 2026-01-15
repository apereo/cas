package org.apereo.cas.mail;

import module java.base;
import org.apereo.cas.config.CasCoreWebAutoConfiguration;
import org.apereo.cas.config.CasMailjetEmailSenderAutoConfiguration;
import org.apereo.cas.configuration.model.support.email.EmailProperties;
import org.apereo.cas.notifications.mail.EmailMessageRequest;
import org.apereo.cas.notifications.mail.EmailSender;
import org.apereo.cas.services.RegisteredServiceTestUtils;
import org.apereo.cas.test.CasTestExtension;
import org.apereo.cas.util.spring.boot.SpringBootTestAutoConfigurations;
import lombok.Getter;
import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link MailjetEmailSenderTests}.
 *
 * @author Misagh Moayyed
 * @since 7.1.0
 */
@SpringBootTestAutoConfigurations
@SpringBootTest(classes = {
    CasCoreWebAutoConfiguration.class,
    CasMailjetEmailSenderAutoConfiguration.class
}, properties = {
    "cas.email-provider.mailjet.sandbox-mode=true",
    "cas.email-provider.mailjet.api-key=1234567890",
    "cas.email-provider.mailjet.secret-key=1234567890"
})
@Tag("Mail")
@ExtendWith(CasTestExtension.class)
@Getter
class MailjetEmailSenderTests {
    @Autowired
    @Qualifier(EmailSender.BEAN_NAME)
    private EmailSender emailSender;

    @Test
    void verifyOperation() throws Exception {
        assertNotNull(emailSender);
        val principal = RegisteredServiceTestUtils.getPrincipal("casuser",
            Map.of("email", List.of("example@gmail.com")));
        val emailRequest = EmailMessageRequest.builder()
            .locale(Locale.GERMANY)
            .body("This is the email body")
            .emailProperties(new EmailProperties()
                .setSubject("This is the subject")
                .setFrom("person@gmail.com"))
            .attribute("email")
            .principal(principal)
            .build();
        assertFalse(emailSender.send(emailRequest).isSuccess());
    }
}
