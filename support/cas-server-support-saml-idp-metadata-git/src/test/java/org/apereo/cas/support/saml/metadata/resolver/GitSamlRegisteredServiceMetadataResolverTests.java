package org.apereo.cas.support.saml.metadata.resolver;

import module java.base;
import org.apereo.cas.configuration.model.support.saml.idp.SamlIdPProperties;
import org.apereo.cas.git.GitRepository;
import org.apereo.cas.support.saml.BaseGitSamlMetadataTests;
import org.apereo.cas.support.saml.OpenSamlConfigBean;
import org.apereo.cas.support.saml.services.SamlRegisteredService;
import org.apereo.cas.support.saml.services.idp.metadata.SamlMetadataDocument;
import org.apereo.cas.util.LoggingUtils;
import org.apereo.cas.util.function.FunctionUtils;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import net.shibboleth.shared.resolver.CriteriaSet;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.io.file.PathUtils;
import org.apache.commons.io.file.StandardDeleteOption;
import org.eclipse.jgit.api.Git;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.parallel.ResourceLock;
import org.opensaml.core.criterion.EntityIdCriterion;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.io.ClassPathResource;
import org.springframework.test.context.TestPropertySource;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

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
    "cas.authn.saml-idp.metadata.git.clone-directory.location=file://${java.io.tmpdir}/cas-saml-metadata-cloned"
})
@Slf4j
@Tag("Git")
@ResourceLock("gitSamlMetadataRepository")
class GitSamlRegisteredServiceMetadataResolverTests extends BaseGitSamlMetadataTests {
    @Autowired
    @Qualifier("gitSamlRegisteredServiceRepositoryScheduler")
    private Runnable gitSamlRegisteredServiceRepositoryScheduler;

    @Autowired
    @Qualifier(OpenSamlConfigBean.DEFAULT_BEAN_NAME)
    private OpenSamlConfigBean openSamlConfigBean;

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
                _ -> PathUtils.deleteDirectory(gitRepoDir.toPath(), StandardDeleteOption.OVERRIDE_READ_ONLY));
        }
        val cloneDirectory = "cas-saml-metadata-cloned";
        val gitCloneRepoDir = new File(FileUtils.getTempDirectory(), cloneDirectory);
        if (gitCloneRepoDir.exists()) {
            FunctionUtils.doAndHandle(
                _ -> PathUtils.deleteDirectory(gitCloneRepoDir.toPath(), StandardDeleteOption.OVERRIDE_READ_ONLY));
        }
    }

    @Test
    void verifyResolver() throws Throwable {
        var md = new SamlMetadataDocument();
        md.setName("SP");
        md.setValue(IOUtils.toString(new ClassPathResource("sp-metadata.xml").getInputStream(), StandardCharsets.UTF_8));
        md.setSignature(IOUtils.toString(new ClassPathResource("cert.pem").getInputStream(), StandardCharsets.UTF_8));
        val metadataManager = resolver.getMetadataManager().orElseThrow();
        metadataManager.store(md);

        val service = new SamlRegisteredService();
        service.setName("SAML Service");
        service.setServiceId("https://carmenwiki.osu.edu/shibboleth");
        service.setDescription("Testing");
        service.setMetadataLocation("git://");
        assertTrue(resolver.supports(service));
        assertTrue(resolver.isAvailable(service));
        assertFalse(resolver.supports(null));
        assertTrue(resolver.resolve(service).isEmpty());
        service.setRequireSignedRoot(false);
        val resolvers = resolver.resolve(service);
        assertFalse(resolvers.isEmpty());
        service.setMetadataLocation("https://example.com/endswith.git");
        assertTrue(resolver.supports(service));

        assertDoesNotThrow(() -> {
            gitSamlRegisteredServiceRepositoryScheduler.run();
            resolver.resolve(null, null);
            metadataManager.store(null);
        });
    }

    @Test
    void verifyEntityIdCriterionSelectsMetadataDocument() throws Throwable {
        val entityId = "https://carmenwiki.osu.edu/shibboleth";
        val metadata = IOUtils.toString(new ClassPathResource("sp-metadata.xml").getInputStream(), StandardCharsets.UTF_8);
        val signature = IOUtils.toString(new ClassPathResource("cert.pem").getInputStream(), StandardCharsets.UTF_8);
        val metadataManager = resolver.getMetadataManager().orElseThrow();
        metadataManager.removeAll();
        metadataManager.store(SamlMetadataDocument.builder().name("SP").value(metadata).signature(signature).build());
        metadataManager.store(SamlMetadataDocument.builder().name("Other").signature(signature)
            .value(metadata.replace(entityId, "https://other.example.org")).build());

        val service = new SamlRegisteredService();
        service.setName("SAML Service");
        service.setServiceId("^https://.+$");
        service.setMetadataLocation("git://");
        service.setRequireSignedRoot(false);
        val resolvers = resolver.resolve(service, new CriteriaSet(new EntityIdCriterion(entityId)));
        assertEquals(1, resolvers.size());
    }

    @Test
    void verifyPullsAreThrottled() throws Throwable {
        val repositoryDirectory = Files.createTempDirectory("git-saml-metadata").toFile();
        val metadataDirectory = new File(repositoryDirectory, "sp-metadata");
        FileUtils.forceMkdir(metadataDirectory);
        val metadata = IOUtils.toString(new ClassPathResource("sp-metadata.xml").getInputStream(), StandardCharsets.UTF_8);
        FileUtils.writeStringToFile(new File(metadataDirectory, "1-SP.xml"), metadata, StandardCharsets.UTF_8);
        FileUtils.writeStringToFile(new File(metadataDirectory, "2-Other.xml"),
            metadata.replace("https://carmenwiki.osu.edu/shibboleth", "https://other.example.org"), StandardCharsets.UTF_8);

        val gitRepository = mock(GitRepository.class);
        when(gitRepository.getRepositoryDirectory()).thenReturn(repositoryDirectory);
        when(gitRepository.pull()).thenReturn(Boolean.TRUE);
        val properties = new SamlIdPProperties();
        properties.getMetadata().getGit().getSchedule().setRepeatInterval("PT1H");
        val metadataResolver = new GitSamlRegisteredServiceMetadataResolver(properties, openSamlConfigBean, gitRepository);

        val service = new SamlRegisteredService();
        service.setName("SAML Service");
        service.setServiceId("^https://.+$");
        service.setMetadataLocation("git://");
        service.setRequireSignedRoot(false);
        val criteria = new CriteriaSet(new EntityIdCriterion("https://carmenwiki.osu.edu/shibboleth"));
        for (var i = 0; i < 3; i++) {
            assertEquals(1, metadataResolver.resolve(service, criteria).size());
        }
        verify(gitRepository, times(1)).pull();
    }

    @Test
    void verifyLoad() throws Throwable {
        val metadataManager = resolver.getMetadataManager().orElseThrow();
        metadataManager.removeAll();
        assertTrue(metadataManager.load().isEmpty());

        var md = new SamlMetadataDocument();
        md.setName("SP");
        md.setValue(IOUtils.toString(new ClassPathResource("sp-metadata.xml").getInputStream(), StandardCharsets.UTF_8));
        md.setSignature(IOUtils.toString(new ClassPathResource("cert.pem").getInputStream(), StandardCharsets.UTF_8));
        metadataManager.store(md);

        val documents = metadataManager.load();
        assertEquals(1, documents.size());
        assertEquals("SP", documents.getFirst().getName());
    }

    @Test
    void verifyFindByName() throws Throwable {
        val metadataManager = resolver.getMetadataManager().orElseThrow();
        metadataManager.removeAll();

        var md = new SamlMetadataDocument();
        md.setName("SP");
        md.setValue(IOUtils.toString(new ClassPathResource("sp-metadata.xml").getInputStream(), StandardCharsets.UTF_8));
        md.setSignature(IOUtils.toString(new ClassPathResource("cert.pem").getInputStream(), StandardCharsets.UTF_8));
        metadataManager.store(md);

        val found = metadataManager.findByName("SP");
        assertTrue(found.isPresent());
        assertEquals("SP", found.get().getName());
        assertTrue(metadataManager.findByName("Unknown").isEmpty());
    }

    @Test
    void verifyFindById() throws Throwable {
        val metadataManager = resolver.getMetadataManager().orElseThrow();
        metadataManager.removeAll();

        var md = new SamlMetadataDocument();
        md.setName("SP");
        md.setValue(IOUtils.toString(new ClassPathResource("sp-metadata.xml").getInputStream(), StandardCharsets.UTF_8));
        md.setSignature(IOUtils.toString(new ClassPathResource("cert.pem").getInputStream(), StandardCharsets.UTF_8));
        metadataManager.store(md);

        val loaded = metadataManager.findByName("SP").orElseThrow();
        val found = metadataManager.findById(loaded.getId());
        assertTrue(found.isPresent());
        assertEquals("SP", found.get().getName());
        assertTrue(metadataManager.findById(-999).isEmpty());
    }

    @Test
    void verifyRemoveByName() throws Throwable {
        val metadataManager = resolver.getMetadataManager().orElseThrow();
        metadataManager.removeAll();

        var md = new SamlMetadataDocument();
        md.setName("SP");
        md.setValue(IOUtils.toString(new ClassPathResource("sp-metadata.xml").getInputStream(), StandardCharsets.UTF_8));
        md.setSignature(IOUtils.toString(new ClassPathResource("cert.pem").getInputStream(), StandardCharsets.UTF_8));
        metadataManager.store(md);

        metadataManager.removeByName("SP");
        assertTrue(metadataManager.findByName("SP").isEmpty());
        assertTrue(metadataManager.load().isEmpty());
    }

    @Test
    void verifyRemoveById() throws Throwable {
        val metadataManager = resolver.getMetadataManager().orElseThrow();
        metadataManager.removeAll();

        var md = new SamlMetadataDocument();
        md.setName("SP");
        md.setValue(IOUtils.toString(new ClassPathResource("sp-metadata.xml").getInputStream(), StandardCharsets.UTF_8));
        md.setSignature(IOUtils.toString(new ClassPathResource("cert.pem").getInputStream(), StandardCharsets.UTF_8));
        metadataManager.store(md);

        val loaded = metadataManager.findByName("SP").orElseThrow();
        metadataManager.removeById(loaded.getId());
        assertTrue(metadataManager.findByName("SP").isEmpty());
        assertTrue(metadataManager.load().isEmpty());
    }

    @Test
    void verifyDocumentsCommittedByHand() throws Throwable {
        val metadataManager = resolver.getMetadataManager().orElseThrow();
        metadataManager.removeAll();
        val metadata = IOUtils.toString(new ClassPathResource("sp-metadata.xml").getInputStream(), StandardCharsets.UTF_8);
        val metadataDirectory = new File(FileUtils.getTempDirectory(), "cas-saml-metadata-cloned/sp-metadata");
        FileUtils.writeStringToFile(new File(metadataDirectory, "SP.xml"), metadata, StandardCharsets.UTF_8);
        FileUtils.writeStringToFile(new File(metadataDirectory, "SP.pem"), "signature", StandardCharsets.UTF_8);
        FileUtils.writeStringToFile(new File(metadataDirectory, "default-sp.xml"), metadata, StandardCharsets.UTF_8);

        val documents = metadataManager.load();
        assertEquals(Set.of("SP", "default-sp"), documents.stream().map(SamlMetadataDocument::getName).collect(Collectors.toSet()));
        val document = metadataManager.findByName("SP").orElseThrow();
        assertEquals("signature", document.getSignature());
        assertEquals(document.getId(), metadataManager.findByName("SP").orElseThrow().getId());
        assertTrue(metadataManager.findById(document.getId()).isPresent());

        metadataManager.removeById(document.getId());
        assertTrue(metadataManager.findByName("SP").isEmpty());
        assertFalse(new File(metadataDirectory, "SP.xml").exists());
        assertFalse(new File(metadataDirectory, "SP.pem").exists());
        assertTrue(metadataManager.findByName("default-sp").isPresent());
        metadataManager.removeByName("default-sp");
        assertTrue(metadataManager.load().isEmpty());
    }

    @Test
    void verifyRemoveAll() throws Throwable {
        val metadataManager = resolver.getMetadataManager().orElseThrow();
        metadataManager.removeAll();

        val md = new SamlMetadataDocument();
        md.setName("SP");
        md.setValue(IOUtils.toString(new ClassPathResource("sp-metadata.xml").getInputStream(), StandardCharsets.UTF_8));
        md.setSignature(IOUtils.toString(new ClassPathResource("cert.pem").getInputStream(), StandardCharsets.UTF_8));
        metadataManager.store(md);
        assertFalse(metadataManager.load().isEmpty());

        metadataManager.removeAll();
        assertTrue(metadataManager.load().isEmpty());
    }
}
