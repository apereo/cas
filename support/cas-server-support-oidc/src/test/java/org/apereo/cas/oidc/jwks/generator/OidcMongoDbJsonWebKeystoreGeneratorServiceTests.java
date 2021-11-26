package org.apereo.cas.oidc.jwks.generator;

import org.apereo.cas.oidc.AbstractOidcTests;
import org.apereo.cas.util.junit.EnabledIfPortOpen;

import lombok.val;
import org.apache.commons.io.IOUtils;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.test.context.TestPropertySource;

import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link OidcMongoDbJsonWebKeystoreGeneratorServiceTests}.
 *
 * @author Misagh Moayyed
 * @since 6.5.0
 */
@Tag("MongoDb")
@TestPropertySource(properties = {
    "cas.authn.oidc.jwks.mongo.database-name=oidc",
    "cas.authn.oidc.jwks.mongo.collection=oidc_jwks",
    "cas.authn.oidc.jwks.mongo.host=localhost",
    "cas.authn.oidc.jwks.mongo.port=27017",
    "cas.authn.oidc.jwks.mongo.user-id=root",
    "cas.authn.oidc.jwks.mongo.password=secret",
    "cas.authn.oidc.jwks.mongo.authentication-database-name=admin",
    "cas.authn.oidc.jwks.mongo.drop-collection=true"
})
@EnabledIfPortOpen(port = 27017)
public class OidcMongoDbJsonWebKeystoreGeneratorServiceTests extends AbstractOidcTests {
    @Test
    public void verifyOperation() throws Exception {
        val resource1 = oidcJsonWebKeystoreGeneratorService.generate();
        val jwks1 = IOUtils.toString(resource1.getInputStream(), StandardCharsets.UTF_8);

        val resource2 = oidcJsonWebKeystoreGeneratorService.generate();
        val jwks2 = IOUtils.toString(resource2.getInputStream(), StandardCharsets.UTF_8);

        assertEquals(jwks1, jwks2);

        val set1 = oidcJsonWebKeystoreGeneratorService.store(OidcJsonWebKeystoreGeneratorService.toJsonWebKeyStore(resource1));
        assertNotNull(set1);

        assertTrue(oidcJsonWebKeystoreGeneratorService.find().isPresent());
    }
}
