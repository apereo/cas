package org.apereo.cas.authentication.adaptive.intel;

import org.apereo.cas.configuration.model.core.authentication.AdaptiveAuthenticationProperties;

import lombok.val;
import org.junit.jupiter.api.Test;
import org.springframework.core.io.ClassPathResource;
import org.springframework.webflow.test.MockRequestContext;

import static org.junit.Assert.*;

/**
 * This is {@link GroovyIPAddressIntelligenceServiceTests}.
 *
 * @author Misagh Moayyed
 * @since 6.0.0
 */
public class GroovyIPAddressIntelligenceServiceTests {
    @Test
    public void verifyOperation() {
        val script = new ClassPathResource("GroovyIPAddressIntelligenceService.groovy");
        val props = new AdaptiveAuthenticationProperties();
        props.getIpIntel().getGroovy().setLocation(script);
        val service = new GroovyIPAddressIntelligenceService(props);
        val response = service.examine(new MockRequestContext(), "1.2.3.4");
        assertTrue(response.isBanned());
    }
}
