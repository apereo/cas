package org.apereo.cas.git;

import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.util.ResourceUtils;

import lombok.val;
import org.apache.commons.io.FileUtils;
import org.eclipse.jgit.util.StringUtils;
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

    /**
     * Verify GitRepositoryBuilder.
     * Build method throws IllegalArgumentException due to authentication failure since key is invalid.
     * This test will pass on ci/cd server because there is no known_hosts, and no ssh keys setup for github.com.
     * The underlying ssh library will use .ssh/known_hosts and use .ssh/config to find keys and .ssh/id_rsa,
     * etc when connecting.
     */
    @Test
    public void verifyBuild() throws Exception {
        val props = casProperties.getServiceRegistry().getGit();
        props.setRepositoryUrl("git@github.com:mmoayyed/sample-data.git");
        props.setUsername("casuser");
        props.setPassword("password");
        props.setBranchesToClone("master");
        props.getCloneDirectory().setLocation(ResourceUtils.getRawResourceFrom(
                FileUtils.getTempDirectoryPath() + File.separator + UUID.randomUUID().toString()));
        props.setPrivateKeyPassphrase("something");
        props.setSshSessionPassword("more-password");
        props.getPrivateKey().setLocation(new ClassPathResource("priv.key"));
        props.setStrictHostKeyChecking(true);
        val builder = GitRepositoryBuilder.newInstance(props);
        val e = assertThrows(IllegalArgumentException.class, builder::build);
        assertTrue(StringUtils.toLowerCase(e.getMessage()).contains("reject hostkey"),
            '[' + e.getMessage() + "] doesn't contain reject hostkey");
        props.setStrictHostKeyChecking(false);
        val builder2 = GitRepositoryBuilder.newInstance(props);
        val e2 = assertThrows(IllegalArgumentException.class, builder2::build);
        assertTrue(StringUtils.toLowerCase(e2.getMessage()).contains("auth fail"),
            '[' + e2.getMessage() + "] doesn't contain auth fail");
    }

    /**
     * Test that clone directory works with file: prefix.
     * Uses the file:// prefix rather than file: because it should work on windows or linux.
     */
    @Test
    public void verifyBuildWithFilePrefix() throws Exception {
        val props = casProperties.getServiceRegistry().getGit();
        props.setRepositoryUrl("https://github.com/mmoayyed/sample-data.git");
        props.setUsername("casuser");
        props.setPassword("password");
        props.setBranchesToClone("master");
        props.getCloneDirectory().setLocation(ResourceUtils.getRawResourceFrom(
                "file://" + FileUtils.getTempDirectoryPath() + File.separator + UUID.randomUUID().toString()));
        val builder = GitRepositoryBuilder.newInstance(props);
        assertDoesNotThrow(builder::build);
    }
}
