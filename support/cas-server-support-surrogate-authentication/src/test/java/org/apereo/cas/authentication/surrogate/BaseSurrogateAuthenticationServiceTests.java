package org.apereo.cas.authentication.surrogate;

import org.apereo.cas.authentication.CoreAuthenticationTestUtils;
import org.apereo.cas.services.ServicesManager;

import lombok.val;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;

import java.util.Optional;

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
        val service = Optional.of(CoreAuthenticationTestUtils.getService());
        assertTrue(getService().canAuthenticateAs(BANDERSON, CoreAuthenticationTestUtils.getPrincipal(CASUSER), service));
        assertFalse(getService().canAuthenticateAs("XXXX", CoreAuthenticationTestUtils.getPrincipal(CASUSER), service));
        assertFalse(getService().canAuthenticateAs(CASUSER, CoreAuthenticationTestUtils.getPrincipal(BANDERSON), service));
    }
}
