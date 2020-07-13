package org.apereo.cas.oidc.jwks;

import org.apereo.cas.oidc.AbstractOidcTests;
import org.apereo.cas.util.ResourceUtils;

import lombok.val;
import org.apache.commons.io.FileUtils;
import org.jose4j.jwk.JsonWebKeySet;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.FileSystemResource;
import org.springframework.test.context.TestPropertySource;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * This is {@link OidcDefaultJsonWebKeystoreCacheLoaderTests}.
 *
 * @author Misagh Moayyed
 * @since 5.3.0
 */
@Tag("OIDC")
@TestPropertySource(properties = {
    "cas.authn.oidc.jwks.jwks-type=ec",
    "cas.authn.oidc.jwks.jwks-key-size=384",
    "cas.authn.oidc.jwks.jwks-file=file:${#systemProperties['java.io.tmpdir']}/keystore.jwks"
})
public class OidcDefaultJsonWebKeystoreCacheLoaderTests extends AbstractOidcTests {
    @BeforeAll
    public static void setup() {
        val file = new File(System.getProperty("java.io.tmpdir"), "keystore.jwks");
        file.delete();
    }

    @Test
    public void verifyOperation() {
        val publicJsonWebKey1 = oidcDefaultJsonWebKeystoreCache.get("https://sso.example.org/cas/oidc");
        assertNotNull(publicJsonWebKey1);
        assertTrue(publicJsonWebKey1.isPresent());

        val publicJsonWebKey2 = oidcDefaultJsonWebKeystoreCache.get("https://sso.example.org/cas/oidc");
        assertNotNull(publicJsonWebKey2);
        assertTrue(publicJsonWebKey2.isPresent());
    }

    @Test
    public void verifyNullResource() {
        val gen = mock(OidcJsonWebKeystoreGeneratorService.class);
        when(gen.generate()).thenReturn(null);
        val loader = new OidcDefaultJsonWebKeystoreCacheLoader(gen);
        assertTrue(loader.load("https://cas.example.org").isEmpty());
    }

    @Test
    public void verifyEmptyFile() throws Exception {
        val gen = mock(OidcJsonWebKeystoreGeneratorService.class);
        when(gen.generate()).thenReturn(ResourceUtils.EMPTY_RESOURCE);
        val loader = new OidcDefaultJsonWebKeystoreCacheLoader(gen);
        assertTrue(loader.load("https://cas.example.org").isEmpty());

        val file = File.createTempFile("keys", ".json");
        FileUtils.writeStringToFile(file, new JsonWebKeySet(List.of()).toJson(), StandardCharsets.UTF_8);
        when(gen.generate()).thenReturn(new FileSystemResource(file));
        assertTrue(loader.load("https://cas.example.org").isEmpty());
    }

    @Test
    public void verifyBadKeys() {
        val gen = mock(OidcJsonWebKeystoreGeneratorService.class);
        val keys = "{ \"keys\": [ {\"kty\":\"EC\","
            + "\"x\":\"sPlKwAgSxxOE\",\"y\":\"6AyisnUKM"
            + "9H8\",\"crv\":\"P-256\"} ]}";
        when(gen.generate()).thenReturn(new ByteArrayResource(keys.getBytes(StandardCharsets.UTF_8)));
        val loader = new OidcDefaultJsonWebKeystoreCacheLoader(gen);
        assertTrue(loader.load("https://cas.example.org").isEmpty());
    }
}
