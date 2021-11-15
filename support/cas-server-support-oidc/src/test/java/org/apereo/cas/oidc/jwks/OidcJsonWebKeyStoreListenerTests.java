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
 * This is {@link OidcJsonWebKeyStoreListenerTests}.
 *
 * @author Misagh Moayyed
 * @since 6.5.0
 */
@Tag("OIDC")
public class OidcJsonWebKeyStoreListenerTests extends AbstractOidcTests {
    @Autowired
    @Qualifier("oidcJsonWebKeyStoreListener")
    private CasEventListener oidcJsonWebKeyStoreListener;

    @Autowired
    private ConfigurableApplicationContext realApplicationContext;

    @Test
    public void verifyOperation() throws Exception {
        val keys = oidcDefaultJsonWebKeystoreCache.get(casProperties.getAuthn().getOidc().getCore().getIssuer());
        assertNotNull(keys);
        assertEquals(oidcDefaultJsonWebKeystoreCache.estimatedSize(), 1);
        assertNotNull(oidcJsonWebKeyStoreListener);
        realApplicationContext.publishEvent(new OidcJsonWebKeystoreModifiedEvent(this,
            File.createTempFile("prefix", "postfix")));
        Thread.sleep(2000);
        val newKeys = oidcDefaultJsonWebKeystoreCache.getIfPresent(casProperties.getAuthn().getOidc().getCore().getIssuer());
        assertNull(newKeys);
    }
}
