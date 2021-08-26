package org.apereo.cas.support.saml.metadata.resolver;

import org.apereo.cas.support.saml.BaseMongoDbSamlMetadataTests;
import org.apereo.cas.support.saml.services.SamlRegisteredService;
import org.apereo.cas.support.saml.services.idp.metadata.SamlMetadataDocument;
import org.apereo.cas.util.junit.EnabledIfPortOpen;

import lombok.val;
import org.apache.commons.io.IOUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.test.context.TestPropertySource;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

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
@EnabledIfPortOpen(port = 27017)
public class MongoDbSamlRegisteredServiceMetadataResolverTests extends BaseMongoDbSamlMetadataTests {
    @Autowired
    @Qualifier("mongoDbSamlMetadataResolverTemplate")
    private MongoTemplate mongoDbSamlIdPMetadataTemplate;

    @BeforeEach
    public void setup() {
        mongoDbSamlIdPMetadataTemplate.remove(new Query(), SamlMetadataDocument.class,
            casProperties.getAuthn().getSamlIdp().getMetadata().getMongo().getCollection());
    }

    @Test
    public void verifyResolver() throws IOException {
        val res = new ClassPathResource("sp-metadata.xml");
        val md = new SamlMetadataDocument();
        md.setName("SP");
        md.setValue(IOUtils.toString(res.getInputStream(), StandardCharsets.UTF_8));
        resolver.saveOrUpdate(md);

        val service = new SamlRegisteredService();
        service.setName("SAML Service");
        service.setServiceId("https://carmenwiki.osu.edu/shibboleth");
        service.setDescription("Testing");
        service.setMetadataLocation("mongodb://");
        assertTrue(resolver.supports(service));
        assertTrue(resolver.isAvailable(service));
        val resolvers = resolver.resolve(service);
        assertEquals(1, resolvers.size());
    }

    @Test
    public void verifyFailsResolver() throws IOException {
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
    public void verifyResolverDoesNotSupport() {
        assertFalse(resolver.supports(null));

    }
}
