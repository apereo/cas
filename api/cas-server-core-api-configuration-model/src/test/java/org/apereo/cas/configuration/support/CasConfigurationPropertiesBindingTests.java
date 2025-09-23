package org.apereo.cas.configuration.support;

import org.apereo.cas.configuration.CasConfigurationProperties;
import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.mail.MailProperties;
import java.util.Map;
import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link CasConfigurationPropertiesBindingTests}.
 *
 * @author Misagh Moayyed
 * @since 7.3.0
 */
@Tag("CasConfiguration")
class CasConfigurationPropertiesBindingTests {
    @Test
    void verifyOperation() {
        val payload = Map.<String, Object>of(
            "cas.server.name", "https://sso.test.org",
            "spring.mail.host", "localhost",
            "spring.mail.port", "25000"
        );
        val bindingContext = CasConfigurationProperties.bindFrom(payload);
        assertEquals("https://sso.test.org", bindingContext.value().getServer().getName());

        val mailProperties = CasConfigurationProperties.bindFrom(payload, MailProperties.class).value();
        assertEquals("localhost", mailProperties.getHost());
        assertEquals(25000, mailProperties.getPort());
    }
}
