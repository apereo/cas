package org.apereo.cas.git;

import org.apereo.cas.configuration.CasConfigurationProperties;

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
    public void verifyBuild() throws Exception {
        val props = casProperties.getServiceRegistry().getGit();
        props.setRepositoryUrl("git@github.com:mmoayyed/sample-data.git");
        props.setUsername("casuser");
        props.setPassword("password");
        props.setBranchesToClone("master");
        props.setCloneDirectory(new File(FileUtils.getTempDirectory(), UUID.randomUUID().toString()));
        props.setPrivateKeyPassphrase("something");
        props.setSshSessionPassword("more-password");
        props.setPrivateKeyPath(new ClassPathResource("priv.key").getFile());
        assertThrows(IllegalArgumentException.class, () -> GitRepositoryBuilder.newInstance(props).build());
    }
}
