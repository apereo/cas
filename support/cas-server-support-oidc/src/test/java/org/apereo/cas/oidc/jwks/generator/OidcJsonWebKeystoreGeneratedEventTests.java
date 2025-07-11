package org.apereo.cas.oidc.jwks.generator;

import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.core.io.FileSystemResource;
import java.nio.file.Files;
import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link OidcJsonWebKeystoreGeneratedEventTests}.
 *
 * @author Misagh Moayyed
 * @since 6.5.0
 */
@Tag("OIDC")
class OidcJsonWebKeystoreGeneratedEventTests {

    @Test
    void verifyOperation() throws Throwable {
        val event = new OidcJsonWebKeystoreGeneratedEvent(this,
            new FileSystemResource(Files.createTempFile("first", "second").toFile()), null);
        assertNotNull(event.getSource());
        assertNotNull(event.getFile());
    }
}


