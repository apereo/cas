package org.apereo.cas.mail;

import org.apereo.cas.config.CasCoreEnvironmentBootstrapAutoConfiguration;
import org.apereo.cas.config.CasCoreMultitenancyAutoConfiguration;
import org.apereo.cas.config.CasCoreNotificationsAutoConfiguration;
import org.apereo.cas.config.CasCoreWebAutoConfiguration;
import org.apereo.cas.config.CasMicrosoftEmailSenderAutoConfiguration;
import org.apereo.cas.configuration.model.support.email.EmailProperties;
import org.apereo.cas.notifications.mail.EmailMessageRequest;
import org.apereo.cas.notifications.mail.EmailSender;
import org.apereo.cas.services.RegisteredServiceTestUtils;
import org.apereo.cas.test.CasTestExtension;
import org.apereo.cas.util.junit.EnabledIfListeningOnPort;
import org.apereo.cas.util.spring.boot.SpringBootTestAutoConfigurations;
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
 * This is {@link MicrosoftEmailSenderCustomizerTests}.
 *
 * @author Misagh Moayyed
 * @since 7.2.0
 */
@SpringBootTestAutoConfigurations
@SpringBootTest(classes = {
    CasCoreWebAutoConfiguration.class,
    CasCoreEnvironmentBootstrapAutoConfiguration.class,
    CasCoreMultitenancyAutoConfiguration.class,
    CasCoreNotificationsAutoConfiguration.class,
    CasMicrosoftEmailSenderAutoConfiguration.class
}, properties = {
    "spring.mail.host=localhost",
    "spring.mail.port=25000",
    "cas.email-provider.microsoft.client-id=d430f66f-bc3b-4e2d-a9bf-bf6c7ded8b7e",
    "cas.email-provider.microsoft.client-secret=${#environmentVariables['AZURE_AD_CLIENT_SECRET_MAIL']}",
    "cas.email-provider.microsoft.tenant-id=2bbf190a-1ee3-487d-b39f-4d5038acf9ad"
})
@Tag("Mail")
@ExtendWith(CasTestExtension.class)
@EnabledIfListeningOnPort(port = 25000)
class MicrosoftEmailSenderCustomizerTests {
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
