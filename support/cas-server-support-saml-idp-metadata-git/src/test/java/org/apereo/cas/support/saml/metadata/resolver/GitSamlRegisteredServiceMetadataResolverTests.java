package org.apereo.cas.support.saml.metadata.resolver;

import org.apereo.cas.support.saml.BaseGitSamlMetadataTests;
import org.apereo.cas.support.saml.services.SamlRegisteredService;
import org.apereo.cas.support.saml.services.idp.metadata.SamlMetadataDocument;
import org.apereo.cas.util.LoggingUtils;
import org.apereo.cas.util.function.FunctionUtils;

import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.io.file.PathUtils;
import org.apache.commons.io.file.StandardDeleteOption;
import org.eclipse.jgit.api.Git;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.io.ClassPathResource;
import org.springframework.test.context.TestPropertySource;

import java.io.File;
import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link GitSamlRegisteredServiceMetadataResolverTests}.
 *
 * @author Misagh Moayyed
 * @since 6.3.0
 */
@TestPropertySource(properties = {
    "cas.authn.saml-idp.metadata.http.metadata-backup-location=file://${java.io.tmpdir}/metadata-backups",

    "cas.authn.saml-idp.metadata.git.schedule.enabled=true",
    
    "cas.authn.saml-idp.metadata.git.sign-commits=false",
    "cas.authn.saml-idp.metadata.git.push-changes=true",
    "cas.authn.saml-idp.metadata.git.idp-metadata-enabled=true",
    "cas.authn.saml-idp.metadata.git.crypto.enabled=false",
    "cas.authn.saml-idp.metadata.git.repository-url=file://${java.io.tmpdir}/cas-metadata-data",
    "cas.authn.saml-idp.metadata.git.clone-directory.location=file://${java.io.tmpdir}/cas-saml-metadata-gsrsmrt"
})
@Slf4j
@Tag("Git")
class GitSamlRegisteredServiceMetadataResolverTests extends BaseGitSamlMetadataTests {
    @Autowired
    @Qualifier("gitSamlRegisteredServiceRepositoryScheduler")
    private Runnable gitSamlRegisteredServiceRepositoryScheduler;
    
    @BeforeAll
    public static void setup() {
        try {
            cleanUp();
            val gitDir = new File(FileUtils.getTempDirectory(), "cas-metadata-data");
            if (!gitDir.mkdir()) {
                throw new IllegalArgumentException("Git repository directory location " + gitDir + " cannot be located/created");
            }
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
    public static void cleanUp() {
        val gitRepoDir = new File(FileUtils.getTempDirectory(), "cas-metadata-data");
        if (gitRepoDir.exists()) {
            FunctionUtils.doAndHandle(
                __ -> PathUtils.deleteDirectory(gitRepoDir.toPath(), StandardDeleteOption.OVERRIDE_READ_ONLY));
        }
        val cloneDirectory = "cas-saml-metadata-gsrsmrt";
        val gitCloneRepoDir = new File(FileUtils.getTempDirectory(), cloneDirectory);
        if (gitCloneRepoDir.exists()) {
            FunctionUtils.doAndHandle(
                __ -> PathUtils.deleteDirectory(gitCloneRepoDir.toPath(), StandardDeleteOption.OVERRIDE_READ_ONLY));
        }
    }

    @Test
    void verifyResolver() throws Throwable {
        val md = new SamlMetadataDocument();
        md.setName("SP");
        md.setValue(IOUtils.toString(new ClassPathResource("sp-metadata.xml").getInputStream(), StandardCharsets.UTF_8));
        md.setSignature(IOUtils.toString(new ClassPathResource("cert.pem").getInputStream(), StandardCharsets.UTF_8));
        resolver.saveOrUpdate(md);

        val service = new SamlRegisteredService();
        service.setName("SAML Service");
        service.setServiceId("https://carmenwiki.osu.edu/shibboleth");
        service.setDescription("Testing");
        service.setMetadataLocation("git://");
        assertTrue(resolver.supports(service));
        assertTrue(resolver.isAvailable(service));
        assertFalse(resolver.supports(null));
        val resolvers = resolver.resolve(service);
        assertFalse(resolvers.isEmpty());
        service.setMetadataLocation("https://example.com/endswith.git");
        assertTrue(resolver.supports(service));

        assertDoesNotThrow(() -> {
            gitSamlRegisteredServiceRepositoryScheduler.run();
            
            resolver.resolve(null, null);
            resolver.saveOrUpdate(null);

        });
    }
}
