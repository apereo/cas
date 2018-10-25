package org.apereo.cas.authentication.surrogate;

import org.apereo.cas.authentication.CoreAuthenticationTestUtils;
import org.apereo.cas.services.ServicesManager;

import org.junit.jupiter.api.Test;
import org.mockito.Mock;

import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link BaseSurrogateAuthenticationServiceTests}.
 *
 * @author Timur Duehr
 * @since 6.0.0
 */
public abstract class BaseSurrogateAuthenticationServiceTests {

    public static final String CASUSER = "casuser";
    public static final String BANDERSON = "banderson";

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
