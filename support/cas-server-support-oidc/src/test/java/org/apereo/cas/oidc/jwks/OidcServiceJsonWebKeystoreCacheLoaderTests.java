package org.apereo.cas.oidc.jwks;

import org.apereo.cas.oidc.AbstractOidcTests;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 * This is {@link OidcServiceJsonWebKeystoreCacheLoaderTests}.
 *
 * @author Misagh Moayyed
 * @since 5.3.0
 */
public class OidcServiceJsonWebKeystoreCacheLoaderTests extends AbstractOidcTests {
    @Test
    public void verifyOperation() {
        final var service = getOidcRegisteredService();
        assertTrue(oidcServiceJsonWebKeystoreCache.get(service).isPresent());
        assertTrue(oidcServiceJsonWebKeystoreCache.get(service).isPresent());
    }
}
