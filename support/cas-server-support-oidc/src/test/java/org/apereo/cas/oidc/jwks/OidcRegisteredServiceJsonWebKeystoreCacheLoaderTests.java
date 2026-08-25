package org.apereo.cas.oidc.jwks;

import module java.base;
import org.apereo.cas.oidc.AbstractOidcTests;
import org.apereo.cas.util.MockWebServer;
import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link OidcRegisteredServiceJsonWebKeystoreCacheLoaderTests}.
 *
 * @author Misagh Moayyed
 * @since 5.3.0
 */
@Tag("OIDCServices")
class OidcRegisteredServiceJsonWebKeystoreCacheLoaderTests extends AbstractOidcTests {
    @Test
    void verifyOperation() {
        val service = getOidcRegisteredService(UUID.randomUUID().toString());
        assertTrue(oidcServiceJsonWebKeystoreCache.get(new OidcJsonWebKeyCacheKey(service, OidcJsonWebKeyUsage.SIGNING)).isPresent());
        assertTrue(oidcServiceJsonWebKeystoreCache.get(new OidcJsonWebKeyCacheKey(service, OidcJsonWebKeyUsage.SIGNING)).isPresent());
    }

    @Test
    void verifyOperationWithOAuth() {
        val service = getOAuthRegisteredService("clientid", "secret");
        assertTrue(oidcServiceJsonWebKeystoreCache.get(new OidcJsonWebKeyCacheKey(service, OidcJsonWebKeyUsage.SIGNING)).isEmpty());
        assertTrue(oidcServiceJsonWebKeystoreCache.get(new OidcJsonWebKeyCacheKey(service, OidcJsonWebKeyUsage.SIGNING)).isEmpty());
    }

    @Test
    void verifyOperationWithKidPerServiceMissing() {
        val service = getOidcRegisteredService(UUID.randomUUID().toString());
        service.setJwksKeyId("myCustomKey");
        assertTrue(oidcServiceJsonWebKeystoreCache.get(
            new OidcJsonWebKeyCacheKey(service, OidcJsonWebKeyUsage.SIGNING)).isEmpty());
    }

    @Test
    void verifyOperationWithKidPerServicePresent() {
        val service = getOidcRegisteredService(UUID.randomUUID().toString());
        service.setJwksKeyId("1234567890");
        service.setJwks("classpath:servicekid.jwks");
        assertTrue(oidcServiceJsonWebKeystoreCache.get(
            new OidcJsonWebKeyCacheKey(service, OidcJsonWebKeyUsage.SIGNING)).isPresent());
    }

    @Test
    void verifyRemoteJwksCannotReachLoopback() {
        try (val webServer = new MockWebServer(HttpStatus.OK)) {
            webServer.start();
            val service = getOidcRegisteredService(UUID.randomUUID().toString());
            service.setJwks("https://127.0.0.1:%s/jwks.json".formatted(webServer.getPort()));
            assertTrue(oidcServiceJsonWebKeystoreCache.get(
                new OidcJsonWebKeyCacheKey(service, OidcJsonWebKeyUsage.SIGNING)).isEmpty());
            assertEquals(0, webServer.getRequestCount());
        }
    }

}
