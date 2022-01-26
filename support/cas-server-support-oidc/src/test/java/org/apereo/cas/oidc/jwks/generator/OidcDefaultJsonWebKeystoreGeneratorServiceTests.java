package org.apereo.cas.oidc.jwks.generator;

import org.apereo.cas.configuration.model.support.oidc.OidcProperties;
import org.apereo.cas.oidc.AbstractOidcTests;

import lombok.val;
import org.apache.commons.io.FileUtils;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.core.io.FileSystemResource;
import org.springframework.test.context.TestPropertySource;

import java.io.File;
import java.util.Date;

import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link OidcDefaultJsonWebKeystoreGeneratorServiceTests}.
 *
 * @author Misagh Moayyed
 * @since 5.3.0
 */
@Tag("OIDC")
@SuppressWarnings("JavaUtilDate")
@TestPropertySource(properties = "cas.authn.oidc.jwks.file-system.jwks-file=file:${#systemProperties['java.io.tmpdir']}/something.jwks")
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
    public void verifyOperation() throws Exception {
        val resource = oidcJsonWebKeystoreGeneratorService.generate();
        assertTrue(resource.exists());
        assertTrue(KEYSTORE.setLastModified(new Date().getTime()));
        Thread.sleep(2000);
        oidcJsonWebKeystoreGeneratorService.store(
            OidcJsonWebKeystoreGeneratorService.toJsonWebKeyStore(resource));
        assertTrue(oidcJsonWebKeystoreGeneratorService.find().isPresent());
        ((DisposableBean) oidcJsonWebKeystoreGeneratorService).destroy();
    }

    @Test
    public void verifyRegeneration() throws Exception {
        val resource1 = oidcJsonWebKeystoreGeneratorService.generate();
        assertTrue(resource1.exists());
        val resource2 = oidcJsonWebKeystoreGeneratorService.generate();
        assertTrue(resource2.exists());
    }

    @Test
    public void verifyCurve256() throws Exception {
        val properties = new OidcProperties();
        properties.getJwks().getCore().setJwksType("ec");
        properties.getJwks().getCore().setJwksKeySize(256);
        val service = new OidcDefaultJsonWebKeystoreGeneratorService(properties, applicationContext);
        service.generate(new FileSystemResource(KEYSTORE));
        assertTrue(KEYSTORE.exists());
        service.destroy();
    }

    @Test
    public void verifyCurve384() throws Exception {
        val properties = new OidcProperties();
        properties.getJwks().getCore().setJwksType("ec");
        properties.getJwks().getCore().setJwksKeySize(384);
        val service = new OidcDefaultJsonWebKeystoreGeneratorService(properties, applicationContext);
        service.generate(new FileSystemResource(KEYSTORE));
        assertTrue(KEYSTORE.exists());
    }

    @Test
    public void verifyCurve521() throws Exception {
        val properties = new OidcProperties();

        properties.getJwks().getCore().setJwksType("ec");
        properties.getJwks().getCore().setJwksKeySize(521);
        val service = new OidcDefaultJsonWebKeystoreGeneratorService(properties, applicationContext);
        service.generate(new FileSystemResource(KEYSTORE));
        assertTrue(KEYSTORE.exists());
        service.destroy();
    }
}
