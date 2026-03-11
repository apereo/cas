package org.apereo.cas.oidc.jwks.register;

import module java.base;
import org.apereo.cas.config.CasOidcJwksMongoDbAutoConfiguration;
import org.apereo.cas.oidc.AbstractOidcTests;
import org.apereo.cas.util.junit.EnabledIfListeningOnPort;
import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.test.context.TestPropertySource;
import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link MongoDbClientJwksRegistrationStoreTests}.
 *
 * @author Misagh Moayyed
 * @since 8.0.0
 */
@Tag("MongoDb")
@TestPropertySource(properties = {
    "CasFeatureModule.OpenIDConnect.client-jwks-registration.enabled=true",
    "CasFeatureModule.OpenIDConnect.jwks-mongodb.enabled=false",

    "cas.authn.oidc.jwks.mongo.database-name=oidc",
    "cas.authn.oidc.jwks.mongo.host=localhost",
    "cas.authn.oidc.jwks.mongo.port=27017",
    "cas.authn.oidc.jwks.mongo.user-id=root",
    "cas.authn.oidc.jwks.mongo.password=secret",
    "cas.authn.oidc.jwks.mongo.authentication-database-name=admin",
    "cas.authn.oidc.jwks.mongo.drop-collection=true"
})
@EnabledIfListeningOnPort(port = 27017)
@ImportAutoConfiguration(CasOidcJwksMongoDbAutoConfiguration.class)
class MongoDbClientJwksRegistrationStoreTests extends AbstractOidcTests {
    @Autowired
    @Qualifier("clientJwksRegistrationStore")
    private ClientJwksRegistrationStore clientJwksRegistrationStore;

    @Test
    void verifyOperation() {
        val jkt = UUID.randomUUID().toString();
        val jwk = UUID.randomUUID().toString();
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
