package org.apereo.cas.notifications.mail;

import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.configuration.model.support.email.EmailProperties;
import org.apereo.cas.notifications.BaseNotificationTests;
import org.apereo.cas.test.CasTestExtension;
import org.apereo.cas.util.CollectionUtils;
import org.apereo.cas.util.junit.EnabledIfListeningOnPort;
import lombok.val;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.HierarchicalMessageSource;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * This is {@link DefaultEmailSenderTests}.
 *
 * @author Misagh Moayyed
 * @since 7.1.0
 */
@Tag("Mail")
@ExtendWith(CasTestExtension.class)
@EnabledIfListeningOnPort(port = 25000)
@EnableConfigurationProperties(CasConfigurationProperties.class)
class DefaultEmailSenderTests {

    @Nested
    @SpringBootTest(
        classes = BaseNotificationTests.SharedTestConfiguration.class,
        properties = {
            "spring.ssl.bundle.jks.mail-bundle.key-store.location=classpath:keystore.p12",
            "spring.ssl.bundle.jks.mail-bundle.key-store.password=changeit",
            "spring.ssl.bundle.jks.mail-bundle.key-store.type=PKCS12",
            "spring.ssl.bundle.jks.mail-bundle.trust-store.location=classpath:keystore.p12",
            "spring.ssl.bundle.jks.mail-bundle.trust-store.password=changeit",
            "spring.ssl.bundle.jks.mail-bundle.trust-store.type=PKCS12",

            "spring.mail.host=localhost",
            "spring.mail.port=25000",
            "spring.mail.ssl.enabled=true",
            "spring.mail.ssl.bundle=mail-bundle",
            "spring.mail.properties.mail.smtp.connectiontimeout=5000",
            "spring.mail.properties.mail.smtp.ssl.checkserveridentity=false"
        })
    class DefaultTests {
        @Autowired
        @Qualifier(EmailSender.BEAN_NAME)
        private EmailSender emailSender;

        @Test
        void verifySubjectCanExpandWithVariables() throws Throwable {
            val emailRequest = EmailMessageRequest
                .builder()
                .context(CollectionUtils.wrap("name", "casuser"))
                .emailProperties(new EmailProperties().setSubject("Hello ${name}"))
                .build();
            val subject = emailSender.determineEmailSubject(emailRequest, mock(HierarchicalMessageSource.class));
            assertEquals("Hello casuser", subject);
            assertFalse(emailSender.send(emailRequest).isSuccess());
        }
    }

    @Nested
    @SpringBootTest(classes = BaseNotificationTests.SharedTestConfiguration.class,
        properties = {
            "cas.multitenancy.core.enabled=true",
            "cas.multitenancy.json.location=classpath:/tenants.json"
        })
    class MultitenancyTests {
        @Autowired
        @Qualifier(EmailSender.BEAN_NAME)
        private EmailSender emailSender;

        @Test
        void verifyEmailSentPerTenant() throws Throwable {
            val emailRequest = EmailMessageRequest
                .builder()
                .context(CollectionUtils.wrap("name", "casuser"))
                .emailProperties(new EmailProperties().setSubject("Hello ${name}").setFrom("cas@example.org"))
                .tenant("shire")
                .body("This is my body!")
                .to(List.of("casuser@example.org"))
                .build();
            assertTrue(emailSender.send(emailRequest).isSuccess());
        }
    }
}
