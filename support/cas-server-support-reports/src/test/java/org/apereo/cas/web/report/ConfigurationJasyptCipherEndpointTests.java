package org.apereo.cas.web.report;

import org.apereo.cas.config.CasCoreBaseEnvironmentConfiguration;
import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.TestPropertySource;
import java.util.UUID;
import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link ConfigurationJasyptCipherEndpointTests}.
 *
 * @author Misagh Moayyed
 * @since 7.0.0
 */
@TestPropertySource(properties = {
    "cas.standalone.configuration-security.psw=Q7M9w4NjYnBxb2#mW",
    "management.endpoint.casConfig.enabled=true"
})
@Tag("ActuatorEndpoint")
@Import(CasCoreBaseEnvironmentConfiguration.class)
class ConfigurationJasyptCipherEndpointTests extends AbstractCasEndpointTests {
    @Autowired
    @Qualifier("casConfigurationCipherEndpoint")
    private ConfigurationJasyptCipherEndpoint casConfigurationCipherEndpoint;

    @Test
    void verifyOperation() throws Throwable {
        val value = UUID.randomUUID().toString();
        val encoded = casConfigurationCipherEndpoint.encrypt(value).getBody();
        assertEquals(casConfigurationCipherEndpoint.decrypt(encoded).getBody(), value);
    }
}
