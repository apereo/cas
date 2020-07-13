package org.apereo.cas.util.cipher;

import org.apereo.cas.util.MockWebServer;

import lombok.SneakyThrows;
import lombok.val;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.MediaType;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.attribute.FileTime;
import java.time.Instant;

import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link JsonWebKeySetStringCipherExecutorTests}.
 *
 * @author Misagh Moayyed
 * @since 5.3.0
 */
@Tag("Simple")
public class JsonWebKeySetStringCipherExecutorTests {
    @Test
    @SneakyThrows
    public void verifyAction() {
        val jwksKeystore = new ClassPathResource("sample.jwks");
        val data = IOUtils.toString(jwksKeystore.getInputStream(), StandardCharsets.UTF_8);
        val keystoreFile = new File(FileUtils.getTempDirectoryPath(), "sample.jwks");
        FileUtils.write(keystoreFile, data, StandardCharsets.UTF_8);

        try (val webServer = new MockWebServer(8435,
            new ByteArrayResource(data.getBytes(StandardCharsets.UTF_8), "REST Output"), MediaType.APPLICATION_JSON_VALUE);
             val cipher = new JsonWebKeySetStringCipherExecutor(keystoreFile, "http://localhost:8435")) {
            webServer.start();
            val token = cipher.encode("Misagh");
            assertEquals("Misagh", cipher.decode(token));
            Files.setLastModifiedTime(keystoreFile.toPath(), FileTime.from(Instant.now()));
            Thread.sleep(5_000);
            cipher.destroy();
        }

    }
}
