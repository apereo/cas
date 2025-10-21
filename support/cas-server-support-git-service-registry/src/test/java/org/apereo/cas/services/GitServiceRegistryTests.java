package org.apereo.cas.services;

import org.apereo.cas.config.CasGitServiceRegistryAutoConfiguration;
import org.apereo.cas.configuration.model.support.git.services.GitServiceRegistryProperties;
import org.apereo.cas.git.GitRepository;
import org.apereo.cas.services.util.RegisteredServiceJsonSerializer;
import org.apereo.cas.services.util.RegisteredServiceYamlSerializer;
import org.apereo.cas.test.CasTestExtension;
import org.apereo.cas.util.CollectionUtils;
import org.apereo.cas.util.LoggingUtils;
import org.apereo.cas.util.RandomUtils;
import org.apereo.cas.util.function.FunctionUtils;
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
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ConfigurableApplicationContext;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Paths;
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
    CasGitServiceRegistryAutoConfiguration.class,
    AbstractServiceRegistryTests.SharedTestConfiguration.class
},
    properties = {
        "cas.service-registry.git.sign-commits=false",
        "cas.service-registry.git.root-directory=svc-cfg",
        "cas.service-registry.git.repository-url=file://${java.io.tmpdir}/cas-sample-data"
    })
@Slf4j
@Tag("Git")
@ExtendWith(CasTestExtension.class)
@Getter
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class GitServiceRegistryTests extends AbstractServiceRegistryTests {

    @Autowired
    private ConfigurableApplicationContext applicationContext;

    @Autowired
    @Qualifier("gitServiceRegistryRepositoryInstance")
    private GitRepository gitRepositoryInstance;

    @Autowired
    @Qualifier(ServiceRegistry.BEAN_NAME)
    private ServiceRegistry newServiceRegistry;

    @BeforeAll
    public static void setup() {
        try {
            val gitRepoSampleDir = new File(FileUtils.getTempDirectory(), "cas-sample-data");
            if (gitRepoSampleDir.exists()) {
                FunctionUtils.doAndHandle(
                    _ -> PathUtils.deleteDirectory(gitRepoSampleDir.toPath(), StandardDeleteOption.OVERRIDE_READ_ONLY));
            }
            val gitDir = new File(FileUtils.getTempDirectory(), GitServiceRegistryProperties.DEFAULT_CAS_SERVICE_REGISTRY_NAME);
            if (gitDir.exists()) {
                FunctionUtils.doAndHandle(
                    _ -> PathUtils.deleteDirectory(gitDir.toPath(), StandardDeleteOption.OVERRIDE_READ_ONLY));
            }
            try (val gitSampleRepo = Git.init().setDirectory(gitRepoSampleDir).setBare(false).call()) {
                FileUtils.write(new File(gitRepoSampleDir, "readme.txt"), "text", StandardCharsets.UTF_8);
                gitSampleRepo.add().addFilepattern("*.txt").call();
                gitSampleRepo.commit().setSign(false).setMessage("Initial commit").call();
            }

            try (val git = Git.init().setDirectory(gitDir).setBare(false).call()) {
                FileUtils.write(new File(gitDir, "readme.txt"), "text", StandardCharsets.UTF_8);
                git.add().addFilepattern("*.txt").call();
                git.commit().setSign(false).setMessage("Initial commit").call();
            }
        } catch (final Exception e) {
            LoggingUtils.error(LOGGER, e);
            fail(e.getMessage(), e);
        }
    }

    @AfterAll
    public static void cleanUp() {
        try {
            val gitRepoDir = new File(FileUtils.getTempDirectory(), "cas-sample-data");
            FunctionUtils.doAndHandle(
                _ -> PathUtils.deleteDirectory(gitRepoDir.toPath(), StandardDeleteOption.OVERRIDE_READ_ONLY));
            val gitDir = new File(FileUtils.getTempDirectory(), GitServiceRegistryProperties.DEFAULT_CAS_SERVICE_REGISTRY_NAME);
            if (gitDir.exists()) {
                FunctionUtils.doAndHandle(
                    _ -> PathUtils.deleteDirectory(gitDir.toPath(), StandardDeleteOption.OVERRIDE_READ_ONLY));
            }
        } catch (final Exception e) {
            LoggingUtils.error(LOGGER, e);
        }
    }

    @Test
    void verifyMalformedJsonFile() throws Throwable {
        val gitDir = new File(FileUtils.getTempDirectory(), GitServiceRegistryProperties.DEFAULT_CAS_SERVICE_REGISTRY_NAME);

        FileUtils.write(Paths.get(gitDir.getAbsolutePath(), "svc-cfg", CasRegisteredService.FRIENDLY_NAME,
            "malformed-1.json").normalize().toFile(), "{\"@class\":\"xxxx\"", StandardCharsets.UTF_8);
        gitRepositoryInstance.commitAll("Malformed json file");

        val svc = buildRegisteredServiceInstance(RandomUtils.nextLong(), CasRegisteredService.class);
        svc.setId(RegisteredServiceDefinition.INITIAL_IDENTIFIER_VALUE);
        newServiceRegistry.save(svc);

        val size = newServiceRegistry.load().size();
        assertEquals(1, size);
    }

    @Test
    void verifyPullFails() throws Throwable {
        val gitRepository = mock(GitRepository.class);
        when(gitRepository.getObjectsInRepository()).thenThrow(new JGitInternalException("error"));
        when(gitRepository.getObjectsInRepository(any())).thenThrow(new JGitInternalException("error"));
        when(gitRepository.getRepositoryDirectory()).thenReturn(gitRepositoryInstance.getRepositoryDirectory());

        val svc = buildRegisteredServiceInstance(RandomUtils.nextLong(), CasRegisteredService.class);
        svc.setId(RegisteredServiceDefinition.INITIAL_IDENTIFIER_VALUE);
        newServiceRegistry.save(svc);
        val size = newServiceRegistry.load().size();

        val registry = new GitServiceRegistry(applicationContext, gitRepository,
            CollectionUtils.wrapList(
                new RegisteredServiceJsonSerializer(applicationContext),
                new RegisteredServiceYamlSerializer(applicationContext)),
            false, null, List.of(), List.of());
        assertEquals(size, registry.load().size());
    }

    /**
     * Validate that load doesn't find services at root of git repo (or sub-directories other than the root-directory).
     * Second service is copied to two other locations and deleted in order to commit all changes to the repository.
     */
    @Test
    void verifyLoadWithRootDirectory() throws IOException {
        val svc = buildRegisteredServiceInstance(RandomUtils.nextLong(), CasRegisteredService.class);
        val svc2 = buildRegisteredServiceInstance(RandomUtils.nextLong(), CasRegisteredService.class);
        svc.setId(RegisteredServiceDefinition.INITIAL_IDENTIFIER_VALUE);
        svc2.setId(RegisteredServiceDefinition.INITIAL_IDENTIFIER_VALUE);
        newServiceRegistry.save(svc);
        newServiceRegistry.save(svc2);
        val size = newServiceRegistry.load().size();
        assertEquals(2, size);
        val gitDir = new File(FileUtils.getTempDirectory(), GitServiceRegistryProperties.DEFAULT_CAS_SERVICE_REGISTRY_NAME);
        val rootDir = new File(gitDir, "svc-cfg");
        val clientDir = new File(rootDir, CasRegisteredService.FRIENDLY_NAME);
        val svc2FileName = svc2.getName() + '-' + svc2.getId() + ".json";
        val svc2File = new File(clientDir, svc2FileName);
        val anotherRootDir = new File(gitDir, "svc-cfg2");
        FileUtils.copyFile(svc2File, new File(gitDir, svc2FileName));
        FileUtils.copyFile(svc2File, new File(anotherRootDir, svc2FileName));
        newServiceRegistry.delete(svc2);
        val size2 = newServiceRegistry.load().size();
        assertEquals(1, size2);
    }
}
