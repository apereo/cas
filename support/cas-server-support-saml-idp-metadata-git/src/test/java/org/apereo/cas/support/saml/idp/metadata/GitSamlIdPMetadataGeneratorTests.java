package org.apereo.cas.support.saml.idp.metadata;

import module java.base;
import org.apereo.cas.support.saml.BaseGitSamlMetadataTests;
import org.apereo.cas.support.saml.services.SamlRegisteredService;
import org.apereo.cas.util.LoggingUtils;
import org.apereo.cas.util.function.FunctionUtils;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.file.PathUtils;
import org.apache.commons.io.file.StandardDeleteOption;
import org.eclipse.jgit.api.Git;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.test.context.TestPropertySource;
import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link GitSamlIdPMetadataGeneratorTests}.
 *
 * @author Misagh Moayyed
 * @since 6.0.0
 */
@TestPropertySource(properties = {
    "cas.authn.saml-idp.metadata.http.metadata-backup-location=file://${java.io.tmpdir}/metadata-backups",

    "cas.authn.saml-idp.metadata.git.sign-commits=false",
    "cas.authn.saml-idp.metadata.git.idp-metadata-enabled=true",
    "cas.authn.saml-idp.metadata.git.repository-url=file://${java.io.tmpdir}/cas-saml-metadata"
})
@Tag("Git")
@Slf4j
class GitSamlIdPMetadataGeneratorTests extends BaseGitSamlMetadataTests {
    @BeforeAll
    public static void setup() {
        try {
            val gitDir = new File(FileUtils.getTempDirectory(), "cas-saml-metadata");
            if (gitDir.exists()) {
                FunctionUtils.doAndHandle(
                    _ -> PathUtils.deleteDirectory(gitDir.toPath(), StandardDeleteOption.OVERRIDE_READ_ONLY));
            }
            if (!gitDir.mkdir()) {
                throw new IllegalArgumentException("Git repository directory location " + gitDir + " cannot be located/created");
            }
            try (val git = Git.init().setDirectory(gitDir).setBare(false).call()) {
                FileUtils.write(new File(gitDir, "readme.txt"), "text", StandardCharsets.UTF_8);
                git.add().addFilepattern("*.*").call();
                git.commit().setSign(false).setMessage("Initial commit").call();
            }
            assertTrue(gitDir.exists());
        } catch (final Exception e) {
            LoggingUtils.error(LOGGER, e);
            fail(e.getMessage(), e);
        }
    }

    @AfterAll
    public static void cleanUp() {
        val gitDir = new File(FileUtils.getTempDirectory(), "cas-saml-metadata");
        if (gitDir.exists()) {
            FunctionUtils.doAndHandle(
                _ -> PathUtils.deleteDirectory(gitDir.toPath(), StandardDeleteOption.OVERRIDE_READ_ONLY));
        }
    }

    @Test
    void verifyOperation() throws Throwable {
        this.samlIdPMetadataGenerator.generate(Optional.empty());
        assertNotNull(samlIdPMetadataLocator.resolveMetadata(Optional.empty()));
        assertNotNull(samlIdPMetadataLocator.resolveEncryptionCertificate(Optional.empty()));
        assertNotNull(samlIdPMetadataLocator.resolveEncryptionKey(Optional.empty()));
        assertNotNull(samlIdPMetadataLocator.resolveSigningCertificate(Optional.empty()));
        assertNotNull(samlIdPMetadataLocator.resolveSigningKey(Optional.empty()));
    }

    @Test
    void verifyService() throws Throwable {
        val service = new SamlRegisteredService();
        service.setName("TestShib");
        service.setId(1000);
        val registeredService = Optional.of(service);

        samlIdPMetadataGenerator.generate(registeredService);
        assertNotNull(samlIdPMetadataLocator.resolveMetadata(registeredService));
        assertNotNull(samlIdPMetadataLocator.resolveEncryptionCertificate(registeredService));
        assertNotNull(samlIdPMetadataLocator.resolveEncryptionKey(registeredService));
        assertNotNull(samlIdPMetadataLocator.resolveSigningCertificate(registeredService));
        assertNotNull(samlIdPMetadataLocator.resolveSigningKey(registeredService));
    }
}
