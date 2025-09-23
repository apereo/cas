package org.apereo.cas.mail;

import org.apereo.cas.config.CasAmazonSimpleEmailServiceAutoConfiguration;
import org.apereo.cas.config.CasCoreEnvironmentBootstrapAutoConfiguration;
import org.apereo.cas.config.CasCoreMultitenancyAutoConfiguration;
import org.apereo.cas.config.CasCoreWebAutoConfiguration;
import org.apereo.cas.configuration.model.support.email.EmailProperties;
import org.apereo.cas.notifications.mail.EmailMessageRequest;
import org.apereo.cas.notifications.mail.EmailSender;
import org.apereo.cas.services.RegisteredServiceTestUtils;
import org.apereo.cas.test.CasTestExtension;
import org.apereo.cas.util.junit.EnabledIfListeningOnPort;
import org.apereo.cas.util.spring.boot.SpringBootTestAutoConfigurations;
import lombok.Getter;
import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link AmazonSimpleEmailServiceEmailSenderTests}.
 *
 * @author Misagh Moayyed
 * @since 7.1.0
 */
@SpringBootTestAutoConfigurations
@SpringBootTest(classes = {
    CasCoreWebAutoConfiguration.class,
    CasCoreMultitenancyAutoConfiguration.class,
    CasCoreEnvironmentBootstrapAutoConfiguration.class,
    CasAmazonSimpleEmailServiceAutoConfiguration.class
}, properties = {
    "cas.email-provider.ses.endpoint=http://127.0.0.1:4566",
    "cas.email-provider.ses.region=us-east-1",
    "cas.email-provider.ses.credential-access-key=test",
    "cas.email-provider.ses.credential-secret-key=test"
})
@EnabledIfListeningOnPort(port = 4566)
@Tag("AmazonWebServices")
@ExtendWith(CasTestExtension.class)
@Getter
class AmazonSimpleEmailServiceEmailSenderTests {
    @Autowired
    @Qualifier(EmailSender.BEAN_NAME)
    private EmailSender emailSender;

    @Test
    void verifyOperation() throws Exception {
        assertNotNull(emailSender);
        val principal = RegisteredServiceTestUtils.getPrincipal("casuser",
            Map.of("email", List.of("hello@example.com")));
        val emailRequest = EmailMessageRequest.builder()
            .locale(Locale.GERMANY)
            .body("This is the email body")
            .emailProperties(new EmailProperties()
                .setSubject("This is the subject")
                .setFrom("hello@example.com"))
            .attribute("email")
            .principal(principal)
            .build();
        assertTrue(emailSender.send(emailRequest).isSuccess());
    }
}
