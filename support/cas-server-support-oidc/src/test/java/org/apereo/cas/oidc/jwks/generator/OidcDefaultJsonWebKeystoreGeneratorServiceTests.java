package org.apereo.cas.oidc.jwks.generator;

import org.apereo.cas.configuration.model.support.oidc.OidcProperties;
import org.apereo.cas.oidc.AbstractOidcTests;

import lombok.val;
import org.apache.commons.io.FileUtils;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.core.io.FileSystemResource;
import org.springframework.test.context.TestPropertySource;

import java.io.File;

import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link OidcDefaultJsonWebKeystoreGeneratorServiceTests}.
 *
 * @author Misagh Moayyed
 * @since 5.3.0
 */
@Tag("OIDC")
@TestPropertySource(properties = "cas.authn.oidc.jwks.jwksFile=file:${#systemProperties['java.io.tmpdir']}/something.jwks")
public class OidcDefaultJsonWebKeystoreGeneratorServiceTests extends AbstractOidcTests {
    private static File KEYSTORE;

    @BeforeAll
    public static void setup() {
        KEYSTORE = new File(FileUtils.getTempDirectoryPath(), "something.jwks");
        if (KEYSTORE.exists()) {
            assertTrue(KEYSTORE.delete());
        }
    }

    @Test
    public void verifyOperation() {
        val resource = oidcJsonWebKeystoreGeneratorService.generate();
        assertTrue(resource.exists());
    }

    @Test
    public void verifyCurve256() {
        val properties = new OidcProperties();
        properties.getJwks().setJwksType("ec");
        properties.getJwks().setJwksKeySize(256);
        val service = new OidcDefaultJsonWebKeystoreGeneratorService(properties);
        service.generate(new FileSystemResource(KEYSTORE));
        assertTrue(KEYSTORE.exists());
    }

    @Test
    public void verifyCurve384() {
        val properties = new OidcProperties();
        properties.getJwks().setJwksType("ec");
        properties.getJwks().setJwksKeySize(384);
        val service = new OidcDefaultJsonWebKeystoreGeneratorService(properties);
        service.generate(new FileSystemResource(KEYSTORE));
        assertTrue(KEYSTORE.exists());
    }

    @Test
    public void verifyCurve521() {
        val properties = new OidcProperties();

        properties.getJwks().setJwksType("ec");
        properties.getJwks().setJwksKeySize(521);
        val service = new OidcDefaultJsonWebKeystoreGeneratorService(properties);
        service.generate(new FileSystemResource(KEYSTORE));
        assertTrue(KEYSTORE.exists());
    }
}
