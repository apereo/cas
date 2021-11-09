package org.apereo.cas.oidc.jwks;

import org.apereo.cas.oidc.AbstractOidcTests;

import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link OidcRegisteredServiceJsonWebKeystoreCacheLoaderTests}.
 *
 * @author Misagh Moayyed
 * @since 5.3.0
 */
@Tag("OIDC")
public class OidcRegisteredServiceJsonWebKeystoreCacheLoaderTests extends AbstractOidcTests {
    @Test
    public void verifyOperation() {
        val service = getOidcRegisteredService();
        assertTrue(oidcServiceJsonWebKeystoreCache.get(service).isPresent());
        assertTrue(oidcServiceJsonWebKeystoreCache.get(service).isPresent());
    }

    @Test
    public void verifyOperationWithOAuth() {
        val service = getOAuthRegisteredService("clientid", "secret");
        assertTrue(oidcServiceJsonWebKeystoreCache.get(service).isEmpty());
        assertTrue(oidcServiceJsonWebKeystoreCache.get(service).isEmpty());
    }

    @Test
    public void verifyOperationWithKidPerService() {
        val service = getOidcRegisteredService();
        service.setJwksKeyId("myCustomKey");
        assertTrue(oidcServiceJsonWebKeystoreCache.get(service).isEmpty());

        service.setJwksKeyId("1234567890");
        assertTrue(oidcServiceJsonWebKeystoreCache.get(service).isPresent());
    }

}
