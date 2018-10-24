package org.apereo.cas.services;

import org.apereo.cas.authentication.surrogate.SurrogateAuthenticationService;
import org.apereo.cas.util.CollectionUtils;

import lombok.val;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.jupiter.api.Test;
import org.springframework.test.context.junit4.rules.SpringClassRule;
import org.springframework.test.context.junit4.rules.SpringMethodRule;

import static org.junit.Assert.*;

/**
 * This is {@link GroovySurrogateRegisteredServiceAccessStrategyTests}.
 *
 * @author Misagh Moayyed
 * @since 5.3.0
 */
public class GroovySurrogateRegisteredServiceAccessStrategyTests {
    @ClassRule
    public static final SpringClassRule SPRING_CLASS_RULE = new SpringClassRule();

    @Rule
    public final SpringMethodRule springMethodRule = new SpringMethodRule();

    @Test
    public void verifySurrogateDisabled() {
        val a = new GroovySurrogateRegisteredServiceAccessStrategy();
        a.setGroovyScript("classpath:/surrogate-access.groovy");
        val result = a.doPrincipalAttributesAllowServiceAccess("casuser-disabled",
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
}
