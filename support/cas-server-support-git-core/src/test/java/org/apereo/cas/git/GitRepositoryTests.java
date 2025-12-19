package org.apereo.cas.git;

import module java.base;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.test.CasTestExtension;
import org.apereo.cas.util.LoggingUtils;
import org.apereo.cas.util.ResourceUtils;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.io.FileUtils;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.ResetCommand;
import org.eclipse.jgit.errors.UnsupportedCredentialItem;
import org.eclipse.jgit.transport.CredentialItem;
import org.eclipse.jgit.transport.CredentialsProvider;
import org.eclipse.jgit.transport.URIish;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.autoconfigure.RefreshAutoConfiguration;
import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link GitRepositoryTests}.
 *
 * @author Misagh Moayyed
 * @since 6.2.0
 */
@SpringBootTest(classes = RefreshAutoConfiguration.class)
@ExtendWith(CasTestExtension.class)
@EnableConfigurationProperties(CasConfigurationProperties.class)
@Tag("Git")
@Slf4j
class GitRepositoryTests {

    @Autowired
    private CasConfigurationProperties casProperties;

    @Test
    void verifyPushPull() throws Throwable {
        val props = casProperties.getServiceRegistry().getGit();
        props.setRepositoryUrl("https://github.com/mmoayyed/sample-data.git");
        props.setBranchesToClone("master");
        props.setStrictHostKeyChecking(false);
        props.setClearExistingIdentities(true);
        val cloneDir = FileUtils.getTempDirectoryPath() + File.separator + UUID.randomUUID();
        props.getCloneDirectory().setLocation(ResourceUtils.getRawResourceFrom(cloneDir));
        val repo = GitRepositoryBuilder.newInstance(props).build();
        assertTrue(repo.pull());
        assertFalse(repo.getObjectsInRepository().isEmpty());
        FileUtils.writeStringToFile(new File(cloneDir, "test.txt"), "test", Charset.defaultCharset());
        try (val git = Git.open(ResourceUtils.getRawResourceFrom(cloneDir).getFile())) {
            git.reset().setMode(ResetCommand.ResetType.HARD).setRef("HEAD~1").call();
        }
        repo.getCredentialsProvider().add(new CredentialsProvider() {
            @Override
            public boolean isInteractive() {
                return false;
            }

            @Override
            public boolean supports(final CredentialItem... items) {
                return true;
            }

            @Override
            public boolean get(final URIish uri, final CredentialItem... items) throws UnsupportedCredentialItem {
                return true;
            }
        });
        try {
            repo.commitAll("Test");
            props.setRebase(true);
            val repo2 = GitRepositoryBuilder.newInstance(props).build();
            repo2.pull();
            try (val git = Git.open(ResourceUtils.getRawResourceFrom(cloneDir).getFile())) {
                var log = git.log().setMaxCount(1).call();
                var revCommit = log.iterator().next();
                assertEquals("Test", revCommit.getFullMessage());
            }
            repo.push();
            fail("Pushing changes should fail");
        } catch (final Exception e) {
            LoggingUtils.error(LOGGER, e);
        }
    }
}
