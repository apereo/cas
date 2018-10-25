package org.apereo.cas.authentication.adaptive.intel;

import org.apereo.cas.configuration.model.core.authentication.AdaptiveAuthenticationProperties;
import org.apereo.cas.util.junit.DisabledIfContinuousIntegration;

import lombok.val;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.IfProfileValue;
import org.springframework.webflow.test.MockRequestContext;

import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link BlackDotIPAddressIntelligenceServiceTests}.
 *
 * @author Misagh Moayyed
 * @since 6.0.0
 */
@IfProfileValue(name = "blackDotEnabled", value = "true")
@SpringBootTest
@DisabledIfContinuousIntegration
public class BlackDotIPAddressIntelligenceServiceTests {
    @Test
    public void verifyBannedOperation() {
        val props = new AdaptiveAuthenticationProperties();
        props.getIpIntel().getBlackDot().setEmailAddress("cas@apereo.org");
        val service = new BlackDotIPAddressIntelligenceService(props);
        val response = service.examine(new MockRequestContext(), "37.58.59.181");
        assertTrue(response.isBanned());
    }

    @Test
    public void verifyAllowedOperation() {
        val props = new AdaptiveAuthenticationProperties();
        props.getIpIntel().getBlackDot().setEmailAddress("cas@apereo.org");
        val service = new BlackDotIPAddressIntelligenceService(props);
        val response = service.examine(new MockRequestContext(), "8.8.8.8");
        assertTrue(response.isAllowed());
    }
}
