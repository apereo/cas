package org.apereo.cas.oidc.jwks;

import org.apache.commons.io.FileUtils;
import org.apereo.cas.oidc.AbstractOidcTests;
import org.junit.Test;

import java.io.File;

import static org.junit.Assert.*;

/**
 * This is {@link OidcJsonWebKeystoreGeneratorServiceTests}.
 *
 * @author Misagh Moayyed
 * @since 5.3.0
 */
public class OidcJsonWebKeystoreGeneratorServiceTests extends AbstractOidcTests {
    @Test
    public void verifyOperation() {
        final File file = new File(FileUtils.getTempDirectoryPath(), "something.jwks");
        file.delete();
        oidcJsonWebKeystoreGeneratorService.generate(file);
        assertTrue(file.exists());
    }
}
