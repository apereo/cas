package org.apereo.cas.authentication;

import org.apereo.cas.authentication.surrogate.JsonResourceSurrogateAuthenticationService;
import org.apereo.cas.services.ServicesManager;

import lombok.val;
import org.junit.Test;
import org.springframework.core.io.ClassPathResource;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

/**
 * This is {@link JsonResourceSurrogateAuthenticationServiceTests}.
 *
 * @author Misagh Moayyed
 * @since 5.3.0
 */
public class JsonResourceSurrogateAuthenticationServiceTests {
    @Test
    public void verifyList() throws Exception {
        val resource = new ClassPathResource("surrogates.json");
        val mgr = mock(ServicesManager.class);
        val r = new JsonResourceSurrogateAuthenticationService(resource, mgr);
        assertFalse(r.getEligibleAccountsForSurrogateToProxy("casuser").isEmpty());
    }

    @Test
    public void verifyProxying() throws Exception {
        val resource = new ClassPathResource("surrogates.json");
        val mgr = mock(ServicesManager.class);
        val r = new JsonResourceSurrogateAuthenticationService(resource, mgr);
        assertTrue(r.canAuthenticateAs("banderson", CoreAuthenticationTestUtils.getPrincipal("casuser"),
            CoreAuthenticationTestUtils.getService()));
    }
}
