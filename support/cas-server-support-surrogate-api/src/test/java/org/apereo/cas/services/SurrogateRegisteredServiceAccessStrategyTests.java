package org.apereo.cas.services;

import org.apereo.cas.authentication.surrogate.SurrogateAuthenticationService;
import org.apereo.cas.util.CollectionUtils;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.junit4.SpringRunner;

import static org.junit.Assert.*;

/**
 * This is {@link SurrogateRegisteredServiceAccessStrategyTests}.
 *
 * @author Misagh Moayyed
 * @since 5.3.0
 */
@RunWith(SpringRunner.class)
public class SurrogateRegisteredServiceAccessStrategyTests {
    @Test
    public void verifySurrogateDisabled() {
        final SurrogateRegisteredServiceAccessStrategy a = new SurrogateRegisteredServiceAccessStrategy();
        a.setSurrogateEnabled(false);
        final boolean result = a.doPrincipalAttributesAllowServiceAccess("casuser",
            CollectionUtils.wrap(SurrogateAuthenticationService.AUTHENTICATION_ATTR_SURROGATE_ENABLED, true));
        assertFalse(result);
    }

    @Test
    public void verifySurrogateDisabledWithAttributes() {
        final SurrogateRegisteredServiceAccessStrategy a = new SurrogateRegisteredServiceAccessStrategy();
        a.setSurrogateEnabled(true);
        a.setSurrogateRequiredAttributes(CollectionUtils.wrap("surrogateA", "surrogateV"));
        final boolean result = a.doPrincipalAttributesAllowServiceAccess("casuser",
            CollectionUtils.wrap(SurrogateAuthenticationService.AUTHENTICATION_ATTR_SURROGATE_ENABLED, true));
        assertFalse(result);
    }

    @Test
    public void verifySurrogateAllowed() {
        final SurrogateRegisteredServiceAccessStrategy a = new SurrogateRegisteredServiceAccessStrategy();
        a.setSurrogateEnabled(true);
        final boolean result = a.doPrincipalAttributesAllowServiceAccess("casuser",
            CollectionUtils.wrap(SurrogateAuthenticationService.AUTHENTICATION_ATTR_SURROGATE_ENABLED, true));
        assertTrue(result);
    }
}
