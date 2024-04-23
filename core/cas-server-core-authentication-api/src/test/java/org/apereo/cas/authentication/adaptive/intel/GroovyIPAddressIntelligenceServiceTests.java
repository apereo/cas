package org.apereo.cas.authentication.adaptive.intel;

import org.apereo.cas.configuration.model.core.authentication.AdaptiveAuthenticationProperties;
import org.apereo.cas.util.MockRequestContext;
import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.core.io.ClassPathResource;
import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link GroovyIPAddressIntelligenceServiceTests}.
 *
 * @author Misagh Moayyed
 * @since 6.0.0
 */
@Tag("GroovyMfa")
class GroovyIPAddressIntelligenceServiceTests {
    @Test
    void verifyOperation() throws Throwable {
        val script = new ClassPathResource("GroovyIPAddressIntelligenceService.groovy");
        val props = new AdaptiveAuthenticationProperties();
        props.getIpIntel().getGroovy().setLocation(script);
        val service = new GroovyIPAddressIntelligenceService(props);
        val response = service.examine(MockRequestContext.create(), "1.2.3.4");
        assertTrue(response.isBanned());
    }
}
