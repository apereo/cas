package org.apereo.cas.oidc.token;

import org.apereo.cas.oidc.AbstractOidcTests;
import org.apereo.cas.oidc.issuer.OidcIssuerService;
import org.apereo.cas.services.DefaultRegisteredServiceProperty;
import org.apereo.cas.services.RegisteredServiceProperty;
import org.apereo.cas.util.cipher.BaseStringCipherExecutor;

import com.github.benmanes.caffeine.cache.LoadingCache;
import lombok.val;
import org.jose4j.jwk.PublicJsonWebKey;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * This is {@link OidcRegisteredServiceJwtAccessTokenCipherExecutorTests}.
 *
 * @author Misagh Moayyed
 * @since 6.1.0
 */
@Tag("OIDC")
public class OidcRegisteredServiceJwtAccessTokenCipherExecutorNoCacheTests extends AbstractOidcTests {

    @Test
    public void verifyEmptyCacheOperation() {
        val id = UUID.randomUUID().toString();

        val defaultCache = mock(LoadingCache.class);
        when(defaultCache.get(any())).thenReturn(Optional.empty());

        val serviceCache = mock(LoadingCache.class);
        when(serviceCache.get(any())).thenReturn(Optional.empty());

        val cipher = new OidcRegisteredServiceJwtAccessTokenCipherExecutor(defaultCache, serviceCache, OidcIssuerService.immutable(id));

        val service = getOidcRegisteredService("whatever");
        assertTrue(cipher.getSigningKey(service).isEmpty());

        service.getProperties().put(
            RegisteredServiceProperty.RegisteredServiceProperties.ACCESS_TOKEN_AS_JWT_ENCRYPTION_ENABLED.getPropertyName(),
            new DefaultRegisteredServiceProperty("true"));
        assertTrue(cipher.getEncryptionKey(service).isEmpty());

        val key = mock(PublicJsonWebKey.class);
        when(serviceCache.get(any())).thenReturn(Optional.of(key));
        assertTrue(cipher.getEncryptionKey(service).isEmpty());
    }

    @Test
    public void verifyCipherOperation() {
        val id = UUID.randomUUID().toString();

        val defaultCache = mock(LoadingCache.class);
        when(defaultCache.get(any())).thenReturn(Optional.empty());

        val serviceCache = mock(LoadingCache.class);
        when(serviceCache.get(any())).thenReturn(Optional.empty());

        val cipher = new OidcRegisteredServiceJwtAccessTokenCipherExecutor(defaultCache, serviceCache, OidcIssuerService.immutable(id));

        val service = getOidcRegisteredService("whatever");

        val exec = cipher.createCipherExecutorInstance(null, null, service,
            BaseStringCipherExecutor.CipherOperationsStrategyType.ENCRYPT_AND_SIGN);
        assertEquals("value", exec.decode("value", new Object[]{service}));

        val key = mock(PublicJsonWebKey.class);
        when(serviceCache.get(any())).thenReturn(Optional.of(key));
        assertEquals("value", exec.decode("value", new Object[]{service}));
    }
}
