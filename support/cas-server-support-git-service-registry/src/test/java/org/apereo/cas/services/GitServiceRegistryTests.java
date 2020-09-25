package org.apereo.cas.services;

import org.apereo.cas.config.CasCoreAuthenticationMetadataConfiguration;
import org.apereo.cas.config.CasCoreServicesConfiguration;
import org.apereo.cas.config.CasCoreUtilConfiguration;
import org.apereo.cas.config.GitServiceRegistryConfiguration;

import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.io.FileUtils;
import org.eclipse.jgit.api.Git;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.mail.MailSenderAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.autoconfigure.RefreshAutoConfiguration;

import java.io.File;
import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.*;

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
    RefreshAutoConfiguration.class,
    MailSenderAutoConfiguration.class
},
    properties = {
        "cas.service-registry.git.sign-commits=false",
        "cas.service-registry.git.root-directory=svc-cfg",
        "cas.service-registry.git.repositoryUrl=file:/tmp/cas-sample-data.git"
    })
@Slf4j
@Tag("FileSystem")
public class GitServiceRegistryTests extends AbstractServiceRegistryTests {

    @Autowired
    @Qualifier("serviceRegistry")
    private ServiceRegistry serviceRegistry;

    @BeforeAll
    public static void setup() {
        try {
            FileUtils.deleteDirectory(new File("/tmp/cas-sample-data"));
            val gitDir = new File(FileUtils.getTempDirectory(), "cas-service-registry");
            if (gitDir.exists()) {
                FileUtils.deleteDirectory(gitDir);
            }
            val git = Git.init().setDirectory(gitDir).setBare(false).call();
            FileUtils.write(new File(gitDir, "readme.txt"), "text", StandardCharsets.UTF_8);
            git.commit().setSign(false).setMessage("Initial commit").call();
        } catch (final Exception e) {
            LOGGER.error(e.getMessage(), e);
            fail(e.getMessage(), e);
        }
    }

    @AfterAll
    public static void cleanUp() throws Exception {
        FileUtils.deleteDirectory(new File("/tmp/cas-sample-data"));
        val gitDir = new File(FileUtils.getTempDirectory(), "cas-service-registry");
        if (gitDir.exists()) {
            FileUtils.deleteDirectory(gitDir);
        }
    }

    @Override
    public ServiceRegistry getNewServiceRegistry() {
        return this.serviceRegistry;
    }
}
