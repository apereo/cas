package org.apereo.cas.oidc.token;

import module java.base;
import org.apereo.cas.oidc.AbstractOidcTests;
import org.apereo.cas.oidc.issuer.OidcIssuerService;
import org.apereo.cas.services.DefaultRegisteredServiceProperty;
import org.apereo.cas.services.RegisteredServiceProperty;
import com.github.benmanes.caffeine.cache.LoadingCache;
import lombok.val;
import org.jose4j.jwk.JsonWebKeySet;
import org.jose4j.jwk.PublicJsonWebKey;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * This is {@link OidcRegisteredServiceJwtAccessTokenCipherExecutorTests}.
 *
 * @author Misagh Moayyed
 * @since 6.1.0
 */
@Tag("OIDCServices")
class OidcRegisteredServiceJwtAccessTokenCipherExecutorNoCacheTests extends AbstractOidcTests {

    @Test
    void verifyEmptyCacheOperation() {
        val id = UUID.randomUUID().toString();

        val defaultCache = mock(LoadingCache.class);
        when(defaultCache.get(any())).thenReturn(null);

        val serviceCache = mock(LoadingCache.class);
        when(serviceCache.get(any())).thenReturn(Optional.empty());

        val cipher = new OidcRegisteredServiceJwtAccessTokenCipherExecutor(defaultCache,
            serviceCache, OidcIssuerService.echoing(id));

        val service = getOidcRegisteredService("whatever");
        assertTrue(cipher.getSigningKey(service).isEmpty());

        service.getProperties().put(
            RegisteredServiceProperty.RegisteredServiceProperties.ACCESS_TOKEN_AS_JWT_ENCRYPTION_ENABLED.getPropertyName(),
            new DefaultRegisteredServiceProperty("true"));
        assertTrue(cipher.getEncryptionKey(service).isEmpty());

        when(serviceCache.get(any())).thenReturn(Optional.of(new JsonWebKeySet(mock(PublicJsonWebKey.class))));
        assertTrue(cipher.getEncryptionKey(service).isEmpty());
    }

    @Test
    void verifyCipherOperation() {
        val id = UUID.randomUUID().toString();

        val defaultCache = mock(LoadingCache.class);
        when(defaultCache.get(any())).thenReturn(null);

        val serviceCache = mock(LoadingCache.class);
        when(serviceCache.get(any())).thenReturn(Optional.empty());

        val cipher = new OidcRegisteredServiceJwtAccessTokenCipherExecutor(defaultCache, serviceCache, OidcIssuerService.echoing(id));

        val service = getOidcRegisteredService("whatever");

        val exec = cipher.createCipherExecutorInstance(null, null, service);
        assertEquals("value", exec.decode("value", new Object[]{service}));

        when(serviceCache.get(any())).thenReturn(Optional.of(new JsonWebKeySet(mock(PublicJsonWebKey.class))));
        assertEquals("value", exec.decode("value", new Object[]{service}));
    }
}
