package org.apereo.cas.support.saml;

import org.apereo.cas.authentication.attribute.AttributeDefinitionStore;
import org.apereo.cas.authentication.principal.ServiceFactory;
import org.apereo.cas.authentication.principal.WebApplicationService;
import org.apereo.cas.config.CasCoreAuthenticationAutoConfiguration;
import org.apereo.cas.config.CasCoreAutoConfiguration;
import org.apereo.cas.config.CasCoreCookieAutoConfiguration;
import org.apereo.cas.config.CasCoreLogoutAutoConfiguration;
import org.apereo.cas.config.CasCoreMultifactorAuthenticationAutoConfiguration;
import org.apereo.cas.config.CasCoreMultifactorAuthenticationWebflowAutoConfiguration;
import org.apereo.cas.config.CasCoreMultitenancyAutoConfiguration;
import org.apereo.cas.config.CasCoreNotificationsAutoConfiguration;
import org.apereo.cas.config.CasCoreSamlAutoConfiguration;
import org.apereo.cas.config.CasCoreScriptingAutoConfiguration;
import org.apereo.cas.config.CasCoreServicesAutoConfiguration;
import org.apereo.cas.config.CasCoreTicketsAutoConfiguration;
import org.apereo.cas.config.CasCoreUtilAutoConfiguration;
import org.apereo.cas.config.CasCoreValidationAutoConfiguration;
import org.apereo.cas.config.CasCoreWebAutoConfiguration;
import org.apereo.cas.config.CasCoreWebflowAutoConfiguration;
import org.apereo.cas.config.CasPersonDirectoryAutoConfiguration;
import org.apereo.cas.config.CasRegisteredServicesTestConfiguration;
import org.apereo.cas.config.CasSamlAutoConfiguration;
import org.apereo.cas.config.CasThemesAutoConfiguration;
import org.apereo.cas.config.CasThymeleafAutoConfiguration;
import org.apereo.cas.config.CasValidationAutoConfiguration;
import org.apereo.cas.multitenancy.TenantExtractor;
import org.apereo.cas.services.RegisteredServicesTemplatesManager;
import org.apereo.cas.services.ServicesManager;
import org.apereo.cas.test.CasTestExtension;
import org.apereo.cas.util.spring.boot.SpringBootTestAutoConfigurations;
import org.apereo.cas.web.UrlValidator;
import net.shibboleth.shared.xml.ParserPool;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.opensaml.core.xml.XMLObjectBuilderFactory;
import org.opensaml.core.xml.config.XMLObjectProviderRegistrySupport;
import org.opensaml.core.xml.io.MarshallerFactory;
import org.opensaml.core.xml.io.UnmarshallerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.SpringBootConfiguration;
import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Import;
import static org.junit.jupiter.api.Assertions.*;

/**
 * OpenSaml context loading tests.
 *
 * @author Misagh Moayyed
 * @since 4.1
 */
@ExtendWith(CasTestExtension.class)
@SpringBootTest(classes = AbstractOpenSamlTests.SharedTestConfiguration.class)
public abstract class AbstractOpenSamlTests {
    protected static final String SAML_REQUEST = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>"
        + "<samlp:AuthnRequest xmlns:samlp=\"urn:oasis:names:tc:SAML:2.0:protocol\" "
        + "ID=\"5545454455\" Version=\"2.0\" IssueInstant=\"Value\" "
        + "ProtocolBinding=\"urn:oasis:names.tc:SAML:2.0:bindings:HTTP-Redirect\" "
        + "ProviderName=\"https://localhost:8443/myRutgers\" "
        + "AssertionConsumerServiceURL=\"https://localhost:8443/myRutgers\"/>";

    @Autowired
    protected ConfigurableApplicationContext applicationContext;

    @Autowired
    @Qualifier(RegisteredServicesTemplatesManager.BEAN_NAME)
    protected RegisteredServicesTemplatesManager registeredServicesTemplatesManager;

    @Autowired
    @Qualifier(OpenSamlConfigBean.DEFAULT_BEAN_NAME)
    protected OpenSamlConfigBean configBean;

    @Autowired
    @Qualifier("shibboleth.ParserPool")
    protected ParserPool parserPool;

    @Autowired
    @Qualifier("shibboleth.BuilderFactory")
    protected XMLObjectBuilderFactory builderFactory;

    @Autowired
    @Qualifier("shibboleth.MarshallerFactory")
    protected MarshallerFactory marshallerFactory;

    @Autowired
    @Qualifier("shibboleth.UnmarshallerFactory")
    protected UnmarshallerFactory unmarshallerFactory;

    @Autowired
    @Qualifier(AttributeDefinitionStore.BEAN_NAME)
    protected AttributeDefinitionStore attributeDefinitionStore;

    @Autowired
    @Qualifier(ServicesManager.BEAN_NAME)
    protected ServicesManager servicesManager;

    @Autowired
    @Qualifier(WebApplicationService.BEAN_NAME_FACTORY)
    protected ServiceFactory<WebApplicationService> webApplicationServiceFactory;

    @Autowired
    @Qualifier(UrlValidator.BEAN_NAME)
    protected UrlValidator urlValidator;

    @Autowired
    @Qualifier(TenantExtractor.BEAN_NAME)
    protected TenantExtractor tenantExtractor;
    
    @Test
    void autowireApplicationContext() {
        assertNotNull(this.applicationContext);
        assertNotNull(this.configBean);
        assertNotNull(this.parserPool);
        assertNotNull(this.builderFactory);
        assertNotNull(this.unmarshallerFactory);
        assertNotNull(this.marshallerFactory);
        assertNotNull(this.configBean.getParserPool());
    }

    @Test
    void loadStaticContextFactories() {
        assertNotNull(XMLObjectProviderRegistrySupport.getParserPool());
        assertNotNull(XMLObjectProviderRegistrySupport.getBuilderFactory());
        assertNotNull(XMLObjectProviderRegistrySupport.getMarshallerFactory());
        assertNotNull(XMLObjectProviderRegistrySupport.getUnmarshallerFactory());
    }

    @Test
    void ensureParserIsInitialized() throws Exception {
        assertNotNull(this.parserPool);
        assertNotNull(this.parserPool.getBuilder());
    }

    @SpringBootConfiguration(proxyBeanMethods = false)
    @Import(CasRegisteredServicesTestConfiguration.class)
    @SpringBootTestAutoConfigurations
    @ImportAutoConfiguration({
        CasThymeleafAutoConfiguration.class,
        CasThemesAutoConfiguration.class,
        CasCoreSamlAutoConfiguration.class,
        CasSamlAutoConfiguration.class,
        CasCoreWebAutoConfiguration.class,
        CasCoreWebflowAutoConfiguration.class,
        CasPersonDirectoryAutoConfiguration.class,
        CasCoreNotificationsAutoConfiguration.class,
        CasCoreValidationAutoConfiguration.class,
        CasValidationAutoConfiguration.class,
        CasCoreServicesAutoConfiguration.class,
        CasCoreAuthenticationAutoConfiguration.class,
        CasCoreTicketsAutoConfiguration.class,
        CasCoreLogoutAutoConfiguration.class,
        CasCoreUtilAutoConfiguration.class,
        CasCoreScriptingAutoConfiguration.class,
        CasCoreCookieAutoConfiguration.class,
        CasCoreMultitenancyAutoConfiguration.class,
        CasCoreMultifactorAuthenticationAutoConfiguration.class,
        CasCoreMultifactorAuthenticationWebflowAutoConfiguration.class,
        CasCoreAutoConfiguration.class
    })
    public static class SharedTestConfiguration {
    }
}
