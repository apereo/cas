package org.apereo.cas.authentication;

import org.apereo.cas.authentication.surrogate.SimpleSurrogateAuthenticationService;
import org.apereo.cas.authentication.surrogate.SurrogateAuthenticationService;
import org.apereo.cas.services.ServicesManager;
import org.apereo.cas.util.CollectionUtils;
import org.junit.Test;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

/**
 * This is {@link SimpleSurrogateAuthenticationServiceTests}.
 *
 * @author Misagh Moayyed
 * @since 5.3.0
 */
public class SimpleSurrogateAuthenticationServiceTests {
    @Test
    public void verifyList() throws Exception {
        final var mgr = mock(ServicesManager.class);
        final SurrogateAuthenticationService r = new SimpleSurrogateAuthenticationService(
            CollectionUtils.wrap("casuser", CollectionUtils.wrapList("banderson")), mgr);
        assertFalse(r.getEligibleAccountsForSurrogateToProxy("casuser").isEmpty());
    }

    @Test
    public void verifyProxying() {
        final var mgr = mock(ServicesManager.class);
        final SurrogateAuthenticationService r = new SimpleSurrogateAuthenticationService(
            CollectionUtils.wrap("casuser", CollectionUtils.wrapList("banderson")), mgr);
        assertTrue(r.canAuthenticateAs("banderson", CoreAuthenticationTestUtils.getPrincipal("casuser"),
            CoreAuthenticationTestUtils.getService()));
    }
}
