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
        val result = a.doPrincipalAttributesAllowServiceAccess("casuser-disabled",
            CollectionUtils.wrap(SurrogateAuthenticationService.AUTHENTICATION_ATTR_SURROGATE_ENABLED, true));
        assertFalse(result);
    }

    @Test
    public void verifySurrogateFails() {
        val a = new GroovySurrogateRegisteredServiceAccessStrategy();
        a.setGroovyScript("classpath:/surrogate-access.groovy");
        val result = a.doPrincipalAttributesAllowServiceAccess("casuser-fail",
            CollectionUtils.wrap(SurrogateAuthenticationService.AUTHENTICATION_ATTR_SURROGATE_ENABLED, true));
        assertFalse(result);
    }

    @Test
    public void verifySurrogateAllowed() {
        val a = new GroovySurrogateRegisteredServiceAccessStrategy();
        a.setGroovyScript("classpath:/surrogate-access.groovy");
        val result = a.doPrincipalAttributesAllowServiceAccess("casuser-enabled",
            CollectionUtils.wrap(SurrogateAuthenticationService.AUTHENTICATION_ATTR_SURROGATE_ENABLED, true));
        assertTrue(result);
    }

    @Test
    public void verifyNoSurrogateSession() {
        val a = new GroovySurrogateRegisteredServiceAccessStrategy();
        a.setGroovyScript("classpath:/surrogate-access.groovy");
        val result = a.doPrincipalAttributesAllowServiceAccess("casuser", Map.of());
        assertTrue(result);
    }
}
