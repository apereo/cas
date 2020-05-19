package org.apereo.cas.support.saml;

import org.apereo.cas.authentication.AuthenticationEventExecutionPlan;
import org.apereo.cas.authentication.AuthenticationEventExecutionPlanConfigurer;
import org.apereo.cas.authentication.CoreAuthenticationTestUtils;
import org.apereo.cas.authentication.handler.support.SimpleTestUsernamePasswordAuthenticationHandler;
import org.apereo.cas.authentication.principal.PrincipalResolver;
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
import org.apereo.cas.config.SamlIdPTicketCatalogConfiguration;
import org.apereo.cas.config.SamlIdpComponentSerializationConfiguration;
import org.apereo.cas.config.support.CasWebApplicationServiceFactoryConfiguration;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.logout.config.CasCoreLogoutConfiguration;
import org.apereo.cas.services.ServicesManager;
import org.apereo.cas.support.saml.idp.metadata.generator.SamlIdPMetadataGenerator;
import org.apereo.cas.support.saml.idp.metadata.locator.SamlIdPMetadataLocator;
import org.apereo.cas.support.saml.services.SamlRegisteredService;
import org.apereo.cas.support.saml.services.idp.metadata.cache.SamlRegisteredServiceCachingMetadataResolver;
import org.apereo.cas.support.saml.web.idp.profile.builders.SamlProfileObjectBuilder;
import org.apereo.cas.support.saml.web.idp.profile.builders.enc.SamlIdPObjectEncrypter;
import org.apereo.cas.support.saml.web.idp.profile.builders.enc.SamlIdPObjectSigner;
import org.apereo.cas.support.saml.web.idp.profile.builders.enc.validate.SamlObjectSignatureValidator;
import org.apereo.cas.ticket.registry.TicketRegistry;
import org.apereo.cas.validation.config.CasCoreValidationConfiguration;
import org.apereo.cas.web.UrlValidator;
import org.apereo.cas.web.config.CasCookieConfiguration;
import org.apereo.cas.web.flow.config.CasCoreWebflowConfiguration;
import org.apereo.cas.web.flow.config.CasWebflowContextConfiguration;

import lombok.Data;
import lombok.val;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import org.jasig.cas.client.authentication.AttributePrincipalImpl;
import org.jasig.cas.client.validation.Assertion;
import org.jasig.cas.client.validation.AssertionImpl;
import org.junit.jupiter.api.Tag;
import org.opensaml.saml.metadata.resolver.MetadataResolver;
import org.opensaml.saml.saml2.core.AuthnRequest;
import org.opensaml.saml.saml2.core.Issuer;
import org.opensaml.saml.saml2.core.Response;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.SpringBootConfiguration;
import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.boot.autoconfigure.aop.AopAutoConfiguration;
import org.springframework.boot.autoconfigure.mail.MailSenderAutoConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.cloud.autoconfigure.RefreshAutoConfiguration;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Lazy;

import java.util.ArrayList;
import java.util.LinkedHashMap;

import static org.mockito.Mockito.*;

/**
 * This is {@link BaseSamlIdPConfigurationTests}.
 *
 * @author Misagh Moayyed
 * @since 5.1.0
 */
@SpringBootTest(classes = BaseSamlIdPConfigurationTests.SharedTestConfiguration.class,
    properties = {
        "cas.authn.saml-idp.entityId=https://cas.example.org/idp",
        "cas.authn.saml-idp.metadata.location=${#systemProperties['java.io.tmpdir']}/idp-metadata"
    })
@EnableConfigurationProperties(CasConfigurationProperties.class)
@Tag("SAML")
public abstract class BaseSamlIdPConfigurationTests {
    @Autowired
    @Qualifier("casSamlIdPMetadataResolver")
    protected MetadataResolver casSamlIdPMetadataResolver;

    @Autowired
    @Qualifier("shibboleth.OpenSAMLConfig")
    protected OpenSamlConfigBean openSamlConfigBean;

    @Autowired
    @Qualifier("samlObjectSigner")
    protected SamlIdPObjectSigner samlIdPObjectSigner;

    @Autowired
    @Qualifier("samlObjectEncrypter")
    protected SamlIdPObjectEncrypter samlIdPObjectEncrypter;

    @Autowired
    @Qualifier("defaultSamlRegisteredServiceCachingMetadataResolver")
    protected SamlRegisteredServiceCachingMetadataResolver samlRegisteredServiceCachingMetadataResolver;

    @Autowired
    @Qualifier("urlValidator")
    protected UrlValidator urlValidator;

    @Autowired
    @Qualifier("servicesManager")
    protected ServicesManager servicesManager;

    @Autowired
    @Qualifier("samlProfileSamlResponseBuilder")
    protected SamlProfileObjectBuilder<Response> samlProfileSamlResponseBuilder;

    @Autowired
    @Qualifier("samlObjectSignatureValidator")
    protected SamlObjectSignatureValidator samlObjectSignatureValidator;

    @Autowired
    protected CasConfigurationProperties casProperties;

    @Autowired
    @Qualifier("defaultSamlRegisteredServiceCachingMetadataResolver")
    protected SamlRegisteredServiceCachingMetadataResolver defaultSamlRegisteredServiceCachingMetadataResolver;

    @Autowired
    @Qualifier("samlIdPMetadataLocator")
    protected SamlIdPMetadataLocator samlIdPMetadataLocator;

    @Autowired
    @Qualifier("samlIdPMetadataGenerator")
    protected SamlIdPMetadataGenerator samlIdPMetadataGenerator;

    @Autowired
    @Qualifier("ticketRegistry")
    protected TicketRegistry ticketRegistry;

    @TestConfiguration
    @Lazy(false)
    public static class SamlIdPMetadataTestConfiguration implements AuthenticationEventExecutionPlanConfigurer {
        @Autowired
        @Qualifier("defaultPrincipalResolver")
        private PrincipalResolver defaultPrincipalResolver;

        @Override
        public void configureAuthenticationExecutionPlan(final AuthenticationEventExecutionPlan plan) {
            plan.registerAuthenticationHandlerWithPrincipalResolver(new SimpleTestUsernamePasswordAuthenticationHandler(), defaultPrincipalResolver);
        }
    }

    @Data
    public static class PermissionSamlAttributeValue {
        private final String type;

        private final String group;

        private final String user;

        @Override
        public String toString() {
            return new ToStringBuilder(this, ToStringStyle.NO_CLASS_NAME_STYLE)
                .append("user", user)
                .append("group", group)
                .append("type", type)
                .build();
        }
    }

    @ImportAutoConfiguration({
        RefreshAutoConfiguration.class,
        MailSenderAutoConfiguration.class,
        AopAutoConfiguration.class
    })
    @SpringBootConfiguration
    @Import({
        SamlIdPMetadataTestConfiguration.class,
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
        CasWebflowContextConfiguration.class,
        SamlIdPConfiguration.class,
        SamlIdpComponentSerializationConfiguration.class,
        SamlIdPTicketCatalogConfiguration.class,
        SamlIdPAuthenticationServiceSelectionStrategyConfiguration.class,
        SamlIdPEndpointsConfiguration.class,
        SamlIdPMetadataConfiguration.class,
        CasCoreTicketsConfiguration.class,
        CasCoreTicketCatalogConfiguration.class,
        CasCoreLogoutConfiguration.class,
        CasCookieConfiguration.class,
        CasCoreValidationConfiguration.class,
        CasCoreConfiguration.class,
        CasCoreAuthenticationServiceSelectionStrategyConfiguration.class,
        CoreSamlConfiguration.class,
        CasPersonDirectoryConfiguration.class,
        CasCoreUtilConfiguration.class
    })
    public static class SharedTestConfiguration {
    }

    protected static Assertion getAssertion() {
        val attributes = new LinkedHashMap<String, Object>(CoreAuthenticationTestUtils.getAttributes());
        val permissions = new ArrayList<>();
        permissions.add(new PermissionSamlAttributeValue("admin", "cas-admins", "super-cas"));
        permissions.add(new PermissionSamlAttributeValue("designer", "cas-designers", "cas-ux"));
        attributes.put("permissions", permissions);
        val casuser = new AttributePrincipalImpl("casuser", attributes);
        return new AssertionImpl(casuser, attributes);
    }

    protected static AuthnRequest getAuthnRequestFor(final SamlRegisteredService service) {
        return getAuthnRequestFor(service.getServiceId());
    }

    protected static AuthnRequest getAuthnRequestFor(final String service) {
        val authnRequest = mock(AuthnRequest.class);
        when(authnRequest.getID()).thenReturn("23hgbcehfgeb7843jdv1");
        val issuer = mock(Issuer.class);
        when(issuer.getValue()).thenReturn(service);
        when(authnRequest.getIssuer()).thenReturn(issuer);
        return authnRequest;
    }

    protected static SamlRegisteredService getSamlRegisteredServiceForTestShib() {
        return getSamlRegisteredServiceForTestShib(false, false, false);
    }

    protected static SamlRegisteredService getSamlRegisteredServiceForTestShib(final boolean signAssertion,
                                                                               final boolean signResponses) {
        return getSamlRegisteredServiceForTestShib(signAssertion, signResponses, false);
    }

    protected static SamlRegisteredService getSamlRegisteredServiceForTestShib(final boolean signAssertion,
                                                                               final boolean signResponses,
                                                                               final boolean encryptAssertions) {
        return getSamlRegisteredServiceFor(signAssertion, signResponses, encryptAssertions, "https://sp.testshib.org/shibboleth-sp");
    }

    protected static SamlRegisteredService getSamlRegisteredServiceFor(final boolean signAssertion,
                                                                       final boolean signResponses,
                                                                       final boolean encryptAssertions,
                                                                       final String entityId) {
        val service = new SamlRegisteredService();
        service.setName("TestShib");
        service.setServiceId(entityId);
        service.setId(100);
        service.setSignAssertions(signAssertion);
        service.setSignResponses(signResponses);
        service.setEncryptAssertions(encryptAssertions);
        service.setDescription("SAML Service");
        service.setMetadataLocation("classpath:metadata/testshib-providers.xml");
        return service;
    }
}
