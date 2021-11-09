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
import java.nio.file.FileSystemException;
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
    "cas.authn.saml-idp.metadata.git.repository-url=file://${java.io.tmpdir}/cas-metadata-idp",
    "cas.authn.saml-idp.metadata.git.clone-directory.location=file://${java.io.tmpdir}/cas-saml-metadata-gsimlt"
})
@Tag("FileSystem")
@Slf4j
public class GitSamlIdPMetadataLocatorTests extends BaseGitSamlMetadataTests {

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
    public static void cleanUp() throws Exception {
        val gitRepoDir = new File(FileUtils.getTempDirectory(), "cas-metadata-idp");
        if (gitRepoDir.exists()) {
            PathUtils.deleteDirectory(gitRepoDir.toPath(), StandardDeleteOption.OVERRIDE_READ_ONLY);
        }
        val cloneDirectory = "cas-saml-metadata-gsimlt";
        val gitCloneRepoDir = new File(FileUtils.getTempDirectory(), cloneDirectory);
        val cloneRepoPath = gitCloneRepoDir.toPath();
        if (gitCloneRepoDir.exists()) {
            try {
                PathUtils.deleteDirectory(cloneRepoPath, StandardDeleteOption.OVERRIDE_READ_ONLY);
            } catch (final FileSystemException e) {
                LOGGER.warn("Can't cleanup [{}] until bean closed: [{}]", cloneRepoPath, e.getMessage());
            }
        }
    }

    @Test
    public void verifySigningKeyWithoutService() {
        val resource = samlIdPMetadataLocator.resolveSigningKey(Optional.empty());
        assertNotNull(resource);
    }
}
