package org.apereo.cas.oidc.jwks;

import module java.base;
import org.apereo.cas.oidc.jwks.generator.OidcJsonWebKeystoreModifiedEvent;
import com.github.benmanes.caffeine.cache.Caffeine;
import lombok.val;
import org.jose4j.jwk.JsonWebKeySet;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * This is {@link OidcDefaultJsonWebKeyStoreListenerTests}.
 *
 * @author Misagh Moayyed
 * @since 6.5.0
 */
@Tag("OIDC")
class OidcDefaultJsonWebKeyStoreListenerTests {
    @Test
    void verifyOperation() {
        val cache = Caffeine.newBuilder().<OidcJsonWebKeyCacheKey, JsonWebKeySet>build(_ -> mock(JsonWebKeySet.class));
        val cacheKey = new OidcJsonWebKeyCacheKey(UUID.randomUUID().toString(), OidcJsonWebKeyUsage.SIGNING);
        assertNotNull(cache.get(cacheKey));

        val listener = new OidcDefaultJsonWebKeyStoreListener(cache);
        listener.handleOidcJsonWebKeystoreModifiedEvent(mock(OidcJsonWebKeystoreModifiedEvent.class));
        assertNull(cache.getIfPresent(cacheKey));
    }
}
