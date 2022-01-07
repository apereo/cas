package org.apereo.cas.oidc.jwks;

import org.apereo.cas.oidc.AbstractOidcTests;
import org.apereo.cas.oidc.jwks.generator.OidcJsonWebKeystoreModifiedEvent;
import org.apereo.cas.util.spring.CasEventListener;

import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.ConfigurableApplicationContext;

import java.io.File;

import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link OidcDefaultJsonWebKeyStoreListenerTests}.
 *
 * @author Misagh Moayyed
 * @since 6.5.0
 */
@Tag("OIDC")
public class OidcDefaultJsonWebKeyStoreListenerTests extends AbstractOidcTests {
    @Autowired
    @Qualifier("oidcJsonWebKeyStoreListener")
    private CasEventListener oidcJsonWebKeyStoreListener;

    @Autowired
    private ConfigurableApplicationContext realApplicationContext;

    @Test
    public void verifyOperation() throws Exception {
        val cacheKey = new OidcJsonWebKeyCacheKey(
            casProperties.getAuthn().getOidc().getCore().getIssuer(), OidcJsonWebKeyUsage.SIGNING);
        val keys = oidcDefaultJsonWebKeystoreCache.get(cacheKey);
        assertNotNull(keys);
        assertNotNull(oidcJsonWebKeyStoreListener);
        realApplicationContext.publishEvent(new OidcJsonWebKeystoreModifiedEvent(this,
            File.createTempFile("prefix", "postfix")));
        Thread.sleep(2000);
        val newKeys = oidcDefaultJsonWebKeystoreCache.getIfPresent(cacheKey);
        assertNull(newKeys);
    }
}
