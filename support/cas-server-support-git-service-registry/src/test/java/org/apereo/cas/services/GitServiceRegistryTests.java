package org.apereo.cas.services;

import org.apereo.cas.config.CasCoreAuthenticationMetadataConfiguration;
import org.apereo.cas.config.CasCoreNotificationsConfiguration;
import org.apereo.cas.config.CasCoreServicesConfiguration;
import org.apereo.cas.config.CasCoreUtilConfiguration;
import org.apereo.cas.config.GitServiceRegistryConfiguration;
import org.apereo.cas.configuration.model.support.git.services.GitServiceRegistryProperties;
import org.apereo.cas.git.GitRepository;
import org.apereo.cas.services.util.RegisteredServiceJsonSerializer;
import org.apereo.cas.services.util.RegisteredServiceYamlSerializer;
import org.apereo.cas.util.CollectionUtils;
import org.apereo.cas.util.LoggingUtils;
import org.apereo.cas.util.RandomUtils;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.io.FileUtils;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.JGitInternalException;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.mail.MailSenderAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.autoconfigure.RefreshAutoConfiguration;
import org.springframework.context.ConfigurableApplicationContext;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * This is {@link GitServiceRegistryTests}.
 * When running on Windows, needs -Dtmpdir=c:/tmp - java.io.tmpdir doesn't work b/c slashes need to be forward.
 * @author Misagh Moayyed
 * @since 6.1.0
 */
@SpringBootTest(classes = {
    GitServiceRegistryConfiguration.class,
    CasCoreServicesConfiguration.class,
    CasCoreNotificationsConfiguration.class,
    CasCoreUtilConfiguration.class,
    CasCoreAuthenticationMetadataConfiguration.class,
    RefreshAutoConfiguration.class,
    MailSenderAutoConfiguration.class
},
    properties = {
        "cas.service-registry.git.sign-commits=false",
        "cas.service-registry.git.root-directory=svc-cfg",
        "cas.service-registry.git.repository-url=file://${tmpdir:/tmp}/cas-sample-data"
    })
@Slf4j
@Tag("FileSystem")
@Getter
public class GitServiceRegistryTests extends AbstractServiceRegistryTests {

    private static String TMPDIR = "/tmp";

    @Autowired
    private ConfigurableApplicationContext applicationContext;

    @Autowired
    @Qualifier("gitServiceRegistryRepositoryInstance")
    private GitRepository gitRepositoryInstance;

    @Autowired
    @Qualifier("serviceRegistry")
    private ServiceRegistry newServiceRegistry;

    @BeforeAll
    public static void setup() {
        try {
            val gitRepoSampleDir = new File(TMPDIR +"/cas-sample-data");
            if (gitRepoSampleDir.exists()) {
                FileUtils.deleteDirectory(gitRepoSampleDir);
            }
            val gitDir = new File(FileUtils.getTempDirectory(), GitServiceRegistryProperties.DEFAULT_CAS_SERVICE_REGISTRY_NAME);
            if (gitDir.exists()) {
                FileUtils.deleteDirectory(gitDir);
            }
            val gitSampleRepo = Git.init().setDirectory(gitRepoSampleDir).setBare(false).call();
            FileUtils.write(new File(gitRepoSampleDir, "readme.txt"), "text", StandardCharsets.UTF_8);
            gitSampleRepo.commit().setSign(false).setMessage("Initial commit").call();

            val git = Git.init().setDirectory(gitDir).setBare(false).call();
            FileUtils.write(new File(gitDir, "readme.txt"), "text", StandardCharsets.UTF_8);
            git.commit().setSign(false).setMessage("Initial commit").call();
        } catch (final Exception e) {
            LoggingUtils.error(LOGGER, e);
            fail(e.getMessage(), e);
        }
    }

    @Test
    public void verifyPullFails() {
        val gitRepository = mock(GitRepository.class);
        when(gitRepository.getObjectsInRepository()).thenThrow(new JGitInternalException("error"));
        when(gitRepository.getObjectsInRepository(any())).thenThrow(new JGitInternalException("error"));
        when(gitRepository.getRepositoryDirectory()).thenReturn(gitRepositoryInstance.getRepositoryDirectory());
        
        val svc = buildRegisteredServiceInstance(RandomUtils.nextLong(), RegexRegisteredService.class);
        svc.setId(RegisteredService.INITIAL_IDENTIFIER_VALUE);
        newServiceRegistry.save(svc);
        val size = newServiceRegistry.load().size();
        
        val registry = new GitServiceRegistry(applicationContext, gitRepository,
            CollectionUtils.wrapList(
                new RegisteredServiceJsonSerializer(),
                new RegisteredServiceYamlSerializer()),
            false, List.of(), List.of());
        assertEquals(size, registry.load().size());
    }

    @AfterAll
    public static void cleanUp() throws Exception {
        FileUtils.deleteDirectory(new File(TMPDIR +"/cas-sample-data"));
        val gitDir = new File(FileUtils.getTempDirectory(), GitServiceRegistryProperties.DEFAULT_CAS_SERVICE_REGISTRY_NAME);
        if (gitDir.exists()) {
            FileUtils.deleteDirectory(gitDir);
        }
    }
}
