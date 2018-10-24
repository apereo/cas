package org.apereo.cas.util.cipher;

import org.apereo.cas.util.MockWebServer;

import lombok.val;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.junit.jupiter.api.Test;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.MediaType;

import java.io.File;
import java.nio.charset.StandardCharsets;

import static org.junit.Assert.*;

/**
 * This is {@link JsonWebKeySetStringCipherExecutorTests}.
 *
 * @author Misagh Moayyed
 * @since 5.3.0
 */
public class JsonWebKeySetStringCipherExecutorTests {
    @Test
    public void verifyAction() throws Exception {
        val jwksKeystore = new ClassPathResource("sample.jwks");
        val data = IOUtils.toString(jwksKeystore.getInputStream(), StandardCharsets.UTF_8);
        val keystoreFile = new File(FileUtils.getTempDirectoryPath(), "sample.jwks");
        FileUtils.write(keystoreFile, data, StandardCharsets.UTF_8);

        try (val webServer = new MockWebServer(8435,
            new ByteArrayResource(data.getBytes(StandardCharsets.UTF_8), "REST Output"), MediaType.APPLICATION_JSON_VALUE)) {
            webServer.start();
            val cipher = new JsonWebKeySetStringCipherExecutor(keystoreFile, "http://localhost:8435");
            val token = cipher.encode("Misagh");
            assertEquals("Misagh", cipher.decode(token));
        } catch (final Exception e) {
            throw new AssertionError(e.getMessage(), e);
        }
    }
}
