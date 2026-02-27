package org.apereo.cas.support.saml.metadata.resolver;

import module java.base;
import org.apereo.cas.support.saml.BaseJpaSamlMetadataTests;
import org.apereo.cas.support.saml.services.SamlRegisteredService;
import org.apereo.cas.support.saml.services.idp.metadata.SamlMetadataDocument;
import lombok.val;
import org.apache.commons.io.IOUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junitpioneer.jupiter.RetryingTest;
import org.springframework.core.io.ClassPathResource;
import org.springframework.transaction.annotation.Transactional;
import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link JpaSamlRegisteredServiceMetadataResolverTests}.
 *
 * @author Misagh Moayyed
 * @since 5.2.0
 */
@Tag("JDBC")
@Transactional(transactionManager = "transactionManagerSamlMetadata")
class JpaSamlRegisteredServiceMetadataResolverTests extends BaseJpaSamlMetadataTests {

    @BeforeEach
    void setup() {
        val metadataManager = resolver.getMetadataManager().orElseThrow();
        metadataManager.removeAll();
    }

    @RetryingTest(3)
    void verifyResolver() throws Throwable {
        val res = new ClassPathResource("samlsp-metadata.xml");
        var md = new SamlMetadataDocument();
        md.setName("SP");
        md.setValue(IOUtils.toString(res.getInputStream(), StandardCharsets.UTF_8));
        val metadataManager = resolver.getMetadataManager().orElseThrow();
        md = metadataManager.store(md);

        val service = new SamlRegisteredService();
        service.setName("SAML Service");
        service.setServiceId("https://carmenwiki.osu.edu/shibboleth");
        service.setDescription("Testing");
        service.setMetadataLocation("jdbc://");
        assertTrue(resolver.supports(service));
        assertFalse(resolver.supports(null));
        assertTrue(resolver.isAvailable(service));
        val resolvers = resolver.resolve(service);
        assertEquals(1, resolvers.size());

        service.setMetadataLocation("whatever");
        assertFalse(resolver.supports(service));
    }

    @Test
    void verifyLoad() throws Throwable {
        val metadataManager = resolver.getMetadataManager().orElseThrow();
        assertTrue(metadataManager.load().isEmpty());

        val res = new ClassPathResource("samlsp-metadata.xml");
        var md = new SamlMetadataDocument();
        md.setName("SP");
        md.setValue(IOUtils.toString(res.getInputStream(), StandardCharsets.UTF_8));
        metadataManager.store(md);

        val documents = metadataManager.load();
        assertEquals(1, documents.size());
        assertEquals("SP", documents.getFirst().getName());
    }

    @Test
    void verifyFindById() throws Throwable {
        val metadataManager = resolver.getMetadataManager().orElseThrow();
        val res = new ClassPathResource("samlsp-metadata.xml");
        var md = new SamlMetadataDocument();
        md.setName("SP");
        md.setValue(IOUtils.toString(res.getInputStream(), StandardCharsets.UTF_8));
        val storedDocument = metadataManager.store(md);

        val found = metadataManager.findById(storedDocument.getId());
        assertTrue(found.isPresent());
        assertEquals(storedDocument.getName(), found.get().getName());
        assertTrue(metadataManager.findById(-999).isEmpty());
    }

    @Test
    void verifyFindByName() throws Throwable {
        val metadataManager = resolver.getMetadataManager().orElseThrow();
        val res = new ClassPathResource("samlsp-metadata.xml");
        var md = new SamlMetadataDocument();
        md.setName("SP");
        md.setValue(IOUtils.toString(res.getInputStream(), StandardCharsets.UTF_8));
        metadataManager.store(md);

        val found = metadataManager.findByName("SP");
        assertTrue(found.isPresent());
        assertEquals("SP", found.get().getName());
        assertTrue(metadataManager.findByName("Unknown").isEmpty());
    }

    @Test
    void verifyRemoveById() throws Throwable {
        val metadataManager = resolver.getMetadataManager().orElseThrow();
        val res = new ClassPathResource("samlsp-metadata.xml");
        var md = new SamlMetadataDocument();
        md.setName("SP");
        md.setValue(IOUtils.toString(res.getInputStream(), StandardCharsets.UTF_8));
        val storedDocument = metadataManager.store(md);

        metadataManager.removeById(storedDocument.getId());
        assertTrue(metadataManager.findById(storedDocument.getId()).isEmpty());
        assertTrue(metadataManager.load().isEmpty());
    }

    @Test
    void verifyRemoveByName() throws Throwable {
        val metadataManager = resolver.getMetadataManager().orElseThrow();
        val res = new ClassPathResource("samlsp-metadata.xml");
        var md = new SamlMetadataDocument();
        md.setName("SP");
        md.setValue(IOUtils.toString(res.getInputStream(), StandardCharsets.UTF_8));
        metadataManager.store(md);

        metadataManager.removeByName("SP");
        assertTrue(metadataManager.findByName("SP").isEmpty());
        assertTrue(metadataManager.load().isEmpty());
    }
}
