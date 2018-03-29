package org.apereo.cas.services;

import org.apereo.cas.authentication.surrogate.SurrogateAuthenticationService;
import org.apereo.cas.util.CollectionUtils;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.junit4.SpringRunner;

import static org.junit.Assert.*;

/**
 * This is {@link GroovySurrogateRegisteredServiceAccessStrategyTests}.
 *
 * @author Misagh Moayyed
 * @since 5.3.0
 */
@RunWith(SpringRunner.class)
public class GroovySurrogateRegisteredServiceAccessStrategyTests {
    @Test
    public void verifySurrogateDisabled() {
        final var a = new GroovySurrogateRegisteredServiceAccessStrategy();
        a.setGroovyScript("classpath:/surrogate-access.groovy");
        final var result = a.doPrincipalAttributesAllowServiceAccess("casuser-disabled",
            CollectionUtils.wrap(SurrogateAuthenticationService.AUTHENTICATION_ATTR_SURROGATE_ENABLED, true));
        assertFalse(result);
    }

    @Test
    public void verifySurrogateAllowed() {
        final var a = new GroovySurrogateRegisteredServiceAccessStrategy();
        a.setGroovyScript("classpath:/surrogate-access.groovy");
        final var result = a.doPrincipalAttributesAllowServiceAccess("casuser-enabled",
            CollectionUtils.wrap(SurrogateAuthenticationService.AUTHENTICATION_ATTR_SURROGATE_ENABLED, true));
        assertTrue(result);
    }
}
