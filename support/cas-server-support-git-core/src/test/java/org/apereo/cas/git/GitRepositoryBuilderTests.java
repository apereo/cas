package org.apereo.cas.git;

import module java.base;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.configuration.model.support.git.services.BaseGitProperties;
import org.apereo.cas.test.CasTestExtension;
import org.apereo.cas.util.ResourceUtils;
import lombok.val;
import org.apache.commons.io.FileUtils;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.function.Executable;
import org.junit.jupiter.api.parallel.Execution;
import org.junit.jupiter.api.parallel.ExecutionMode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.autoconfigure.RefreshAutoConfiguration;
import org.springframework.core.io.ClassPathResource;
import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link GitRepositoryBuilderTests}.
 *
 * @author Misagh Moayyed
 * @since 6.2.0
 */
@SpringBootTest(classes = RefreshAutoConfiguration.class)
@ExtendWith(CasTestExtension.class)
@EnableConfigurationProperties(CasConfigurationProperties.class)
@Tag("Git")
@Execution(ExecutionMode.SAME_THREAD)
class GitRepositoryBuilderTests {

    @Autowired
    private CasConfigurationProperties casProperties;

    @Test
    void verifyTestPrivateKey() throws Throwable {
        val props = casProperties.getServiceRegistry().getGit();
        props.setRepositoryUrl("git@github.com:mmoayyed/sample-data.git");
        props.setBranchesToClone("master");
        props.setActiveBranch("master");
        props.setClearExistingIdentities(true);
        props.getCloneDirectory().setLocation(ResourceUtils.getRawResourceFrom(
            FileUtils.getTempDirectoryPath() + File.separator + UUID.randomUUID()));
        props.setPrivateKeyPassphrase("mis@gh");
        props.getPrivateKey().setLocation(new ClassPathResource("apereocasgithub"));
        props.setStrictHostKeyChecking(false);
        val builder = GitRepositoryBuilder.newInstance(props);
        assertDoesNotThrow((Executable) builder::build);
    }

    @Test
    void verifyBuild() throws Throwable {
        val props = casProperties.getServiceRegistry().getGit();
        props.setRepositoryUrl("git@github.com:mmoayyed/sample-data.git");
        props.setUsername("casuser");
        props.setPassword("password");
        props.setBranchesToClone("master");
        props.setActiveBranch("master");
        props.setHttpClientType(BaseGitProperties.HttpClientTypes.HTTP_CLIENT);
        props.getCloneDirectory().setLocation(ResourceUtils.getRawResourceFrom(
            FileUtils.getTempDirectoryPath() + File.separator + UUID.randomUUID()));
        props.setPrivateKeyPassphrase("something");
        props.setSshSessionPassword("more-password");
        props.getPrivateKey().setLocation(new ClassPathResource("priv.key"));
        props.setStrictHostKeyChecking(true);
        val builder = GitRepositoryBuilder.newInstance(props);
        assertThrows(IllegalArgumentException.class, builder::build);
        props.setStrictHostKeyChecking(false);
        val builder2 = GitRepositoryBuilder.newInstance(props);
        assertThrows(IllegalArgumentException.class, builder2::build);
    }

    /**
     * Test that clone directory works with file: prefix.
     * Uses the file:// prefix rather than file: because it should work on windows or linux.
     */
    @Test
    void verifyBuildWithFilePrefix() throws Throwable {
        val props = casProperties.getServiceRegistry().getGit();
        props.setRepositoryUrl("https://github.com/mmoayyed/sample-data.git");
        props.setUsername("casuser");
        props.setPassword("password");
        props.setBranchesToClone("master");
        props.setActiveBranch("master");
        props.getCloneDirectory().setLocation(ResourceUtils.getRawResourceFrom(
            "file://" + FileUtils.getTempDirectoryPath() + File.separator + UUID.randomUUID()));
        val builder = GitRepositoryBuilder.newInstance(props);
        assertDoesNotThrow(builder::build);
    }

    @Test
    void verifyBuildWithBadBranchAndWithoutExistingDirectory() throws Throwable {
        val props = casProperties.getServiceRegistry().getGit();
        props.setRepositoryUrl("https://github.com/mmoayyed/sample-data.git");
        props.setUsername("casuser");
        props.setPassword("password");
        props.setActiveBranch("badbranch");
        props.getCloneDirectory().setLocation(ResourceUtils.getRawResourceFrom(
                FileUtils.getTempDirectoryPath() + File.separator + UUID.randomUUID()));
        val builder = GitRepositoryBuilder.newInstance(props);
        assertThrows(IllegalArgumentException.class, builder::build);
    }

    @Test
    void verifyBuildWithBadBranchButWithExistingDirectory() throws Throwable {
        val directory = FileUtils.getTempDirectoryPath() + File.separator + UUID.randomUUID();

        val props = casProperties.getServiceRegistry().getGit();
        props.setRepositoryUrl("https://github.com/mmoayyed/sample-data.git");
        props.setUsername("casuser");
        props.setPassword("password");
        props.setActiveBranch("master");
        props.getCloneDirectory().setLocation(ResourceUtils.getRawResourceFrom(directory));
        val builder = GitRepositoryBuilder.newInstance(props);
        assertDoesNotThrow(builder::build);

        val props2 = casProperties.getServiceRegistry().getGit();
        props2.setRepositoryUrl("https://github.com/mmoayyed/sample-data.git");
        props2.setUsername("casuser");
        props2.setPassword("password");
        props2.setActiveBranch("badbranch");
        props2.getCloneDirectory().setLocation(ResourceUtils.getRawResourceFrom(directory));
        val builder2 = GitRepositoryBuilder.newInstance(props2);
        assertDoesNotThrow(builder2::build);
    }
}
