package org.apereo.cas.oidc.jwks.generator;

import org.apereo.cas.configuration.model.support.oidc.OidcProperties;
import org.apereo.cas.configuration.support.CasConfigurationJasyptCipherExecutor;
import org.apereo.cas.oidc.AbstractOidcTests;
import org.apereo.cas.util.RandomUtils;
import lombok.val;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.jose4j.jwk.JsonWebKeySet;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.core.io.FileSystemResource;
import org.springframework.test.context.TestPropertySource;
import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.Date;
import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link OidcDefaultJsonWebKeystoreGeneratorServiceTests}.
 *
 * @author Misagh Moayyed
 * @since 5.3.0
 */
@Tag("OIDC")
class OidcDefaultJsonWebKeystoreGeneratorServiceTests {
    static {
        System.setProperty(CasConfigurationJasyptCipherExecutor.JasyptEncryptionParameters.PASSWORD.getPropertyName(), "P@$$w0rd");
    }

    @TestPropertySource(properties = "cas.authn.oidc.jwks.file-system.jwks-file={\"keys\": []}")
    @Nested
    class EmbeddedKeystoreTests extends AbstractOidcTests {
        @Test
        void verifyOperation() throws Throwable {
            val resource = oidcJsonWebKeystoreGeneratorService.find();
            assertTrue(resource.isPresent());
            val jwks = new JsonWebKeySet(IOUtils.toString(resource.get().getInputStream(), StandardCharsets.UTF_8));
            assertTrue(jwks.getJsonWebKeys().isEmpty());
        }
    }

    @TestPropertySource(properties = "cas.authn.oidc.jwks.file-system.jwks-file=classpath:/encrypted.jwks")
    @Nested
    class EncryptedKeystoreTests extends AbstractOidcTests {
        @Test
        void verifyOperation() throws Throwable {
            val resource = oidcJsonWebKeystoreGeneratorService.find();
            assertTrue(resource.isPresent());
            val jwks = new JsonWebKeySet(IOUtils.toString(resource.get().getInputStream(), StandardCharsets.UTF_8));
            assertFalse(jwks.getJsonWebKeys().isEmpty());
        }
    }

    @TestPropertySource(properties = "cas.authn.oidc.jwks.file-system.jwks-file=file:${#systemProperties['java.io.tmpdir']}/something.jwks")
    @Nested
    class DefaultTests extends AbstractOidcTests {
        private File keystore;

        @BeforeEach
        void setup() {
            keystore = new File(FileUtils.getTempDirectoryPath(), "something.jwks");
            if (keystore.exists()) {
                assertTrue(keystore.delete());
            }
        }

        @Test
        void verifyOperation() throws Throwable {
            val resource = oidcJsonWebKeystoreGeneratorService.generate();
            assertTrue(resource.exists());
            assertTrue(keystore.setLastModified(new Date().getTime()));
            Thread.sleep(2000);
            oidcJsonWebKeystoreGeneratorService.store(
                OidcJsonWebKeystoreGeneratorService.toJsonWebKeyStore(resource));
            assertTrue(oidcJsonWebKeystoreGeneratorService.find().isPresent());
            ((DisposableBean) oidcJsonWebKeystoreGeneratorService).destroy();
        }

        @Test
        void verifyRegeneration() throws Throwable {
            val resource1 = oidcJsonWebKeystoreGeneratorService.generate();
            assertTrue(resource1.exists());
            val resource2 = oidcJsonWebKeystoreGeneratorService.generate();
            assertTrue(resource2.exists());
        }

        @Test
        void verifyCurve256() throws Throwable {
            val properties = new OidcProperties();
            properties.getJwks().getCore().setJwksType("ec");
            properties.getJwks().getCore().setJwksKeySize(256);
            verifyGeneration(properties);
        }

        @Test
        void verifyCurve384() throws Throwable {
            val properties = new OidcProperties();
            properties.getJwks().getCore().setJwksType("ec");
            properties.getJwks().getCore().setJwksKeySize(384);
            verifyGeneration(properties);
        }

        @Test
        void verifyCurve521() throws Throwable {
            val properties = new OidcProperties();
            properties.getJwks().getCore().setJwksType("ec");
            properties.getJwks().getCore().setJwksKeySize(521);
            verifyGeneration(properties);
        }

        private void verifyGeneration(final OidcProperties properties) throws Throwable {
            val file = Files.createTempFile(RandomUtils.randomAlphabetic(6), ".jwks").toFile();
            properties.getJwks().getFileSystem().setJwksFile(file.getAbsolutePath());
            val service = new OidcDefaultJsonWebKeystoreGeneratorService(properties, applicationContext);
            service.generate(new FileSystemResource(file));
            assertTrue(file.exists());
            service.destroy();
        }
    }
}
