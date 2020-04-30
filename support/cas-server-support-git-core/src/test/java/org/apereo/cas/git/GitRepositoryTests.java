package org.apereo.cas.git;

import org.apereo.cas.configuration.CasConfigurationProperties;

import lombok.extern.slf4j.Slf4j;
import lombok.val;
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
    public void verifyPushPull() {
        val props = casProperties.getServiceRegistry().getGit();
        props.setRepositoryUrl("https://github.com/mmoayyed/sample-data.git");
        props.setBranchesToClone("master");
        val builder = GitRepositoryBuilder.newInstance(props).build();
        assertFalse(builder.getObjectsInRepository().isEmpty());
        assertTrue(builder.pull());

        builder.getCredentialsProvider().add(new CredentialsProvider() {
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
            builder.push();
            fail("Pushing changes should fail");
        } catch (final Exception e) {
            LOGGER.trace(e.getMessage(), e);
        }
    }
}
