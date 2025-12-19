package org.apereo.cas.support.saml.metadata.resolver;

import module java.base;
import org.apereo.cas.support.saml.BaseDynamoDbSamlMetadataTests;
import org.apereo.cas.support.saml.services.SamlRegisteredService;
import org.apereo.cas.support.saml.services.idp.metadata.SamlMetadataDocument;
import org.apereo.cas.util.junit.EnabledIfListeningOnPort;
import lombok.val;
import org.apache.commons.io.IOUtils;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
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
class DynamoDbSamlRegisteredServiceMetadataResolverTests extends BaseDynamoDbSamlMetadataTests {
    @Test
    void verifyResolver() throws Throwable {
        val res = new ClassPathResource("sp-metadata.xml");
        val md = new SamlMetadataDocument();
        md.setName("SP");
        md.setValue(IOUtils.toString(res.getInputStream(), StandardCharsets.UTF_8));
        resolver.saveOrUpdate(md);

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
        md.setValue(IOUtils.toString(res.getInputStream(), StandardCharsets.UTF_8));
        resolver.saveOrUpdate(md);

        val service = new SamlRegisteredService();
        service.setName("SAML Service");
        service.setServiceId("https://carmenwiki.osu.edu/shibboleth");
        val resolvers = resolver.resolve(service);
        assertTrue(resolvers.isEmpty());
    }

    @Test
    void verifyResolverDoesNotSupport() {
        assertFalse(resolver.supports(null));
    }
}
