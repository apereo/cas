package org.apereo.cas.support.saml.idp.metadata;

import org.apereo.cas.support.saml.BaseGitSamlMetadataTests;
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
    "cas.authn.saml-idp.metadata.http.metadata-backup-location=file://${java.io.tmpdir}/metadata-backups",

    "cas.authn.saml-idp.metadata.git.sign-commits=false",
    "cas.authn.saml-idp.metadata.git.idp-metadata-enabled=true",
    "cas.authn.saml-idp.metadata.git.repository-url=file://${java.io.tmpdir}/cas-metadata-idp",
    "cas.authn.saml-idp.metadata.git.clone-directory.location=file://${java.io.tmpdir}/cas-saml-metadata-gsimlt"
})
@Tag("Git")
@Slf4j
class GitSamlIdPMetadataLocatorTests extends BaseGitSamlMetadataTests {

    @BeforeAll
    public static void setup() {
        try {
            val gitDir = new File(FileUtils.getTempDirectory(), "cas-metadata-idp");
            cleanUp();
            if (!gitDir.mkdir()) {
                throw new IllegalArgumentException("Git repository directory location " + gitDir + " cannot be located/created");
            }
            val git = Git.init().setDirectory(gitDir).setBare(false).call();
            FileUtils.write(new File(gitDir, "readme.txt"), "text", StandardCharsets.UTF_8);
            git.add().addFilepattern("*.txt").call();
            git.commit().setSign(false).setMessage("Initial commit").call();
        } catch (final Exception e) {
            LoggingUtils.error(LOGGER, e);
            fail(e.getMessage(), e);
        }
    }

    @AfterAll
    public static void cleanUp() {
        val gitRepoDir = new File(FileUtils.getTempDirectory(), "cas-metadata-idp");
        if (gitRepoDir.exists()) {
            FunctionUtils.doAndHandle(
                __ -> PathUtils.deleteDirectory(gitRepoDir.toPath(), StandardDeleteOption.OVERRIDE_READ_ONLY));
        }
        val cloneDirectory = "cas-saml-metadata-gsimlt";
        val gitCloneRepoDir = new File(FileUtils.getTempDirectory(), cloneDirectory);
        if (gitCloneRepoDir.exists()) {
            FunctionUtils.doAndHandle(
                __ -> PathUtils.deleteDirectory(gitCloneRepoDir.toPath(), StandardDeleteOption.OVERRIDE_READ_ONLY));
        }
    }

    @Test
    void verifySigningKeyWithoutService() throws Throwable {
        val resource = samlIdPMetadataLocator.resolveSigningKey(Optional.empty());
        assertNotNull(resource);
    }
}
