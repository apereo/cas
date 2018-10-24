package org.apereo.cas.support.saml.metadata.resolver;

import org.apereo.cas.support.saml.BaseMongoDbSamlMetadataTests;
import org.apereo.cas.support.saml.services.SamlRegisteredService;
import org.apereo.cas.support.saml.services.idp.metadata.SamlMetadataDocument;
import org.apereo.cas.util.junit.ConditionalIgnore;
import org.apereo.cas.util.junit.RunningContinuousIntegrationCondition;

import lombok.val;
import org.apache.commons.io.IOUtils;
import org.junit.jupiter.api.Test;
import org.springframework.core.io.ClassPathResource;
import org.springframework.test.context.TestPropertySource;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

import static org.junit.Assert.*;

/**
 * This is {@link MongoDbSamlRegisteredServiceMetadataResolverTests}.
 *
 * @author Misagh Moayyed
 * @since 5.2.0
 */
@TestPropertySource(properties = {
    "cas.authn.samlIdp.metadata.mongo.databaseName=saml-idp-resolver",
    "cas.authn.samlIdp.metadata.mongo.dropCollection=true",
    "cas.authn.samlIdp.metadata.mongo.collection=samlResolver",
    "cas.authn.samlIdp.metadata.mongo.host=localhost",
    "cas.authn.samlIdp.metadata.mongo.port=27017",
    "cas.authn.samlIdp.metadata.mongo.userId=root",
    "cas.authn.samlIdp.metadata.mongo.password=secret",
    "cas.authn.samlIdp.metadata.mongo.authenticationDatabaseName=admin",
    "cas.authn.samlIdp.metadata.mongo.idpMetadataCollection=saml-idp-metadata-resolver",
    "cas.authn.samlIdp.metadata.location=file:/tmp"
    })
@ConditionalIgnore(condition = RunningContinuousIntegrationCondition.class, port = 27017)
public class MongoDbSamlRegisteredServiceMetadataResolverTests extends BaseMongoDbSamlMetadataTests {
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
        val resolvers = resolver.resolve(service);
        assertEquals(1, resolvers.size());
    }
}
