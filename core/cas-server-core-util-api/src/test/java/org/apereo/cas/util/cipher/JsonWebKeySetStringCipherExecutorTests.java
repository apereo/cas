package org.apereo.cas.util.cipher;

import org.apache.commons.io.IOUtils;
import org.apereo.cas.util.MockWebServer;
import org.junit.Test;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.MediaType;

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
        final ClassPathResource jwksKeystore = new ClassPathResource("sample.jwks");
        final String data = IOUtils.toString(jwksKeystore.getInputStream(), StandardCharsets.UTF_8);
        try (MockWebServer webServer = new MockWebServer(8435,
            new ByteArrayResource(data.getBytes(StandardCharsets.UTF_8), "REST Output"), MediaType.APPLICATION_JSON_VALUE)) {
            webServer.start();
            final JsonWebKeySetStringCipherExecutor cipher = new JsonWebKeySetStringCipherExecutor(jwksKeystore, "http://localhost:8435");
            final String token = cipher.encode("Misagh");
            assertEquals("Misagh", cipher.decode(token));
        } catch (final Exception e) {
            throw new AssertionError(e.getMessage(), e);
        }
    }
}
