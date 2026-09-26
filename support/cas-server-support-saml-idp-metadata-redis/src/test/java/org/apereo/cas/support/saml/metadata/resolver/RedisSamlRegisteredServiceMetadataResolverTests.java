package org.apereo.cas.support.saml.metadata.resolver;

import module java.base;
import org.apereo.cas.support.saml.BaseRedisSamlMetadataTests;
import org.apereo.cas.support.saml.services.SamlRegisteredService;
import org.apereo.cas.support.saml.services.idp.metadata.SamlMetadataDocument;
import org.apereo.cas.util.RandomUtils;
import org.apereo.cas.util.junit.EnabledIfListeningOnPort;
import lombok.val;
import net.shibboleth.shared.resolver.CriteriaSet;
import org.apache.commons.io.IOUtils;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.opensaml.core.criterion.EntityIdCriterion;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.ClassPathResource;
import org.springframework.test.context.TestPropertySource;
import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link RedisSamlRegisteredServiceMetadataResolverTests}.
 *
 * @author Misagh Moayyed
 * @since 6.4.0
 */
@TestPropertySource(properties = {
    "cas.authn.saml-idp.metadata.redis.host=localhost",
    "cas.authn.saml-idp.metadata.redis.port=6379",
    "cas.authn.saml-idp.metadata.file-system.location=file:/tmp"
})
@Tag("Redis")
@EnabledIfListeningOnPort(port = 6379)
class RedisSamlRegisteredServiceMetadataResolverTests extends BaseRedisSamlMetadataTests {
    private static final String DEFAULT_ENTITY_ID = "https://carmenwiki.osu.edu/shibboleth";

    private static String randomEntityId() {
        return "https://%s.example.org/shibboleth".formatted(RandomUtils.randomAlphabetic(8));
    }

    private static SamlMetadataDocument documentFor(final String entityId) throws Exception {
        val metadata = IOUtils.toString(new ClassPathResource("sp-metadata.xml").getInputStream(), StandardCharsets.UTF_8);
        return SamlMetadataDocument.builder()
            .name(RandomUtils.randomAlphabetic(8))
            .value(metadata.replace(DEFAULT_ENTITY_ID, entityId))
            .build();
    }

    @Test
    void verifyResolver() throws Throwable {
        val entityId = randomEntityId();
        val metadataManager = resolver.getMetadataManager().orElseThrow();
        metadataManager.store(documentFor(entityId));

        val service = new SamlRegisteredService();
        service.setName("SAML Service");
        service.setServiceId("^https://.+$");
        service.setDescription("Testing");
        service.setMetadataLocation("redis://");
        assertTrue(resolver.supports(service));
        assertTrue(resolver.isAvailable(service));
        assertFalse(resolver.resolve(service).isEmpty());
        val resolvers = resolver.resolve(service, new CriteriaSet(new EntityIdCriterion(entityId)));
        assertEquals(1, resolvers.size());
    }

    @Test
    void verifyFailsResolver() throws Throwable {
        val entityId = randomEntityId();
        val res = new ByteArrayResource("bad-data".getBytes(StandardCharsets.UTF_8));
        val md = new SamlMetadataDocument();
        md.setName(RandomUtils.randomAlphabetic(8));
        md.setEntityId(entityId);
        md.setValue(IOUtils.toString(res.getInputStream(), StandardCharsets.UTF_8));
        val metadataManager = resolver.getMetadataManager().orElseThrow();
        metadataManager.store(md);

        val service = new SamlRegisteredService();
        service.setName("SAML Service");
        service.setServiceId(entityId);
        val resolvers = resolver.resolve(service, new CriteriaSet(new EntityIdCriterion(entityId)));
        assertTrue(resolvers.isEmpty());
    }

    @Test
    void verifyEntityIdCriterionSelectsMetadataDocument() throws Throwable {
        val entityId = randomEntityId();
        val metadataManager = resolver.getMetadataManager().orElseThrow();
        metadataManager.store(documentFor(entityId));
        metadataManager.store(documentFor(randomEntityId()));

        val service = new SamlRegisteredService();
        service.setName("SAML Service");
        service.setServiceId("^https://.+$");
        service.setMetadataLocation("redis://");
        val resolvers = resolver.resolve(service, new CriteriaSet(new EntityIdCriterion(entityId)));
        assertEquals(1, resolvers.size());
    }

    @Test
    void verifyResolverDoesNotSupport() {
        assertFalse(resolver.supports(null));
    }

    @Test
    void verifyLoad() throws Throwable {
        val metadataManager = resolver.getMetadataManager().orElseThrow();
        val md = documentFor(randomEntityId());
        assertTrue(metadataManager.load().stream().noneMatch(doc -> md.getName().equals(doc.getName())));
        metadataManager.store(md);

        val documents = metadataManager.load().stream().filter(doc -> md.getName().equals(doc.getName())).toList();
        assertEquals(1, documents.size());
    }

    @Test
    void verifyFindById() throws Throwable {
        val metadataManager = resolver.getMetadataManager().orElseThrow();
        val storedDocument = metadataManager.store(documentFor(randomEntityId()));

        val found = metadataManager.findById(storedDocument.getId());
        assertTrue(found.isPresent());
        assertEquals(storedDocument.getName(), found.get().getName());
        assertTrue(metadataManager.findById(-999).isEmpty());
    }

    @Test
    void verifyFindByName() throws Throwable {
        val metadataManager = resolver.getMetadataManager().orElseThrow();
        val md = documentFor(randomEntityId());
        metadataManager.store(md);

        val found = metadataManager.findByName(md.getName());
        assertTrue(found.isPresent());
        assertEquals(md.getName(), found.get().getName());
        assertTrue(metadataManager.findByName(UUID.randomUUID().toString()).isEmpty());
    }

    @Test
    void verifyRemoveById() throws Throwable {
        val metadataManager = resolver.getMetadataManager().orElseThrow();
        val storedDocument = metadataManager.store(documentFor(randomEntityId()));

        metadataManager.removeById(storedDocument.getId());
        assertTrue(metadataManager.findById(storedDocument.getId()).isEmpty());
        assertTrue(metadataManager.load().stream().noneMatch(doc -> storedDocument.getName().equals(doc.getName())));
    }

    @Test
    void verifyRemoveByName() throws Throwable {
        val metadataManager = resolver.getMetadataManager().orElseThrow();
        val md = documentFor(randomEntityId());
        metadataManager.store(md);

        metadataManager.removeByName(md.getName());
        assertTrue(metadataManager.findByName(md.getName()).isEmpty());
        assertTrue(metadataManager.load().stream().noneMatch(doc -> md.getName().equals(doc.getName())));
    }
}
