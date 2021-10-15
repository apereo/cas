package org.apereo.cas.support.saml.metadata.resolver;

import org.apereo.cas.config.CasCouchDbCoreConfiguration;
import org.apereo.cas.config.CouchDbSamlIdPFactoryConfiguration;
import org.apereo.cas.config.CouchDbSamlIdPMetadataConfiguration;
import org.apereo.cas.config.SamlIdPCouchDbRegisteredServiceMetadataConfiguration;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.couchdb.core.CouchDbConnectorFactory;
import org.apereo.cas.couchdb.saml.SamlMetadataDocumentCouchDbRepository;
import org.apereo.cas.support.saml.BaseSamlIdPMetadataTests;
import org.apereo.cas.support.saml.services.SamlRegisteredService;
import org.apereo.cas.support.saml.services.idp.metadata.SamlMetadataDocument;
import org.apereo.cas.support.saml.services.idp.metadata.cache.resolver.SamlRegisteredServiceMetadataResolver;
import org.apereo.cas.util.junit.EnabledIfPortOpen;

import lombok.val;
import org.apache.commons.io.IOUtils;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.io.ClassPathResource;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link CouchDbSamlRegisteredServiceMetadataResolverTests}.
 *
 * @author Timur Duehr
 * @since 6.0.0
 */
@Tag("CouchDb")
@SpringBootTest(classes = {
    CouchDbSamlIdPFactoryConfiguration.class,
    CouchDbSamlIdPMetadataConfiguration.class,
    SamlIdPCouchDbRegisteredServiceMetadataConfiguration.class,
    CasCouchDbCoreConfiguration.class,
    BaseSamlIdPMetadataTests.SharedTestConfiguration.class
},
    properties = {
        "cas.authn.saml-idp.metadata.file-system.location=classpath:",
        "cas.authn.saml-idp.metadata.couch-db.db-name=saml_resolver",
        "cas.authn.saml-idp.metadata.couch-db.username=cas",
        "cas.authn.saml-idp.metadata.couch-db.caching=false",
        "cas.authn.saml-idp.metadata.couch-db.password=password"
    })
@EnableConfigurationProperties(CasConfigurationProperties.class)
@EnableTransactionManagement
@EnabledIfPortOpen(port = 5984)
public class CouchDbSamlRegisteredServiceMetadataResolverTests {
    @Autowired
    @Qualifier("couchDbSamlRegisteredServiceMetadataResolver")
    private SamlRegisteredServiceMetadataResolver resolver;

    @Autowired
    @Qualifier("samlMetadataCouchDbFactory")
    private CouchDbConnectorFactory couchDbFactory;

    @Autowired
    @Qualifier("samlMetadataDocumentCouchDbRepository")
    private SamlMetadataDocumentCouchDbRepository couchDbRepository;

    @BeforeEach
    public void setUp() {
        couchDbFactory.getCouchDbInstance()
            .createDatabaseIfNotExists(couchDbFactory.getCouchDbConnector().getDatabaseName());
        couchDbRepository.initStandardDesignDocument();
    }

    @AfterEach
    public void tearDown() {
        couchDbFactory.getCouchDbInstance()
            .deleteDatabase(couchDbFactory.getCouchDbConnector().getDatabaseName());
    }

    @Test
    public void verifyResolver() throws Exception {
        val res = new ClassPathResource("samlsp-metadata.xml");
        val md = new SamlMetadataDocument();
        md.setName("SP");
        md.setValue(IOUtils.toString(res.getInputStream(), StandardCharsets.UTF_8));
        resolver.saveOrUpdate(md);

        val service = new SamlRegisteredService();
        service.setName("SAML Service");
        service.setServiceId("https://carmenwiki.osu.edu/shibboleth");
        service.setDescription("Testing");
        service.setMetadataLocation("couchdb://");
        assertTrue(resolver.supports(service));
        assertFalse(resolver.supports(null));
        assertTrue(resolver.isAvailable(service));
        val resolvers = resolver.resolve(service);
        assertSame(1, resolvers.size());
    }

}
