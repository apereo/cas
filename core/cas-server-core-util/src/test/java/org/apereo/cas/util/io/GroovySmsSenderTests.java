package org.apereo.cas.util.io;

import org.apereo.cas.config.CasCoreUtilConfiguration;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.mail.MailSenderAutoConfiguration;
import org.springframework.boot.autoconfigure.mail.MailSenderValidatorAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.autoconfigure.RefreshAutoConfiguration;
import org.springframework.test.context.TestPropertySource;

import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link GroovySmsSenderTests}.
 *
 * @author Misagh Moayyed
 * @since 6.0.0
 */
@SpringBootTest(classes = {
    RefreshAutoConfiguration.class,
    CasCoreUtilConfiguration.class,
    MailSenderAutoConfiguration.class,
    MailSenderValidatorAutoConfiguration.class
})
@TestPropertySource(properties = {"cas.smsProvider.groovy.location=classpath:/GroovySmsSender.groovy"})
public class GroovySmsSenderTests {
    @Autowired
    @Qualifier("communicationsManager")
    private CommunicationsManager communicationsManager;

    @Test
    public void verifyOperation() {
        assertTrue(communicationsManager.isSmsSenderDefined());
        assertTrue(communicationsManager.sms("CAS", "CAS", "Hello CAS"));
    }
}
