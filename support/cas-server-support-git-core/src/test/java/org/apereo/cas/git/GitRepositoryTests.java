package org.apereo.cas.git;

import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.util.ResourceUtils;

import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.io.FileUtils;
import org.eclipse.jgit.errors.UnsupportedCredentialItem;
import org.eclipse.jgit.transport.CredentialItem;
import org.eclipse.jgit.transport.CredentialsProvider;
import org.eclipse.jgit.transport.URIish;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.autoconfigure.RefreshAutoConfiguration;

import java.io.File;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link GitRepositoryTests}.
 *
 * @author Misagh Moayyed
 * @since 6.2.0
 */
@SpringBootTest(classes = RefreshAutoConfiguration.class)
@EnableConfigurationProperties(CasConfigurationProperties.class)
@Tag("FileSystem")
@Slf4j
public class GitRepositoryTests {

    @Autowired
    private CasConfigurationProperties casProperties;

    @Test
    public void verifyPushPull() throws Exception {
        val props = casProperties.getServiceRegistry().getGit();
        props.setRepositoryUrl("https://github.com/mmoayyed/sample-data.git");
        props.setBranchesToClone("master");
        props.setStrictHostKeyChecking(false);
        props.setClearExistingIdentities(true);
        props.getCloneDirectory().setLocation(ResourceUtils.getRawResourceFrom(
            FileUtils.getTempDirectoryPath() + File.separator + UUID.randomUUID()));
        val repo = GitRepositoryBuilder.newInstance(props).build();
        assertTrue(repo.pull());
        assertFalse(repo.getObjectsInRepository().isEmpty());

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
            repo.push();
            fail("Pushing changes should fail");
        } catch (final Exception e) {
            LOGGER.error(e.getMessage(), e);
        }
    }
}
