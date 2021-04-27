package org.apereo.cas.git;


import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.configuration.model.support.git.services.BaseGitProperties;
import org.apereo.cas.util.ResourceUtils;

import lombok.val;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.file.PathUtils;
import org.apache.commons.io.file.StandardDeleteOption;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.function.Executable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.autoconfigure.RefreshAutoConfiguration;
import org.springframework.core.io.ClassPathResource;

import java.io.*;
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
    public void verifyTestPrivateKey() throws Exception {
        val props = casProperties.getServiceRegistry().getGit();
        props.setRepositoryUrl("git@github.com:mmoayyed/sample-data.git");
        props.setBranchesToClone("master");
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
    public void verifyBuild() throws Exception {
        val props = casProperties.getServiceRegistry().getGit();
        props.setRepositoryUrl("git@github.com:mmoayyed/sample-data.git");
        props.setUsername("casuser");
        props.setPassword("password");
        props.setBranchesToClone("master");
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
    public void verifyBuildWithFilePrefix() throws Exception {
        val props = casProperties.getServiceRegistry().getGit();
        props.setRepositoryUrl("https://github.com/mmoayyed/sample-data.git");
        props.setUsername("casuser");
        props.setPassword("password");
        props.setBranchesToClone("master");
        props.getCloneDirectory().setLocation(ResourceUtils.getRawResourceFrom(
            "file://" + FileUtils.getTempDirectoryPath() + File.separator + UUID.randomUUID()));
        val builder = GitRepositoryBuilder.newInstance(props);
        assertDoesNotThrow(builder::build);
    }

    /**
     * This test uses a dummy private repo on gitlab and the username password is a read-only deploy token.
     * Tests client auth of https and cloning repositories with multiple jgit http client implementations.
     * @throws IOException IO error
     */
    @Test
    public void verifyBuildWithHttpClientOptions() throws IOException {
        val readonlyDeployToken = "ST8hSZUWDs7ujS83EVnk";
        for (BaseGitProperties.HttpClientTypes type : BaseGitProperties.HttpClientTypes.values()) {
            val props = casProperties.getServiceRegistry().getGit();
            props.setHttpClientType(type);
            props.setRepositoryUrl("https://gitlab.com/hdeadman-bah/cas-git-auth-test.git");
            props.setUsername("cas-app");
            props.setPassword(readonlyDeployToken);
            props.setBranchesToClone("master");
            val cloneDir = "file://" + FileUtils.getTempDirectoryPath() + File.separator + UUID.randomUUID();
            props.getCloneDirectory().setLocation(ResourceUtils.getRawResourceFrom(cloneDir));
            val builder = GitRepositoryBuilder.newInstance(props);
            val gitRepository = builder.build();
            assertTrue(new File(gitRepository.getRepositoryDirectory(), "README.md").exists());
            gitRepository.destroy();
            PathUtils.deleteDirectory(gitRepository.getRepositoryDirectory().toPath(), StandardDeleteOption.OVERRIDE_READ_ONLY);
        }
    }

}
