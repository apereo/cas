package org.apereo.cas.oidc.jwks;

import org.apereo.cas.oidc.AbstractOidcTests;
import org.apereo.cas.oidc.jwks.generator.OidcJsonWebKeystoreGeneratorService;
import org.apereo.cas.util.ResourceUtils;
import lombok.val;
import org.apache.commons.io.FileUtils;
import org.jose4j.jwk.JsonWebKey;
import org.jose4j.jwk.JsonWebKeySet;
import org.jose4j.jwk.PublicJsonWebKey;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.test.context.TestPropertySource;
import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
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
    "cas.authn.oidc.jwks.core.jwks-type=ec",
    "cas.authn.oidc.jwks.core.jwks-key-size=384",
    "cas.authn.oidc.jwks.file-system.jwks-file=file:${#systemProperties['java.io.tmpdir']}/keystore.jwks"
})
class OidcDefaultJsonWebKeystoreCacheLoaderTests extends AbstractOidcTests {
    @BeforeAll
    public static void setup() throws Throwable {
        val file = new File(FileUtils.getTempDirectory(), "keystore.jwks");
        if (file.exists()) {
            FileUtils.delete(file);
        }
    }

    @Test
    void verifyNoWebKeys() {
        val jwks = new JsonWebKeySet();
        val loader = mock(OidcDefaultJsonWebKeystoreCacheLoader.class);
        when(loader.buildJsonWebKeySet(any(OidcJsonWebKeyCacheKey.class))).thenReturn(Optional.of(jwks));
        when(loader.load(any(OidcJsonWebKeyCacheKey.class))).thenCallRealMethod();
        assertNull(loader.load(new OidcJsonWebKeyCacheKey(UUID.randomUUID().toString(), OidcJsonWebKeyUsage.SIGNING)));

        jwks.getJsonWebKeys().add(mock(JsonWebKey.class));
        assertNull(loader.load(new OidcJsonWebKeyCacheKey(UUID.randomUUID().toString(), OidcJsonWebKeyUsage.SIGNING)));
    }

    @Test
    void verifyBadKeyCount() throws Throwable {
        val jwks = new JsonWebKeySet();
        val jsonWebKey = mock(PublicJsonWebKey.class);
        jwks.getJsonWebKeys().add(jsonWebKey);

        val loader = mock(OidcDefaultJsonWebKeystoreCacheLoader.class);
        when(loader.buildJsonWebKeySet(any(OidcJsonWebKeyCacheKey.class))).thenCallRealMethod();
        when(loader.load(any(OidcJsonWebKeyCacheKey.class))).thenCallRealMethod();
        when(loader.generateJwksResource()).thenReturn(new ByteArrayResource("jwks".getBytes(StandardCharsets.UTF_8)));
        when(loader.buildJsonWebKeySet(any(Resource.class), any(OidcJsonWebKeyCacheKey.class))).thenReturn(jwks);
        assertNull(loader.load(new OidcJsonWebKeyCacheKey(UUID.randomUUID().toString(), OidcJsonWebKeyUsage.SIGNING)));

        when(jsonWebKey.getAlgorithm()).thenReturn("some-alg");
        assertNull(loader.load(new OidcJsonWebKeyCacheKey(UUID.randomUUID().toString(), OidcJsonWebKeyUsage.SIGNING)));
    }

    @Test
    void verifyOperation() {
        val publicJsonWebKey1 = oidcDefaultJsonWebKeystoreCache.get(
            new OidcJsonWebKeyCacheKey("https://sso.example.org/cas/oidc", OidcJsonWebKeyUsage.SIGNING));
        assertNotNull(publicJsonWebKey1);

        val publicJsonWebKey2 = oidcDefaultJsonWebKeystoreCache.get(
            new OidcJsonWebKeyCacheKey("https://sso.example.org/cas/oidc", OidcJsonWebKeyUsage.SIGNING));
        assertNotNull(publicJsonWebKey2);
    }

    @Test
    void verifyNullResource() throws Throwable {
        val gen = mock(OidcJsonWebKeystoreGeneratorService.class);
        when(gen.generate()).thenReturn(null);
        val loader = new OidcDefaultJsonWebKeystoreCacheLoader(gen);
        assertNull(loader.load(new OidcJsonWebKeyCacheKey("https://cas.example.org", OidcJsonWebKeyUsage.SIGNING)));
    }

    @Test
    void verifyEmptyFile() throws Throwable {
        val gen = mock(OidcJsonWebKeystoreGeneratorService.class);
        when(gen.generate()).thenReturn(ResourceUtils.EMPTY_RESOURCE);
        val loader = new OidcDefaultJsonWebKeystoreCacheLoader(gen);
        assertNull(loader.load(new OidcJsonWebKeyCacheKey("https://cas.example.org", OidcJsonWebKeyUsage.SIGNING)));

        val file = Files.createTempFile("keys", ".json").toFile();
        FileUtils.writeStringToFile(file, new JsonWebKeySet(List.of()).toJson(), StandardCharsets.UTF_8);
        when(gen.generate()).thenReturn(new FileSystemResource(file));
        assertNull(loader.load(new OidcJsonWebKeyCacheKey("https://cas.example.org", OidcJsonWebKeyUsage.SIGNING)));
    }

    @Test
    void verifyBadKeys() throws Throwable {
        val gen = mock(OidcJsonWebKeystoreGeneratorService.class);
        val keys = "{ \"keys\": [ {\"kty\":\"EC\","
                   + "\"x\":\"sPlKwAgSxxOE\",\"y\":\"6AyisnUKM"
                   + "9H8\",\"crv\":\"P-256\"} ]}";
        when(gen.generate()).thenReturn(new ByteArrayResource(keys.getBytes(StandardCharsets.UTF_8)));
        val loader = new OidcDefaultJsonWebKeystoreCacheLoader(gen);
        assertNull(loader.load(new OidcJsonWebKeyCacheKey("https://cas.example.org", OidcJsonWebKeyUsage.SIGNING)));
    }
}
