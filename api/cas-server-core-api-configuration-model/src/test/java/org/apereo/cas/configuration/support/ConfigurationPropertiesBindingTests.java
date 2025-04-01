package org.apereo.cas.configuration.support;

import org.apereo.cas.configuration.CasConfigurationProperties;
import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import java.util.Map;
import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link ConfigurationPropertiesBindingTests}.
 *
 * @author Misagh Moayyed
 * @since 7.3.0
 */
@Tag("CasConfiguration")
class ConfigurationPropertiesBindingTests {
    @Test
    void verifyOperation() throws Exception {
        val payload = Map.<String, Object>of("cas.server.name", "https://sso.test.org");
        val properties = CasConfigurationProperties.bindFrom("Test1", payload).orElseThrow();
        assertEquals("https://sso.test.org", properties.getServer().getName());
    }
}
