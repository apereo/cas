package org.apereo.cas.services;

import org.apereo.cas.config.CasCoreAuthenticationMetadataConfiguration;
import org.apereo.cas.config.CasCoreNotificationsConfiguration;
import org.apereo.cas.config.CasCoreServicesConfiguration;
import org.apereo.cas.config.CasCoreUtilConfiguration;
import org.apereo.cas.config.CasCoreWebConfiguration;
import org.apereo.cas.config.GitServiceRegistryConfiguration;
import org.apereo.cas.config.support.CasWebApplicationServiceFactoryConfiguration;
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
import org.apache.commons.io.file.PathUtils;
import org.apache.commons.io.file.StandardDeleteOption;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.JGitInternalException;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.mail.MailSenderAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.autoconfigure.RefreshAutoConfiguration;
import org.springframework.context.ConfigurableApplicationContext;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * This is {@link GitServiceRegistryTests}.
 *
 * @author Misagh Moayyed
 * @since 6.1.0
 */
@SpringBootTest(classes = {
    GitServiceRegistryConfiguration.class,
    CasCoreServicesConfiguration.class,
    CasCoreNotificationsConfiguration.class,
    CasCoreUtilConfiguration.class,
    CasCoreWebConfiguration.class,
    CasWebApplicationServiceFactoryConfiguration.class,
    CasCoreAuthenticationMetadataConfiguration.class,
    RefreshAutoConfiguration.class,
    MailSenderAutoConfiguration.class
},
    properties = {
        "cas.service-registry.git.sign-commits=false",
        "cas.service-registry.git.root-directory=svc-cfg",
        "cas.service-registry.git.repository-url=file://${java.io.tmpdir}/cas-sample-data"
    })
@Slf4j
@Tag("FileSystem")
@Getter
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class GitServiceRegistryTests extends AbstractServiceRegistryTests {

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
            val gitRepoSampleDir = new File(FileUtils.getTempDirectory(), "cas-sample-data");
            if (gitRepoSampleDir.exists()) {
                PathUtils.delete(gitRepoSampleDir.toPath(),
                    StandardDeleteOption.OVERRIDE_READ_ONLY);
            }
            val gitDir = new File(FileUtils.getTempDirectory(), GitServiceRegistryProperties.DEFAULT_CAS_SERVICE_REGISTRY_NAME);
            if (gitDir.exists()) {
                PathUtils.delete(gitDir.toPath(),
                    StandardDeleteOption.OVERRIDE_READ_ONLY);
            }
            val gitSampleRepo = Git.init().setDirectory(gitRepoSampleDir).setBare(false).call();
            FileUtils.write(new File(gitRepoSampleDir, "readme.txt"), "text", StandardCharsets.UTF_8);
            gitSampleRepo.add().addFilepattern("*.txt").call();
            gitSampleRepo.commit().setSign(false).setMessage("Initial commit").call();

            val git = Git.init().setDirectory(gitDir).setBare(false).call();
            FileUtils.write(new File(gitDir, "readme.txt"), "text", StandardCharsets.UTF_8);
            git.add().addFilepattern("*.txt").call();
            git.commit().setSign(false).setMessage("Initial commit").call();
        } catch (final Exception e) {
            LoggingUtils.error(LOGGER, e);
            fail(e.getMessage(), e);
        }
    }

    @AfterAll
    public static void cleanUp() throws Exception {
        val gitRepoDir = new File(FileUtils.getTempDirectory(), "cas-sample-data");
        PathUtils.deleteDirectory(gitRepoDir.toPath(), StandardDeleteOption.OVERRIDE_READ_ONLY);
        val gitDir = new File(FileUtils.getTempDirectory(), GitServiceRegistryProperties.DEFAULT_CAS_SERVICE_REGISTRY_NAME);
        if (gitDir.exists()) {
            PathUtils.deleteDirectory(gitDir.toPath(), StandardDeleteOption.OVERRIDE_READ_ONLY);
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
            false, null, List.of(), List.of());
        assertEquals(size, registry.load().size());
    }

    /**
     * Validate that load doesn't find services at root of git repo (or sub-directories other than the root-directory).
     * Second service is copied to two other locations and deleted in order to commit all changes to the repository.
     */
    @Test
    public void verifyLoadWithRootDirectory() throws IOException {
        val svc = buildRegisteredServiceInstance(RandomUtils.nextLong(), RegexRegisteredService.class);
        val svc2 = buildRegisteredServiceInstance(RandomUtils.nextLong(), RegexRegisteredService.class);
        svc.setId(RegisteredService.INITIAL_IDENTIFIER_VALUE);
        svc2.setId(RegisteredService.INITIAL_IDENTIFIER_VALUE);
        newServiceRegistry.save(svc);
        newServiceRegistry.save(svc2);
        val size = newServiceRegistry.load().size();
        assertEquals(2, size);
        val gitDir = new File(FileUtils.getTempDirectory(), GitServiceRegistryProperties.DEFAULT_CAS_SERVICE_REGISTRY_NAME);
        val rootDir = new File(gitDir, "svc-cfg");
        val clientDir = new File(rootDir, RegexRegisteredService.FRIENDLY_NAME);
        val svc2FileName = svc2.getName() + "-" + svc2.getId() + ".json";
        val svc2File = new File(clientDir, svc2FileName);
        val anotherRootDir = new File(gitDir, "svc-cfg2");
        FileUtils.copyFile(svc2File, new File(gitDir, svc2FileName));
        FileUtils.copyFile(svc2File, new File(anotherRootDir, svc2FileName));
        newServiceRegistry.delete(svc2);
        val size2 = newServiceRegistry.load().size();
        assertEquals(1, size2);
    }
}
