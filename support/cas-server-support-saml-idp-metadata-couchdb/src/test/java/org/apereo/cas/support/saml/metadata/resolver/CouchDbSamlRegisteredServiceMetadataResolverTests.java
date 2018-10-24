package org.apereo.cas.support.saml.metadata.resolver;

import org.apereo.cas.category.CouchDbCategory;
import org.apereo.cas.config.CasCoreAuthenticationConfiguration;
import org.apereo.cas.config.CasCoreAuthenticationHandlersConfiguration;
import org.apereo.cas.config.CasCoreAuthenticationMetadataConfiguration;
import org.apereo.cas.config.CasCoreAuthenticationPolicyConfiguration;
import org.apereo.cas.config.CasCoreAuthenticationPrincipalConfiguration;
import org.apereo.cas.config.CasCoreAuthenticationServiceSelectionStrategyConfiguration;
import org.apereo.cas.config.CasCoreAuthenticationSupportConfiguration;
import org.apereo.cas.config.CasCoreConfiguration;
import org.apereo.cas.config.CasCoreHttpConfiguration;
import org.apereo.cas.config.CasCoreServicesAuthenticationConfiguration;
import org.apereo.cas.config.CasCoreServicesConfiguration;
import org.apereo.cas.config.CasCoreTicketCatalogConfiguration;
import org.apereo.cas.config.CasCoreTicketIdGeneratorsConfiguration;
import org.apereo.cas.config.CasCoreTicketsConfiguration;
import org.apereo.cas.config.CasCoreUtilConfiguration;
import org.apereo.cas.config.CasCoreWebConfiguration;
import org.apereo.cas.config.CasCouchDbCoreConfiguration;
import org.apereo.cas.config.CasDefaultServiceTicketIdGeneratorsConfiguration;
import org.apereo.cas.config.CasPersonDirectoryConfiguration;
import org.apereo.cas.config.CoreSamlConfiguration;
import org.apereo.cas.config.CouchDbSamlIdPFactoryConfiguration;
import org.apereo.cas.config.CouchDbSamlIdPMetadataConfiguration;
import org.apereo.cas.config.SamlIdPAuthenticationServiceSelectionStrategyConfiguration;
import org.apereo.cas.config.SamlIdPConfiguration;
import org.apereo.cas.config.SamlIdPCouchDbRegisteredServiceMetadataConfiguration;
import org.apereo.cas.config.SamlIdPEndpointsConfiguration;
import org.apereo.cas.config.SamlIdPMetadataConfiguration;
import org.apereo.cas.config.support.CasWebApplicationServiceFactoryConfiguration;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.couchdb.core.CouchDbConnectorFactory;
import org.apereo.cas.couchdb.saml.SamlMetadataDocumentCouchDbRepository;
import org.apereo.cas.logout.config.CasCoreLogoutConfiguration;
import org.apereo.cas.support.saml.services.SamlRegisteredService;
import org.apereo.cas.support.saml.services.idp.metadata.SamlMetadataDocument;
import org.apereo.cas.support.saml.services.idp.metadata.cache.resolver.SamlRegisteredServiceMetadataResolver;
import org.apereo.cas.validation.config.CasCoreValidationConfiguration;
import org.apereo.cas.web.config.CasCookieConfiguration;
import org.apereo.cas.web.flow.config.CasCoreWebflowConfiguration;

import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.io.IOUtils;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.experimental.categories.Category;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.aop.AopAutoConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.autoconfigure.RefreshAutoConfiguration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.rules.SpringClassRule;
import org.springframework.test.context.junit4.rules.SpringMethodRule;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

import static org.junit.Assert.*;

/**
 * This is {@link CouchDbSamlRegisteredServiceMetadataResolverTests}.
 *
 * @author Timur Duehr
 * @since 6.0.0
 */
@Category(CouchDbCategory.class)
@SpringBootTest(classes = {
    CouchDbSamlIdPFactoryConfiguration.class,
    CouchDbSamlIdPMetadataConfiguration.class,
    SamlIdPCouchDbRegisteredServiceMetadataConfiguration.class,
    CasCouchDbCoreConfiguration.class,
    CasDefaultServiceTicketIdGeneratorsConfiguration.class,
    CasCoreTicketIdGeneratorsConfiguration.class,
    CasWebApplicationServiceFactoryConfiguration.class,
    CasCoreAuthenticationConfiguration.class,
    CasCoreServicesAuthenticationConfiguration.class,
    CasCoreAuthenticationPolicyConfiguration.class,
    CasCoreAuthenticationPrincipalConfiguration.class,
    CasCoreAuthenticationMetadataConfiguration.class,
    CasCoreAuthenticationSupportConfiguration.class,
    CasCoreAuthenticationHandlersConfiguration.class,
    CasCoreHttpConfiguration.class,
    CasCoreServicesConfiguration.class,
    CasCoreWebConfiguration.class,
    CasCoreWebflowConfiguration.class,
    SamlIdPConfiguration.class,
    SamlIdPAuthenticationServiceSelectionStrategyConfiguration.class,
    SamlIdPEndpointsConfiguration.class,
    SamlIdPMetadataConfiguration.class,
    RefreshAutoConfiguration.class,
    AopAutoConfiguration.class,
    CasCoreAuthenticationConfiguration.class,
    CasCoreServicesAuthenticationConfiguration.class,
    CasCoreTicketsConfiguration.class,
    CasCoreTicketCatalogConfiguration.class,
    CasCoreLogoutConfiguration.class,
    CasCookieConfiguration.class,
    CasCoreValidationConfiguration.class,
    CasCoreConfiguration.class,
    CasCoreAuthenticationServiceSelectionStrategyConfiguration.class,
    CoreSamlConfiguration.class,
    CasPersonDirectoryConfiguration.class,
    CasCoreUtilConfiguration.class})
@EnableConfigurationProperties(CasConfigurationProperties.class)
@EnableTransactionManagement(proxyTargetClass = true)
@Slf4j
@TestPropertySource(properties = {
    "cas.authn.samlIdp.metadata.location=classpath:",
    "cas.authn.samlIdp.metadata.couchDb.dbName=saml_resolver",
    "cas.authn.samlIdp.metadata.couchDb.username=cas",
    "cas.authn.samlIdp.metadata.couchdb.password=password"
})
public class CouchDbSamlRegisteredServiceMetadataResolverTests {
    @ClassRule
    public static final SpringClassRule SPRING_CLASS_RULE = new SpringClassRule();

    @Rule
    public final SpringMethodRule springMethodRule = new SpringMethodRule();

    @Autowired
    @Qualifier("couchDbSamlRegisteredServiceMetadataResolver")
    private SamlRegisteredServiceMetadataResolver resolver;

    @Autowired
    @Qualifier("samlMetadataDocumentCouchDbRepository")
    private SamlMetadataDocumentCouchDbRepository couchDbRepository;

    @Autowired
    @Qualifier("samlMetadataCouchDbFactory")
    private CouchDbConnectorFactory couchDbFactory;

    @BeforeEach
    public void setUp() {
        couchDbFactory.getCouchDbInstance().createDatabaseIfNotExists(couchDbFactory.getCouchDbConnector().getDatabaseName());
        couchDbRepository.initStandardDesignDocument();
    }

    @AfterEach
    public void tearDown() {
        couchDbFactory.getCouchDbInstance().deleteDatabase(couchDbFactory.getCouchDbConnector().getDatabaseName());
    }

    @Test
    public void verifyResolver() {
        val res = new ClassPathResource("samlsp-metadata.xml");
        val md = new SamlMetadataDocument();
        md.setName("SP");
        try {
            md.setValue(IOUtils.toString(res.getInputStream(), StandardCharsets.UTF_8));
        } catch (final IOException e) {
            throw new AssertionError(e);
        }
        resolver.saveOrUpdate(md);

        val service = new SamlRegisteredService();
        service.setName("SAML Service");
        service.setServiceId("https://carmenwiki.osu.edu/shibboleth");
        service.setDescription("Testing");
        service.setMetadataLocation("couchdb://");
        assertTrue(resolver.supports(service));
        val resolvers = resolver.resolve(service);
        assertSame(1, resolvers.size());
    }

}
