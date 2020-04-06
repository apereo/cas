package org.apereo.cas.oidc.jwks.generator;

import org.apereo.cas.oidc.AbstractOidcTests;
import org.apereo.cas.oidc.jwks.OidcJsonWebKeyStoreUtils;
import org.apereo.cas.util.MockWebServer;

import lombok.val;
import org.jose4j.jwk.JsonWebKey;
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
    "cas.authn.oidc.jwks.rest.basicAuthUsername=casuser",
    "cas.authn.oidc.jwks.rest.basicAuthPassword=123456"
})
public class OidcRestfulJsonWebKeystoreGeneratorServiceTests extends AbstractOidcTests {
    private static MockWebServer SERVER;

    @BeforeAll
    public static void setup() {
        val webkey = OidcJsonWebKeyStoreUtils.generateJsonWebKey("rsa", 2048);
        val data = webkey.toJson(JsonWebKey.OutputControlLevel.INCLUDE_PRIVATE);

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
    public void verifyOperation() {
        val resource = oidcJsonWebKeystoreGeneratorService.generate();
        assertTrue(resource.exists());
    }
}
