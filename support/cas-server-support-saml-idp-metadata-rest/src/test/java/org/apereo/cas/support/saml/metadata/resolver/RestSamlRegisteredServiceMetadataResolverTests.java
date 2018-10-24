package org.apereo.cas.support.saml.metadata.resolver;

import org.apereo.cas.category.RestfulApiCategory;
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
import org.apereo.cas.config.CasDefaultServiceTicketIdGeneratorsConfiguration;
import org.apereo.cas.config.CasPersonDirectoryConfiguration;
import org.apereo.cas.config.CoreSamlConfiguration;
import org.apereo.cas.config.SamlIdPAuthenticationServiceSelectionStrategyConfiguration;
import org.apereo.cas.config.SamlIdPConfiguration;
import org.apereo.cas.config.SamlIdPEndpointsConfiguration;
import org.apereo.cas.config.SamlIdPMetadataConfiguration;
import org.apereo.cas.config.SamlIdPRestMetadataConfiguration;
import org.apereo.cas.config.support.CasWebApplicationServiceFactoryConfiguration;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.logout.config.CasCoreLogoutConfiguration;
import org.apereo.cas.support.saml.services.SamlRegisteredService;
import org.apereo.cas.support.saml.services.idp.metadata.SamlMetadataDocument;
import org.apereo.cas.support.saml.services.idp.metadata.cache.resolver.SamlRegisteredServiceMetadataResolver;
import org.apereo.cas.util.MockWebServer;
import org.apereo.cas.validation.config.CasCoreValidationConfiguration;
import org.apereo.cas.web.config.CasCookieConfiguration;
import org.apereo.cas.web.flow.config.CasCoreWebflowConfiguration;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.SneakyThrows;
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
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.rules.SpringClassRule;
import org.springframework.test.context.junit4.rules.SpringMethodRule;

import java.nio.charset.StandardCharsets;

import static org.junit.Assert.*;

/**
 * This is {@link RestSamlRegisteredServiceMetadataResolverTests}.
 *
 * @author Misagh Moayyed
 * @since 5.3.0
 */
@Category(RestfulApiCategory.class)
@SpringBootTest(classes = {
    SamlIdPRestMetadataConfiguration.class,
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
@TestPropertySource(properties = {
    "cas.authn.samlIdp.metadata.rest.url=http://localhost:8078",
    "cas.authn.samlIdp.metadata.location=file:/tmp"
})
@EnableConfigurationProperties(CasConfigurationProperties.class)
public class RestSamlRegisteredServiceMetadataResolverTests {

    @ClassRule
    public static final SpringClassRule SPRING_CLASS_RULE = new SpringClassRule();

    private static final ObjectMapper MAPPER = new ObjectMapper().findAndRegisterModules();

    @Rule
    public final SpringMethodRule springMethodRule = new SpringMethodRule();

    @Autowired
    @Qualifier("restSamlRegisteredServiceMetadataResolver")
    private SamlRegisteredServiceMetadataResolver resolver;

    private MockWebServer webServer;

    @BeforeEach
    @SneakyThrows
    public void initialize() {
        val doc = new SamlMetadataDocument();
        doc.setId(1);
        doc.setName("SAML Document");
        doc.setSignature(null);
        doc.setValue(IOUtils.toString(new ClassPathResource("sp-metadata.xml").getInputStream(), StandardCharsets.UTF_8));
        val data = MAPPER.writeValueAsString(doc);

        this.webServer = new MockWebServer(8078,
            new ByteArrayResource(data.getBytes(StandardCharsets.UTF_8), "REST Output"),
            MediaType.APPLICATION_XML_VALUE);

        this.webServer.start();
    }

    @AfterEach
    public void cleanup() {
        this.webServer.stop();
    }

    @Test
    public void verifyRestEndpointProducesMetadata() {
        val service = new SamlRegisteredService();
        service.setName("SAML Wiki Service");
        service.setServiceId("https://carmenwiki.osu.edu/shibboleth");
        service.setDescription("Testing");
        service.setMetadataLocation("rest://");
        assertTrue(resolver.supports(service));
        val resolvers = resolver.resolve(service);
        assertTrue(resolvers.size() == 1);
    }
}
