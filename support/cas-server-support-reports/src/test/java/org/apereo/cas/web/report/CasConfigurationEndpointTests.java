package org.apereo.cas.web.report;

import org.apereo.cas.config.CasCoreEnvironmentBootstrapAutoConfiguration;
import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.test.context.TestPropertySource;
import java.util.UUID;
import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link CasConfigurationEndpointTests}.
 *
 * @author Misagh Moayyed
 * @since 7.0.0
 */
@TestPropertySource(properties = {
    "cas.standalone.configuration-security.psw=Q7M9w4NjYnBxb2#mW",
    "management.endpoint.casConfig.access=UNRESTRICTED"
})
@Tag("ActuatorEndpoint")
@ImportAutoConfiguration(CasCoreEnvironmentBootstrapAutoConfiguration.class)
class CasConfigurationEndpointTests extends AbstractCasEndpointTests {
    @Autowired
    @Qualifier("casConfigurationEndpoint")
    private CasConfigurationEndpoint casConfigurationEndpoint;

    @Test
    void verifyOperation() {
        val value = UUID.randomUUID().toString();
        val encoded = casConfigurationEndpoint.encrypt(value).getBody();
        assertEquals(casConfigurationEndpoint.decrypt(encoded).getBody(), value);
    }
}
