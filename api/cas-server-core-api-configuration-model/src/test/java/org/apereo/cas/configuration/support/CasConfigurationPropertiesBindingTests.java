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
    void verifyOperation() throws Exception {
        val payload = Map.<String, Object>of(
            "cas.server.name", "https://sso.test.org",
            "spring.mail.host", "localhost",
            "spring.mail.port", "25000"
        );
        val properties = CasConfigurationProperties.bindFrom(payload).orElseThrow();
        assertEquals("https://sso.test.org", properties.getServer().getName());

        val emailProps = CasConfigurationProperties.bindFrom(payload, MailProperties.class).orElseThrow();
        assertEquals("localhost", emailProps.getHost());
        assertEquals(25000, emailProps.getPort());
    }
}
