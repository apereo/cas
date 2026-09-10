package org.apereo.cas.support.saml.metadata.resolver;

import module java.base;
import org.apereo.cas.support.saml.BaseDynamoDbSamlMetadataTests;
import org.apereo.cas.support.saml.services.SamlRegisteredService;
import org.apereo.cas.support.saml.services.idp.metadata.SamlMetadataDocument;
import org.apereo.cas.util.junit.EnabledIfListeningOnPort;
import lombok.val;
import net.shibboleth.shared.resolver.CriteriaSet;
import org.apache.commons.io.IOUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.parallel.Execution;
import org.junit.jupiter.api.parallel.ExecutionMode;
import org.junitpioneer.jupiter.RetryingTest;
import org.opensaml.core.criterion.EntityIdCriterion;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.ClassPathResource;
import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link DynamoDbSamlRegisteredServiceMetadataResolverTests}.
 *
 * @author Misagh Moayyed
 * @since 7.2.0
 */
@Tag("DynamoDb")
@EnabledIfListeningOnPort(port = 8000)
@Execution(ExecutionMode.SAME_THREAD)
class DynamoDbSamlRegisteredServiceMetadataResolverTests extends BaseDynamoDbSamlMetadataTests {
    @BeforeEach
    void setup() {
        resolver.getMetadataManager().orElseThrow().removeAll();
    }

    @Test
    void verifyResolver() throws Throwable {
        val res = new ClassPathResource("sp-metadata.xml");
        var md = new SamlMetadataDocument();
        md.setName("SP");
        md.setValue(IOUtils.toString(res.getInputStream(), StandardCharsets.UTF_8));
        val metadataManager = resolver.getMetadataManager().orElseThrow();
        metadataManager.store(md);

        val service = new SamlRegisteredService();
        service.setName("SAML Service");
        service.setServiceId("https://carmenwiki.osu.edu/shibboleth");
        service.setDescription("Testing");
        service.setMetadataLocation("dynamodb://");
        assertTrue(resolver.supports(service));
        assertTrue(resolver.isAvailable(service));
        val resolvers = resolver.resolve(service);
        assertEquals(1, resolvers.size());
    }

    @Test
    void verifyFailsResolver() throws Throwable {
        val res = new ByteArrayResource("bad-data".getBytes(StandardCharsets.UTF_8));
        val md = new SamlMetadataDocument();
        md.setName("SP");
        md.setEntityId("https://carmenwiki.osu.edu/shibboleth");
        md.setValue(IOUtils.toString(res.getInputStream(), StandardCharsets.UTF_8));
        val metadataManager = resolver.getMetadataManager().orElseThrow();
        metadataManager.store(md);

        val service = new SamlRegisteredService();
        service.setName("SAML Service");
        service.setServiceId("https://carmenwiki.osu.edu/shibboleth");
        val resolvers = resolver.resolve(service);
        assertTrue(resolvers.isEmpty());
    }

    @RetryingTest(3)
    void verifyEntityIdCriterionSelectsMetadataDocument() throws Throwable {
        val entityId = "https://carmenwiki.osu.edu/shibboleth";
        val metadata = IOUtils.toString(new ClassPathResource("sp-metadata.xml").getInputStream(), StandardCharsets.UTF_8);
        val metadataManager = resolver.getMetadataManager().orElseThrow();
        metadataManager.store(SamlMetadataDocument.builder().name("SP").value(metadata).build());
        metadataManager.store(SamlMetadataDocument.builder().name("Other")
            .value(metadata.replace(entityId, "https://other.example.org")).build());

        val service = new SamlRegisteredService();
        service.setName("SAML Service");
        service.setServiceId("^https://.+$");
        service.setMetadataLocation("dynamodb://");
        val resolvers = resolver.resolve(service, new CriteriaSet(new EntityIdCriterion(entityId)));
        assertEquals(1, resolvers.size());
    }

    @Test
    void verifyResolverDoesNotSupport() {
        assertFalse(resolver.supports(null));
    }
}
