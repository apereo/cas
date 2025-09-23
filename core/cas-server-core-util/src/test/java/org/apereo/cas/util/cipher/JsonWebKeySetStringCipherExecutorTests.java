package org.apereo.cas.util.cipher;

import org.apereo.cas.util.MockWebServer;
import lombok.val;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.jose4j.jwk.JsonWebKey;
import org.jose4j.jwk.JsonWebKeySet;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.core.io.ClassPathResource;
import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.attribute.FileTime;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;
import static org.apache.commons.lang3.ArrayUtils.*;
import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link JsonWebKeySetStringCipherExecutorTests}.
 *
 * @author Misagh Moayyed
 * @since 5.3.0
 */
@Tag("Cipher")
class JsonWebKeySetStringCipherExecutorTests {
    private static File getKeystoreFile() throws Exception {
        val jwksKeystore = new ClassPathResource("sample.jwks");
        val data = IOUtils.toString(jwksKeystore.getInputStream(), StandardCharsets.UTF_8);
        val keystoreFile = new File(FileUtils.getTempDirectoryPath(), "sample.jwks");
        FileUtils.write(keystoreFile, data, StandardCharsets.UTF_8);
        return keystoreFile;
    }

    @Test
    void verifyAction() throws Throwable {
        val jwksKeystore = new ClassPathResource("sample.jwks");
        val data = IOUtils.toString(jwksKeystore.getInputStream(), StandardCharsets.UTF_8);
        val keystoreFile = getKeystoreFile();
        try (val webServer = new MockWebServer(data);
             val cipher = new JsonWebKeySetStringCipherExecutor(keystoreFile, "http://localhost:" + webServer.getPort())) {
            webServer.start();
            val token = cipher.encode("Misagh");
            assertEquals("Misagh", cipher.decode(token));
            Files.setLastModifiedTime(keystoreFile.toPath(), FileTime.from(Instant.now()));
            Thread.sleep(5_000);
            cipher.destroy();
        }
    }


    @Test
    void verifyEmptyFileForEncoding() throws Throwable {
        val keystoreFile = Files.createTempFile("keystore", ".json").toFile();
        FileUtils.write(keystoreFile, "{ \"keys\": [] }", StandardCharsets.UTF_8);
        try (val cipher = new JsonWebKeySetStringCipherExecutor(keystoreFile)) {
            assertThrows(IllegalArgumentException.class, () -> cipher.encode("value", EMPTY_OBJECT_ARRAY));
        }
    }

    @Test
    void verifyEncodingWithPublicKeyOnly() throws Throwable {
        val jwksKeystore = new ClassPathResource("sample.jwks");
        val data = IOUtils.toString(jwksKeystore.getInputStream(), StandardCharsets.UTF_8);
        val json = new JsonWebKeySet(data).toJson(JsonWebKey.OutputControlLevel.PUBLIC_ONLY);
        val keystoreFile = Files.createTempFile("keystorepub", ".json").toFile();
        FileUtils.write(keystoreFile, json, StandardCharsets.UTF_8);
        try (val cipher = new JsonWebKeySetStringCipherExecutor(keystoreFile, Optional.of("cas"))) {
            assertThrows(IllegalArgumentException.class, () -> cipher.encode("value", EMPTY_OBJECT_ARRAY));
        }
    }

    @Test
    void verifyEmptyFileForDecoding() throws Throwable {
        val keystoreFile = Files.createTempFile("keystore", ".json").toFile();
        FileUtils.write(keystoreFile, "{ \"keys\": [] }", StandardCharsets.UTF_8);
        try (val cipher = new JsonWebKeySetStringCipherExecutor(keystoreFile, Optional.of("kid"))) {
            assertThrows(IllegalArgumentException.class, () -> cipher.decode("value", EMPTY_OBJECT_ARRAY));
        }
    }

    @Test
    void verifyEmptyJwks() throws Throwable {
        val keystoreFile = getKeystoreFile();
        try (val cipher = new JsonWebKeySetStringCipherExecutor(keystoreFile, Optional.empty(), null)) {
            assertNotNull(cipher.decode("value", EMPTY_OBJECT_ARRAY));
        }
    }

    @Test
    void verifyEmptyPayload() throws Throwable {
        val data = "{ \"keys\": [] }";
        val keystoreFile = getKeystoreFile();
        try (val webServer = new MockWebServer(data);
             val cipher = new JsonWebKeySetStringCipherExecutor(keystoreFile, "http://localhost:" + webServer.getPort())) {
            webServer.start();
            assertThrows(IllegalArgumentException.class, () -> cipher.encode(UUID.randomUUID().toString()));
        }
    }
}
