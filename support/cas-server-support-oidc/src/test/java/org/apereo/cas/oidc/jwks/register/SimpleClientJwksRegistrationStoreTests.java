package org.apereo.cas.oidc.jwks.register;

import module java.base;
import org.apereo.cas.oidc.AbstractOidcTests;
import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.test.context.TestPropertySource;
import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link SimpleClientJwksRegistrationStoreTests}.
 *
 * @author Misagh Moayyed
 * @since 8.0.0
 */
@Tag("OIDC")
@TestPropertySource(properties = "CasFeatureModule.OpenIDConnect.client-jwks-registration.enabled=true")
class SimpleClientJwksRegistrationStoreTests extends AbstractOidcTests {
    @Autowired
    @Qualifier("clientJwksRegistrationStore")
    private ClientJwksRegistrationStore clientJwksRegistrationStore;

    @Test
    void verifyOperation() {
        val jkt = "test-jkt";
        val jwk = "test-jwk";
        clientJwksRegistrationStore.removeAll();
        clientJwksRegistrationStore.save(jkt, jwk);
        val entry = clientJwksRegistrationStore.findByJkt(jkt);
        assertTrue(entry.isPresent());
        assertEquals(jwk, entry.get().jwk());
        val allEntries = clientJwksRegistrationStore.load();
        assertFalse(allEntries.isEmpty());
        clientJwksRegistrationStore.removeByJkt(jkt);
        val removedEntry = clientJwksRegistrationStore.findByJkt(jkt);
        assertFalse(removedEntry.isPresent());

    }
}
