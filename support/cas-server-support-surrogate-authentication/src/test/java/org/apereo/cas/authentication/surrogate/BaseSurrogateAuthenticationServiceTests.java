package org.apereo.cas.authentication.surrogate;

import org.apereo.cas.authentication.CoreAuthenticationTestUtils;
import org.apereo.cas.services.ServicesManager;
import org.apereo.cas.util.junit.ConditionalIgnoreRule;

import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.springframework.test.context.junit4.rules.SpringClassRule;
import org.springframework.test.context.junit4.rules.SpringMethodRule;

import static org.junit.Assert.*;

/**
 * This is {@link BaseSurrogateAuthenticationServiceTests}.
 *
 * @author Timur Duehr
 * @since 6.0.0
 */
public abstract class BaseSurrogateAuthenticationServiceTests {

    @ClassRule
    public static final SpringClassRule SPRING_CLASS_RULE = new SpringClassRule();
    public static final String CASUSER = "casuser";
    public static final String BANDERSON = "banderson";

    @Rule
    public final SpringMethodRule springMethodRule = new SpringMethodRule();

    @Rule
    public final ConditionalIgnoreRule conditionalIgnoreRule = new ConditionalIgnoreRule();

    @Mock
    protected ServicesManager servicesManager;

    public abstract SurrogateAuthenticationService getService();

    @Test
    public void verifyList() {
        assertFalse(getService().getEligibleAccountsForSurrogateToProxy(CASUSER).isEmpty());
    }

    @Test
    public void verifyProxying() {
        assertTrue(getService().canAuthenticateAs(BANDERSON, CoreAuthenticationTestUtils.getPrincipal(CASUSER),
            CoreAuthenticationTestUtils.getService()));
        assertFalse(getService().canAuthenticateAs("XXXX", CoreAuthenticationTestUtils.getPrincipal(CASUSER),
            CoreAuthenticationTestUtils.getService()));
        assertFalse(getService().canAuthenticateAs(CASUSER, CoreAuthenticationTestUtils.getPrincipal(BANDERSON),
            CoreAuthenticationTestUtils.getService()));
    }
}
