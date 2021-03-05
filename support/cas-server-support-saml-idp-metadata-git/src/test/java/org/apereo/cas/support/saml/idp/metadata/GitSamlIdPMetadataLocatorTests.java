package org.apereo.cas.support.saml.idp.metadata;

import org.apereo.cas.support.saml.BaseGitSamlMetadataTests;
import org.apereo.cas.util.LoggingUtils;

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

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link GitSamlIdPMetadataLocatorTests}.
 *
 * @author Misagh Moayyed
 * @since 6.2.0
 */
@TestPropertySource(properties = {
    "cas.authn.saml-idp.metadata.git.sign-commits=false",
    "cas.authn.saml-idp.metadata.git.idp-metadata-enabled=true",
    "cas.authn.saml-idp.metadata.git.repository-url=file:${java.io.tmpdir}/cas-metadata-idp.git"
})
@Tag("FileSystem")
@Slf4j
public class GitSamlIdPMetadataLocatorTests extends BaseGitSamlMetadataTests {

    @BeforeAll
    public static void setup() {
        try {
            val gitRepoDir = new File(FileUtils.getTempDirectory(), "cas-metadata-idp");
            if (gitRepoDir.exists()) {
                PathUtils.deleteDirectory(gitRepoDir.toPath(), StandardDeleteOption.OVERRIDE_READ_ONLY);
            }
            val gitDir = new File(FileUtils.getTempDirectory(), "cas-saml-metadata");
            if (gitDir.exists()) {
                PathUtils.deleteDirectory(gitDir.toPath(), StandardDeleteOption.OVERRIDE_READ_ONLY);
            }
            val git = Git.init().setDirectory(gitDir).setBare(false).call();
            FileUtils.write(new File(gitDir, "readme.txt"), "text", StandardCharsets.UTF_8);
            git.commit().setSign(false).setMessage("Initial commit").call();
        } catch (final Exception e) {
            LoggingUtils.error(LOGGER, e);
            fail(e.getMessage(), e);
        }
    }

    @AfterAll
    public static void cleanUp() throws Exception {
        val gitRepoDir = new File(FileUtils.getTempDirectory(), "cas-metadata-idp");
        if (gitRepoDir.exists()) {
            PathUtils.deleteDirectory(gitRepoDir.toPath(), StandardDeleteOption.OVERRIDE_READ_ONLY);
        }
        val gitDir = new File(FileUtils.getTempDirectory(), "cas-saml-metadata");
        if (gitDir.exists()) {
            PathUtils.deleteDirectory(gitDir.toPath(), StandardDeleteOption.OVERRIDE_READ_ONLY);
        }
    }

    @Test
    public void verifySigningKeyWithoutService() {
        val resource = samlIdPMetadataLocator.resolveSigningKey(Optional.empty());
        assertNotNull(resource);
    }
}
