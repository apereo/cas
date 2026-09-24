package org.apereo.cas.support.saml.metadata.resolver;

import module java.base;
import org.apereo.cas.support.saml.BaseMongoDbSamlMetadataTests;
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
 * This is {@link MongoDbSamlRegisteredServiceMetadataResolverTests}.
 *
 * @author Misagh Moayyed
 * @since 5.2.0
 */
@TestPropertySource(properties = {
    "cas.authn.saml-idp.metadata.mongo.database-name=saml-idp-resolver",
    "cas.authn.saml-idp.metadata.mongo.drop-collection=true",
    "cas.authn.saml-idp.metadata.mongo.collection=samlResolver",
    "cas.authn.saml-idp.metadata.mongo.host=localhost",
    "cas.authn.saml-idp.metadata.mongo.port=27017",
    "cas.authn.saml-idp.metadata.mongo.user-id=root",
    "cas.authn.saml-idp.metadata.mongo.password=secret",
    "cas.authn.saml-idp.metadata.mongo.authentication-database-name=admin",
    "cas.authn.saml-idp.metadata.mongo.idp-metadata-collection=saml-idp-metadata-resolver",
    "cas.authn.saml-idp.metadata.file-system.location=file:/tmp"
})
@Tag("MongoDb")
@EnabledIfListeningOnPort(port = 27017)
class MongoDbSamlRegisteredServiceMetadataResolverTests extends BaseMongoDbSamlMetadataTests {
    private static final String DEFAULT_ENTITY_ID = "https://carmenwiki.osu.edu/shibboleth";


    @Test
    void verifyResolver() throws Throwable {
        val entityId = "https://%s.example.org/shibboleth".formatted(RandomUtils.randomAlphabetic(8));
        val metadataManager = resolver.getMetadataManager().orElseThrow();
        val storedDocument = metadataManager.store(documentFor(entityId));

        val service = new SamlRegisteredService();
        service.setName("SAML Service");
        service.setServiceId("^https://.+$");
        service.setDescription("Testing");
        service.setMetadataLocation("mongodb://");
        assertTrue(resolver.supports(service));
        assertTrue(resolver.isAvailable(service));
        val resolvers = resolver.resolve(service, new CriteriaSet(new EntityIdCriterion(entityId)));
        assertEquals(1, resolvers.size());

        metadataManager.removeById(storedDocument.getId());
        metadataManager.removeByName(storedDocument.getName());
        assertTrue(metadataManager.findById(storedDocument.getId()).isEmpty());
        assertTrue(metadataManager.findByName(storedDocument.getName()).isEmpty());
    }

    private SamlMetadataDocument documentFor(final String entityId) throws Exception {
        val metadata = IOUtils.toString(new ClassPathResource("sp-metadata.xml").getInputStream(), StandardCharsets.UTF_8);
        return SamlMetadataDocument.builder()
            .name(RandomUtils.randomAlphabetic(8))
            .value(metadata.replace(DEFAULT_ENTITY_ID, entityId))
            .build();
    }

    @Test
    void verifyFailsResolver() throws Throwable {
        val entityId = "https://%s.example.org/shibboleth".formatted(RandomUtils.randomAlphabetic(8));
        val res = new ByteArrayResource("bad-data".getBytes(StandardCharsets.UTF_8));
        val md = new SamlMetadataDocument();
        md.setName(RandomUtils.randomAlphabetic(8));
        md.setEntityId(entityId);
        md.setValue(IOUtils.toString(res.getInputStream(), StandardCharsets.UTF_8));
        resolver.getMetadataManager().orElseThrow().store(md);

        val service = new SamlRegisteredService();
        service.setName("SAML Service");
        service.setServiceId(entityId);
        val resolvers = resolver.resolve(service, new CriteriaSet(new EntityIdCriterion(entityId)));
        assertTrue(resolvers.isEmpty());
    }

    @Test
    void verifyEntityIdCriterionSelectsMetadataDocument() throws Throwable {
        val entityId = "https://%s.example.org/shibboleth".formatted(RandomUtils.randomAlphabetic(8));
        val otherEntityId = "https://%s.example.org/shibboleth".formatted(RandomUtils.randomAlphabetic(8));
        val metadata = IOUtils.toString(new ClassPathResource("sp-metadata.xml").getInputStream(), StandardCharsets.UTF_8);
        val metadataManager = resolver.getMetadataManager().orElseThrow();
        metadataManager.store(SamlMetadataDocument.builder().name(RandomUtils.randomAlphabetic(8))
            .value(metadata.replace(DEFAULT_ENTITY_ID, entityId)).build());
        metadataManager.store(SamlMetadataDocument.builder().name(RandomUtils.randomAlphabetic(8))
            .value(metadata.replace(DEFAULT_ENTITY_ID, otherEntityId)).build());

        val service = new SamlRegisteredService();
        service.setName("SAML Service");
        service.setServiceId("^https://.+$");
        service.setMetadataLocation("mongodb://");
        val resolvers = resolver.resolve(service, new CriteriaSet(new EntityIdCriterion(entityId)));
        assertEquals(1, resolvers.size());
    }

    @Test
    void verifyResolverDoesNotSupport() {
        assertFalse(resolver.supports(null));
    }
}
