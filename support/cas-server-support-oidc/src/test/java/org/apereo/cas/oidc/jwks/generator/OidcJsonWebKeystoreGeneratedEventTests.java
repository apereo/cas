package org.apereo.cas.oidc.jwks.generator;

import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.core.io.FileSystemResource;

import java.io.File;

import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link OidcJsonWebKeystoreGeneratedEventTests}.
 *
 * @author Misagh Moayyed
 * @since 6.5.0
 */
@Tag("OIDC")
public class OidcJsonWebKeystoreGeneratedEventTests {

    @Test
    public void verifyOperation() throws Exception {
        val event = new OidcJsonWebKeystoreGeneratedEvent(this,
            new FileSystemResource(File.createTempFile("first", "second")));
        assertNotNull(event.getSource());
        assertNotNull(event.getFile());
    }
}


