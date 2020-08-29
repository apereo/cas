package org.apereo.cas.support.saml.metadata.resolver;

import org.apereo.cas.support.saml.BaseGitSamlMetadataTests;
import org.apereo.cas.support.saml.services.SamlRegisteredService;
import org.apereo.cas.support.saml.services.idp.metadata.SamlMetadataDocument;
import org.apereo.cas.util.LoggingUtils;

import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.eclipse.jgit.api.Git;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.core.io.ClassPathResource;
import org.springframework.test.context.TestPropertySource;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link GitSamlRegisteredServiceMetadataResolverTests}.
 *
 * @author Misagh Moayyed
 * @since 6.3.0
 */
@TestPropertySource(properties = {
    "cas.authn.saml-idp.metadata.git.sign-commits=false",
    "cas.authn.saml-idp.metadata.git.repository-url=file:/tmp/cas-metadata-data.git"
})
@Slf4j
@Tag("FileSystem")
public class GitSamlRegisteredServiceMetadataResolverTests extends BaseGitSamlMetadataTests {
    @BeforeAll
    public static void setup() {
        try {
            FileUtils.deleteDirectory(new File("/tmp/cas-metadata-data"));
            val gitDir = new File(FileUtils.getTempDirectory(), "cas-saml-metadata");
            if (gitDir.exists()) {
                FileUtils.deleteDirectory(gitDir);
            }
            if (!gitDir.mkdir()) {
                throw new IllegalArgumentException("Git repository directory location " + gitDir + " cannot be located/created");
            }
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
        FileUtils.deleteDirectory(new File("/tmp/cas-metadata-data"));
        val gitDir = new File(FileUtils.getTempDirectory(), "cas-saml-metadata");
        if (gitDir.exists()) {
            FileUtils.deleteDirectory(gitDir);
        }
    }

    @Test
    public void verifyResolver() throws IOException {
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
    }
}
