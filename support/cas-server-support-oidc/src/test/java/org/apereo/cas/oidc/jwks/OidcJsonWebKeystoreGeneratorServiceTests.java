package org.apereo.cas.oidc.jwks;

import org.apereo.cas.oidc.AbstractOidcTests;

import lombok.val;
import org.apache.commons.io.FileUtils;
import org.junit.jupiter.api.Test;
import org.springframework.core.io.FileSystemResource;

import java.io.File;

import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link OidcJsonWebKeystoreGeneratorServiceTests}.
 *
 * @author Misagh Moayyed
 * @since 5.3.0
 */
public class OidcJsonWebKeystoreGeneratorServiceTests extends AbstractOidcTests {
    @Test
    public void verifyOperation() {
        val file = new File(FileUtils.getTempDirectoryPath(), "something.jwks");
        file.delete();
        oidcJsonWebKeystoreGeneratorService.generate(new FileSystemResource(file));
        assertTrue(file.exists());
    }
}
