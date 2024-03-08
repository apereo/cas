package org.apereo.cas.services;

import org.apereo.cas.authentication.surrogate.SurrogateAuthenticationService;
import org.apereo.cas.util.CollectionUtils;

import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link SurrogateRegisteredServiceAccessStrategyTests}.
 *
 * @author Misagh Moayyed
 * @since 5.3.0
 */
@Tag("RegisteredService")
class SurrogateRegisteredServiceAccessStrategyTests {
    @Test
    void verifySurrogateDisabled() throws Throwable {
        val strategy = new SurrogateRegisteredServiceAccessStrategy();
        assertTrue(executeStrategy(strategy));
    }

    @Test
    void verifySurrogateDisabledWithAttributes() throws Throwable {
        val strategy = new SurrogateRegisteredServiceAccessStrategy();
        strategy.setSurrogateRequiredAttributes(CollectionUtils.wrap("surrogateA", "surrogateV"));
        assertFalse(executeStrategy(strategy));
    }

    @Test
    void verifySurrogateAttributesNotAvail() throws Throwable {
        val strategy = new SurrogateRegisteredServiceAccessStrategy();
        strategy.setSurrogateRequiredAttributes(CollectionUtils.wrap("surrogateA", "surrogateV",
            "surrogateB", "surrogateZ"));
        assertFalse(executeStrategy(strategy));
    }

    @Test
    void verifySurrogateAllowed() throws Throwable {
        val strategy = new SurrogateRegisteredServiceAccessStrategy();
        assertTrue(executeStrategy(strategy));
    }

    private static boolean executeStrategy(final RegisteredServiceAccessStrategy strategy) throws Throwable {
        val request = RegisteredServiceAccessStrategyRequest.builder().principalId("casuser")
            .attributes(CollectionUtils.wrap(SurrogateAuthenticationService.AUTHENTICATION_ATTR_SURROGATE_ENABLED, true))
            .build();
        return strategy.authorizeRequest(request);
    }
}
