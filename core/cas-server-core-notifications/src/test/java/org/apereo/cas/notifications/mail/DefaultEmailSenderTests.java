package org.apereo.cas.notifications.mail;

import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.configuration.model.support.email.EmailProperties;
import org.apereo.cas.notifications.BaseNotificationTests;
import org.apereo.cas.test.CasTestExtension;
import org.apereo.cas.util.CollectionUtils;
import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.HierarchicalMessageSource;
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
@SpringBootTest(classes = BaseNotificationTests.SharedTestConfiguration.class)
@EnableConfigurationProperties(CasConfigurationProperties.class)
class DefaultEmailSenderTests {
    @Autowired
    @Qualifier(EmailSender.BEAN_NAME)
    private EmailSender emailSender;

    @Test
    void verifySubjectCanExpandWithVariables() throws Exception {
        val emailRequest = EmailMessageRequest.builder()
            .context(CollectionUtils.wrap("name", "casuser"))
            .emailProperties(new EmailProperties().setSubject("Hello ${name}"))
            .build();
        val subject = emailSender.determineEmailSubject(emailRequest, mock(HierarchicalMessageSource.class));
        assertEquals("Hello casuser", subject);
    }
}
