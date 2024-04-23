package org.apereo.cas.oidc.jwks.generator;

import org.apereo.cas.oidc.AbstractOidcTests;
import org.apereo.cas.oidc.jwks.OidcJsonWebKeyUsage;

import lombok.val;
import org.apache.commons.io.IOUtils;
import org.jose4j.jwk.JsonWebKeySet;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.test.context.TestPropertySource;

import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link OidcGroovyJsonWebKeystoreGeneratorServiceTests}.
 *
 * @author Misagh Moayyed
 * @since 6.2.0
 */
@Tag("Groovy")
@TestPropertySource(properties = "cas.authn.oidc.jwks.groovy.location=classpath:GroovyJwksService.groovy")
class OidcGroovyJsonWebKeystoreGeneratorServiceTests extends AbstractOidcTests {
    @Test
    void verifyOperation() throws Throwable {
        val resource = oidcJsonWebKeystoreGeneratorService.generate();
        assertTrue(resource.exists());
        assertTrue(oidcJsonWebKeystoreGeneratorService.find().isPresent());
        assertDoesNotThrow(() -> {
            val results = new String(IOUtils.toByteArray(resource.getInputStream()), StandardCharsets.UTF_8);
            new JsonWebKeySet(results);
        });
    }

    @Test
    void verifyStoreOperation() throws Throwable {
        val jwks = new JsonWebKeySet(OidcJsonWebKeystoreGeneratorService.generateJsonWebKey(
            casProperties.getAuthn().getOidc(), OidcJsonWebKeyUsage.ENCRYPTION));
        assertNotNull(oidcJsonWebKeystoreGeneratorService.store(jwks));
    }
}
