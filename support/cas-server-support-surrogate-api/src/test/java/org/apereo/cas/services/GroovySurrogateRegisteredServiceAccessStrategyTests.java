package org.apereo.cas.services;

import org.apereo.cas.authentication.surrogate.SurrogateAuthenticationService;
import org.apereo.cas.util.CollectionUtils;

import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link GroovySurrogateRegisteredServiceAccessStrategyTests}.
 *
 * @author Misagh Moayyed
 * @since 5.3.0
 */
@Tag("Groovy")
public class GroovySurrogateRegisteredServiceAccessStrategyTests {
    @Test
    public void verifySurrogateDisabled() {
        val a = new GroovySurrogateRegisteredServiceAccessStrategy();
        a.setGroovyScript("classpath:/surrogate-access.groovy");
        assertFalse(executeStrategy("casuser-disabled", true, a));
    }

    private static boolean executeStrategy(final String principal, final boolean surrogate, final GroovySurrogateRegisteredServiceAccessStrategy strategy) {
        val request = RegisteredServiceAccessStrategyRequest.builder().principalId(principal)
            .attributes(surrogate ? CollectionUtils.wrap(SurrogateAuthenticationService.AUTHENTICATION_ATTR_SURROGATE_ENABLED, true) : Map.of())
            .build();
        return strategy.doPrincipalAttributesAllowServiceAccess(request);
    }

    @Test
    public void verifySurrogateFails() {
        val a = new GroovySurrogateRegisteredServiceAccessStrategy();
        a.setGroovyScript("classpath:/surrogate-access.groovy");
        assertFalse(executeStrategy("casuser-fail", true, a));
    }

    @Test
    public void verifySurrogateAllowed() {
        val a = new GroovySurrogateRegisteredServiceAccessStrategy();
        a.setGroovyScript("classpath:/surrogate-access.groovy");
        assertTrue(executeStrategy("casuser-enabled", true, a));
    }

    @Test
    public void verifyNoSurrogateSession() {
        val a = new GroovySurrogateRegisteredServiceAccessStrategy();
        a.setGroovyScript("classpath:/surrogate-access.groovy");
        assertTrue(executeStrategy("casuser", false, a));
    }
}
