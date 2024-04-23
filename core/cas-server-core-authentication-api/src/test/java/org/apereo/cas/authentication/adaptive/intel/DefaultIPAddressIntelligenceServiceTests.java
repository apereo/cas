package org.apereo.cas.authentication.adaptive.intel;

import org.apereo.cas.configuration.model.core.authentication.AdaptiveAuthenticationProperties;
import org.apereo.cas.util.MockRequestContext;

import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link DefaultIPAddressIntelligenceServiceTests}.
 *
 * @author Misagh Moayyed
 * @since 6.3.0
 */
@Tag("Authentication")
class DefaultIPAddressIntelligenceServiceTests {
    @Test
    void verifyOperation() throws Throwable {
        val context = MockRequestContext.create();
        val input = new DefaultIPAddressIntelligenceService(new AdaptiveAuthenticationProperties());
        assertTrue(input.examine(context, "1.2.3.4").isAllowed());
    }

}
