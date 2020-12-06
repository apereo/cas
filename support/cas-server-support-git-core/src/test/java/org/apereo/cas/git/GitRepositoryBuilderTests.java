package org.apereo.cas.git;

import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.util.ResourceUtils;

import lombok.val;
import org.apache.commons.io.FileUtils;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.autoconfigure.RefreshAutoConfiguration;
import org.springframework.core.io.ClassPathResource;

import java.io.File;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link GitRepositoryBuilderTests}.
 *
 * @author Misagh Moayyed
 * @since 6.2.0
 */
@SpringBootTest(classes = RefreshAutoConfiguration.class)
@EnableConfigurationProperties(CasConfigurationProperties.class)
@Tag("FileSystem")
public class GitRepositoryBuilderTests {

    @Autowired
    private CasConfigurationProperties casProperties;

    @Test
    /**
     * Verify GitRepositoryBuilder.
     * Build method throws IllegalArgumentException due to authentication failure since key is invalid.
     */
    public void verifyBuild() throws Exception {
        val props = casProperties.getServiceRegistry().getGit();
        props.setRepositoryUrl("git@github.com:mmoayyed/sample-data.git");
        props.setUsername("casuser");
        props.setPassword("password");
        props.setBranchesToClone("master");
        props.setCloneDirectory(ResourceUtils.getRawResourceFrom(
                FileUtils.getTempDirectoryPath() + File.separator + UUID.randomUUID().toString()));
        props.setPrivateKeyPassphrase("something");
        props.setSshSessionPassword("more-password");
        props.setPrivateKeyPath(new ClassPathResource("priv.key").getFile());
        props.setStrictHostKeyChecking(false);
        val builder = GitRepositoryBuilder.newInstance(props);
        assertThrows(IllegalArgumentException.class, builder::build);
    }

    @Test
    /**
     * Test that clone directory works with file: prefix.
     * Uses the file:// prefix rather than file: because it should work on windows or linux.
     */
    public void verifyBuildWithFilePrefix() throws Exception {
        val props = casProperties.getServiceRegistry().getGit();
        props.setRepositoryUrl("https://github.com/mmoayyed/sample-data.git");
        props.setUsername("casuser");
        props.setPassword("password");
        props.setBranchesToClone("master");
        props.setCloneDirectory(ResourceUtils.getRawResourceFrom(
                "file://" + FileUtils.getTempDirectoryPath() + File.separator + UUID.randomUUID().toString()));
        val builder = GitRepositoryBuilder.newInstance(props);
        assertDoesNotThrow(builder::build);
    }
}
