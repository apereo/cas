package org.apereo.cas.oidc.jwks.generator;

import org.apereo.cas.configuration.model.support.oidc.OidcProperties;
import org.apereo.cas.oidc.AbstractOidcTests;
import org.apereo.cas.oidc.jwks.OidcJsonWebKeyStoreUtils;
import org.apereo.cas.oidc.jwks.OidcJsonWebKeyUsage;
import org.apereo.cas.util.MockWebServer;

import lombok.val;
import org.jose4j.jwk.JsonWebKey;
import org.jose4j.jwk.JsonWebKeySet;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;

import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link OidcRestfulJsonWebKeystoreGeneratorServiceTests}.
 *
 * @author Misagh Moayyed
 * @since 6.2.0
 */
@Tag("RestfulApi")
@TestPropertySource(properties = {
    "cas.authn.oidc.jwks.rest.url=http://localhost:9521",
    "cas.authn.oidc.jwks.rest.basic-auth-username=casuser",
    "cas.authn.oidc.jwks.rest.basic-auth-password=123456"
})
class OidcRestfulJsonWebKeystoreGeneratorServiceTests extends AbstractOidcTests {
    private static MockWebServer SERVER;

    @BeforeAll
    public static void setup() {
        val webKey = OidcJsonWebKeyStoreUtils.generateJsonWebKey("rsa", 2048, OidcJsonWebKeyUsage.SIGNING);
        val data = webKey.toJson(JsonWebKey.OutputControlLevel.INCLUDE_PRIVATE);
        SERVER = new MockWebServer(9521,
            new ByteArrayResource(data.getBytes(StandardCharsets.UTF_8), "REST Output"),
            MediaType.APPLICATION_JSON_VALUE);
        SERVER.start();
    }

    @AfterAll
    public static void clean() {
        SERVER.close();
    }

    @Test
    void verifyOperation() throws Throwable {
        val resource = oidcJsonWebKeystoreGeneratorService.generate();
        assertTrue(resource.exists());

        assertTrue(oidcJsonWebKeystoreGeneratorService.find().isPresent());

        val jwks = new JsonWebKeySet(OidcJsonWebKeystoreGeneratorService.generateJsonWebKey(
            casProperties.getAuthn().getOidc(), OidcJsonWebKeyUsage.SIGNING));
        assertNotNull(oidcJsonWebKeystoreGeneratorService.store(jwks));
    }

    @Test
    void verifyFailsOperation() throws Throwable {
        var oidcProperties = new OidcProperties();
        oidcProperties.getJwks().getRest().setUrl("https://localhost:1234");
        oidcProperties.getJwks().getRest().setMethod("get");
        val resource = new OidcRestfulJsonWebKeystoreGeneratorService(oidcProperties).generate();
        assertNull(resource);
    }
}
