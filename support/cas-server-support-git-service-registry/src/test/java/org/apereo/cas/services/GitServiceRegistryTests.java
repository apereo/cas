package org.apereo.cas.services;

import org.apereo.cas.config.CasCoreAuthenticationMetadataConfiguration;
import org.apereo.cas.config.CasCoreServicesConfiguration;
import org.apereo.cas.config.CasCoreUtilConfiguration;
import org.apereo.cas.config.GitServiceRegistryConfiguration;

import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.io.FileUtils;
import org.eclipse.jgit.api.Git;
import org.junit.jupiter.api.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.autoconfigure.RefreshAutoConfiguration;
import org.springframework.test.context.TestPropertySource;

import java.io.File;

/**
 * This is {@link GitServiceRegistryTests}.
 *
 * @author Misagh Moayyed
 * @since 6.1.0
 */
@SpringBootTest(classes = {
    GitServiceRegistryConfiguration.class,
    CasCoreServicesConfiguration.class,
    CasCoreUtilConfiguration.class,
    CasCoreAuthenticationMetadataConfiguration.class,
    RefreshAutoConfiguration.class})
@Slf4j
@TestPropertySource(properties = {
    "cas.serviceRegistry.git.repositoryUrl=file:/tmp/cas-sample-data.git"
})
@Tag("FileSystem")
public class GitServiceRegistryTests extends AbstractServiceRegistryTests {

    @Autowired
    @Qualifier("serviceRegistry")
    private ServiceRegistry serviceRegistry;

    static {
        try {
            val gitDir = new File(FileUtils.getTempDirectory(), "cas-service-registry");
            if (gitDir.exists()) {
                FileUtils.deleteDirectory(gitDir);
            }
            val git = Git.init().setDirectory(gitDir).setBare(false).call();
            LOGGER.debug(git.getRepository().getBranch());
            git.commit().setMessage("Initial commit").call();
        } catch (final Exception e) {
            LOGGER.error(e.getMessage(), e);
        }
    }

    @Override
    public ServiceRegistry getNewServiceRegistry() {
        return this.serviceRegistry;
    }
}
