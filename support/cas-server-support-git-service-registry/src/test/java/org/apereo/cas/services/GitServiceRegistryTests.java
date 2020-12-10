package org.apereo.cas.services;

import org.apereo.cas.config.CasCoreAuthenticationMetadataConfiguration;
import org.apereo.cas.config.CasCoreNotificationsConfiguration;
import org.apereo.cas.config.CasCoreServicesConfiguration;
import org.apereo.cas.config.CasCoreUtilConfiguration;
import org.apereo.cas.config.GitServiceRegistryConfiguration;
import org.apereo.cas.configuration.model.support.git.services.GitServiceRegistryProperties;
import org.apereo.cas.util.LoggingUtils;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.io.FileUtils;
import org.eclipse.jgit.api.Git;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.mail.MailSenderAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.autoconfigure.RefreshAutoConfiguration;

import java.io.File;
import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.*;

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

    @Value("${tmpdir:/tmp}")
    private static String TMPDIR;

    @Autowired
    @Qualifier("serviceRegistry")
    private ServiceRegistry newServiceRegistry;

    @BeforeAll
    public static void setup() {
        try {
            val gitRepoSampleDir = new File(TMPDIR +"/cas-sample-data");
            if (gitRepoSampleDir.exists()) {
                deleteDirectory(gitRepoSampleDir);
                FileUtils.deleteDirectory(gitRepoSampleDir);
            }
            val gitDir = new File(FileUtils.getTempDirectory(), GitServiceRegistryProperties.DEFAULT_CAS_SERVICE_REGISTRY_NAME);
            if (gitDir.exists()) {
                deleteDirectory(gitDir);
                FileUtils.deleteDirectory(gitDir);
            }
            val gitSampleRepo = Git.init().setDirectory(gitDir).setBare(false).call();
            FileUtils.write(new File(gitDir, "readme.txt"), "text", StandardCharsets.UTF_8);
            gitSampleRepo.commit().setSign(false).setMessage("Initial commit").call();

            val git = Git.init().setDirectory(gitDir).setBare(false).call();
            FileUtils.write(new File(gitDir, "readme.txt"), "text", StandardCharsets.UTF_8);
            git.commit().setSign(false).setMessage("Initial commit").call();
        } catch (final Exception e) {
            LoggingUtils.error(LOGGER, e);
            fail(e.getMessage(), e);
        }
    }

    @AfterAll
    public static void cleanUp() throws Exception {
        FileUtils.deleteDirectory(new File(TMPDIR +"/cas-sample-data"));
        val gitDir = new File(FileUtils.getTempDirectory(), GitServiceRegistryProperties.DEFAULT_CAS_SERVICE_REGISTRY_NAME);
        if (gitDir.exists()) {
            deleteDirectory(gitDir);
            FileUtils.deleteDirectory(gitDir);
        }
    }

    /**
     * Extra deleteDirectory method b/c FileUtils.deleteDirectory wasn't working reliably on Windows.
     * @param path path to folder to be recursively deleted
     * @return
     */
    private static boolean deleteDirectory(final File path) {
        if (path.exists()) {
            File[] files = path.listFiles();
            for (int i = 0; i < files.length; i++) {
                if (files[i].isDirectory()) {
                    deleteDirectory(files[i]);
                } else {
                    files[i].delete();
                }
            }
        }
        return path.delete();
    }
}
