package org.apereo.cas.services;

import org.apereo.cas.authentication.surrogate.SurrogateAuthenticationService;
import org.apereo.cas.util.CollectionUtils;

import lombok.val;
import org.junit.jupiter.api.Test;

import static org.junit.Assert.*;

/**
 * This is {@link SurrogateRegisteredServiceAccessStrategyTests}.
 *
 * @author Misagh Moayyed
 * @since 5.3.0
 */
public class SurrogateRegisteredServiceAccessStrategyTests {
    @Test
    public void verifySurrogateDisabled() {
        val a = new SurrogateRegisteredServiceAccessStrategy();
        a.setSurrogateEnabled(false);
        val result = a.doPrincipalAttributesAllowServiceAccess("casuser",
            CollectionUtils.wrap(SurrogateAuthenticationService.AUTHENTICATION_ATTR_SURROGATE_ENABLED, true));
        assertFalse(result);
    }

    @Test
    public void verifySurrogateDisabledWithAttributes() {
        val a = new SurrogateRegisteredServiceAccessStrategy();
        a.setSurrogateEnabled(true);
        a.setSurrogateRequiredAttributes(CollectionUtils.wrap("surrogateA", "surrogateV"));
        val result = a.doPrincipalAttributesAllowServiceAccess("casuser",
            CollectionUtils.wrap(SurrogateAuthenticationService.AUTHENTICATION_ATTR_SURROGATE_ENABLED, true));
        assertFalse(result);
    }

    @Test
    public void verifySurrogateAllowed() {
        val a = new SurrogateRegisteredServiceAccessStrategy();
        a.setSurrogateEnabled(true);
        val result = a.doPrincipalAttributesAllowServiceAccess("casuser",
            CollectionUtils.wrap(SurrogateAuthenticationService.AUTHENTICATION_ATTR_SURROGATE_ENABLED, true));
        assertTrue(result);
    }
}
