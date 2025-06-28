package org.apereo.cas.oidc.jwks;

import org.apereo.cas.oidc.AbstractOidcTests;
import org.apereo.cas.oidc.jwks.generator.OidcJsonWebKeystoreModifiedEvent;
import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ConfigurableApplicationContext;
import java.nio.file.Files;
import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link OidcDefaultJsonWebKeyStoreListenerTests}.
 *
 * @author Misagh Moayyed
 * @since 6.5.0
 */
@Tag("OIDC")
class OidcDefaultJsonWebKeyStoreListenerTests extends AbstractOidcTests {
    @Autowired
    private ConfigurableApplicationContext realApplicationContext;

    @Test
    void verifyOperation() throws Throwable {
        val cacheKey = new OidcJsonWebKeyCacheKey(casProperties.getAuthn().getOidc().getCore().getIssuer(), OidcJsonWebKeyUsage.SIGNING);
        val keys = oidcDefaultJsonWebKeystoreCache.get(cacheKey);
        assertNotNull(keys);
        assertNotNull(oidcJsonWebKeyStoreListener);
        realApplicationContext.publishEvent(new OidcJsonWebKeystoreModifiedEvent(this,
            Files.createTempFile("prefix", "postfix").toFile(), null));
        Thread.sleep(2000);
        val newKeys = oidcDefaultJsonWebKeystoreCache.getIfPresent(cacheKey);
        assertNull(newKeys);
    }
}
