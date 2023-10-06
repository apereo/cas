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
        val a = new SurrogateRegisteredServiceAccessStrategy();
        a.setSurrogateEnabled(false);
        assertFalse(executeStrategy(a));
    }

    private static boolean executeStrategy(final SurrogateRegisteredServiceAccessStrategy a) {
        val request = RegisteredServiceAccessStrategyRequest.builder().principalId("casuser")
            .attributes(CollectionUtils.wrap(SurrogateAuthenticationService.AUTHENTICATION_ATTR_SURROGATE_ENABLED, true))
            .build();
        return a.authorizeRequest(request);
    }

    @Test
    void verifySurrogateDisabledWithAttributes() throws Throwable {
        val a = new SurrogateRegisteredServiceAccessStrategy();
        a.setSurrogateEnabled(true);
        a.setSurrogateRequiredAttributes(CollectionUtils.wrap("surrogateA", "surrogateV"));
        assertFalse(executeStrategy(a));
    }

    @Test
    void verifySurrogateAttributesNotAvail() throws Throwable {
        val a = new SurrogateRegisteredServiceAccessStrategy();
        a.setSurrogateEnabled(true);
        a.setSurrogateRequiredAttributes(CollectionUtils.wrap("surrogateA", "surrogateV",
            "surrogateB", "surrogateZ"));
        assertFalse(executeStrategy(a));
    }

    @Test
    void verifySurrogateAllowed() throws Throwable {
        val a = new SurrogateRegisteredServiceAccessStrategy();
        a.setSurrogateEnabled(true);
        assertTrue(executeStrategy(a));
    }
}
