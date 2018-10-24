package org.apereo.cas.util.io;

import org.apereo.cas.authentication.principal.Principal;
import org.apereo.cas.category.MailCategory;
import org.apereo.cas.config.CasCoreUtilConfiguration;
import org.apereo.cas.util.CollectionUtils;
import org.apereo.cas.util.junit.ConditionalIgnore;
import org.apereo.cas.util.junit.ConditionalIgnoreRule;
import org.apereo.cas.util.junit.RunningContinuousIntegrationCondition;

import lombok.val;
import org.junit.Rule;
import org.junit.experimental.categories.Category;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.mail.MailSenderAutoConfiguration;
import org.springframework.boot.autoconfigure.mail.MailSenderValidatorAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.autoconfigure.RefreshAutoConfiguration;
import org.springframework.test.context.TestPropertySource;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

/**
 * This is {@link CommunicationsManagerTests}.
 *
 * @author Misagh Moayyed
 * @since 5.3.0
 */
@SpringBootTest(classes = {
    RefreshAutoConfiguration.class,
    CasCoreUtilConfiguration.class,
    MailSenderAutoConfiguration.class,
    MailSenderValidatorAutoConfiguration.class
})
@Category(MailCategory.class)
@ConditionalIgnore(condition = RunningContinuousIntegrationCondition.class, port = 25000)
@TestPropertySource(properties = {"spring.mail.host=localhost", "spring.mail.port=25000", "spring.mail.testConnection=true"})
public class CommunicationsManagerTests {

    @Rule
    public final ConditionalIgnoreRule conditionalIgnoreRule = new ConditionalIgnoreRule();

    @Autowired
    @Qualifier("communicationsManager")
    private CommunicationsManager communicationsManager;

    @Test
    public void verifyMailSender() {
        assertTrue(communicationsManager.isMailSenderDefined());
        assertTrue(communicationsManager.email("Test Body", "cas@example.org", "Subject", "sample@example.org"));
        val p = mock(Principal.class);
        when(p.getId()).thenReturn("casuser");
        when(p.getAttributes()).thenReturn(CollectionUtils.wrap("email", "cas@example.org"));
        assertTrue(communicationsManager.email(p, "email", "Body",
            "cas@example.org", "Test Subject", "cc@example.org", "bcc@example.org"));
        assertTrue(communicationsManager.email("Test Body", "cas@example.org", "Subject",
            "sample@example.org", "cc@example.org", "bcc@example.org"));
    }
}
