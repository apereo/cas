package org.apereo.cas.oidc.jwks;

import org.apereo.cas.oidc.AbstractOidcTests;

import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link OidcRegisteredServiceJsonWebKeystoreCacheLoaderTests}.
 *
 * @author Misagh Moayyed
 * @since 5.3.0
 */
@Tag("OIDC")
class OidcRegisteredServiceJsonWebKeystoreCacheLoaderTests extends AbstractOidcTests {
    @Test
    void verifyOperation() throws Throwable {
        val service = getOidcRegisteredService(UUID.randomUUID().toString());
        assertTrue(oidcServiceJsonWebKeystoreCache.get(new OidcJsonWebKeyCacheKey(service, OidcJsonWebKeyUsage.SIGNING)).isPresent());
        assertTrue(oidcServiceJsonWebKeystoreCache.get(new OidcJsonWebKeyCacheKey(service, OidcJsonWebKeyUsage.SIGNING)).isPresent());
    }

    @Test
    void verifyOperationWithOAuth() throws Throwable {
        val service = getOAuthRegisteredService("clientid", "secret");
        assertTrue(oidcServiceJsonWebKeystoreCache.get(new OidcJsonWebKeyCacheKey(service, OidcJsonWebKeyUsage.SIGNING)).isEmpty());
        assertTrue(oidcServiceJsonWebKeystoreCache.get(new OidcJsonWebKeyCacheKey(service, OidcJsonWebKeyUsage.SIGNING)).isEmpty());
    }

    @Test
    void verifyOperationWithKidPerServiceMissing() throws Throwable {
        val service = getOidcRegisteredService(UUID.randomUUID().toString());
        service.setJwksKeyId("myCustomKey");
        assertTrue(oidcServiceJsonWebKeystoreCache.get(
            new OidcJsonWebKeyCacheKey(service, OidcJsonWebKeyUsage.SIGNING)).isEmpty());
    }

    @Test
    void verifyOperationWithKidPerServicePresent() throws Throwable {
        val service = getOidcRegisteredService(UUID.randomUUID().toString());
        service.setJwksKeyId("1234567890");
        service.setJwks("classpath:servicekid.jwks");
        assertTrue(oidcServiceJsonWebKeystoreCache.get(
            new OidcJsonWebKeyCacheKey(service, OidcJsonWebKeyUsage.SIGNING)).isPresent());
    }

}
