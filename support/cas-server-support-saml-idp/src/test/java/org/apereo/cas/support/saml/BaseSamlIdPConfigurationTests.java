package org.apereo.cas.support.saml;

import org.apereo.cas.audit.spi.config.CasCoreAuditConfiguration;
import org.apereo.cas.authentication.AuthenticationEventExecutionPlan;
import org.apereo.cas.authentication.AuthenticationEventExecutionPlanConfigurer;
import org.apereo.cas.authentication.CoreAuthenticationTestUtils;
import org.apereo.cas.authentication.handler.support.SimpleTestUsernamePasswordAuthenticationHandler;
import org.apereo.cas.authentication.principal.PrincipalResolver;
import org.apereo.cas.authentication.principal.ServiceFactory;
import org.apereo.cas.authentication.principal.WebApplicationService;
import org.apereo.cas.config.CasCoreAuthenticationConfiguration;
import org.apereo.cas.config.CasCoreAuthenticationHandlersConfiguration;
import org.apereo.cas.config.CasCoreAuthenticationMetadataConfiguration;
import org.apereo.cas.config.CasCoreAuthenticationPolicyConfiguration;
import org.apereo.cas.config.CasCoreAuthenticationPrincipalConfiguration;
import org.apereo.cas.config.CasCoreAuthenticationServiceSelectionStrategyConfiguration;
import org.apereo.cas.config.CasCoreAuthenticationSupportConfiguration;
import org.apereo.cas.config.CasCoreConfiguration;
import org.apereo.cas.config.CasCoreHttpConfiguration;
import org.apereo.cas.config.CasCoreMultifactorAuthenticationConfiguration;
import org.apereo.cas.config.CasCoreNotificationsConfiguration;
import org.apereo.cas.config.CasCoreServicesAuthenticationConfiguration;
import org.apereo.cas.config.CasCoreServicesConfiguration;
import org.apereo.cas.config.CasCoreTicketCatalogConfiguration;
import org.apereo.cas.config.CasCoreTicketIdGeneratorsConfiguration;
import org.apereo.cas.config.CasCoreTicketsConfiguration;
import org.apereo.cas.config.CasCoreUtilConfiguration;
import org.apereo.cas.config.CasCoreUtilSerializationConfiguration;
import org.apereo.cas.config.CasCoreWebConfiguration;
import org.apereo.cas.config.CasDefaultServiceTicketIdGeneratorsConfiguration;
import org.apereo.cas.config.CasPersonDirectoryConfiguration;
import org.apereo.cas.config.CoreSamlConfiguration;
import org.apereo.cas.config.SamlIdPAuthenticationServiceSelectionStrategyConfiguration;
import org.apereo.cas.config.SamlIdPConfiguration;
import org.apereo.cas.config.SamlIdPEndpointsConfiguration;
import org.apereo.cas.config.SamlIdPMetadataConfiguration;
import org.apereo.cas.config.SamlIdPTicketCatalogConfiguration;
import org.apereo.cas.config.SamlIdPTicketSerializationConfiguration;
import org.apereo.cas.config.SamlIdpComponentSerializationConfiguration;
import org.apereo.cas.config.support.CasWebApplicationServiceFactoryConfiguration;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.logout.config.CasCoreLogoutConfiguration;
import org.apereo.cas.pac4j.DistributedJEESessionStore;
import org.apereo.cas.services.ServicesManager;
import org.apereo.cas.services.ServicesManagerRegisteredServiceLocator;
import org.apereo.cas.support.saml.idp.metadata.generator.SamlIdPMetadataCustomizer;
import org.apereo.cas.support.saml.idp.metadata.generator.SamlIdPMetadataGenerator;
import org.apereo.cas.support.saml.idp.metadata.locator.SamlIdPMetadataLocator;
import org.apereo.cas.support.saml.services.SamlRegisteredService;
import org.apereo.cas.support.saml.services.idp.metadata.cache.SamlRegisteredServiceCachingMetadataResolver;
import org.apereo.cas.support.saml.web.idp.profile.builders.AuthenticatedAssertionContext;
import org.apereo.cas.support.saml.web.idp.profile.builders.SamlProfileObjectBuilder;
import org.apereo.cas.support.saml.web.idp.profile.builders.enc.SamlIdPObjectEncrypter;
import org.apereo.cas.support.saml.web.idp.profile.builders.enc.SamlIdPObjectSigner;
import org.apereo.cas.support.saml.web.idp.profile.builders.enc.validate.SamlObjectSignatureValidator;
import org.apereo.cas.ticket.registry.TicketRegistry;
import org.apereo.cas.util.RandomUtils;
import org.apereo.cas.validation.config.CasCoreValidationConfiguration;
import org.apereo.cas.web.UrlValidator;
import org.apereo.cas.web.config.CasCookieConfiguration;
import org.apereo.cas.web.cookie.CasCookieBuilder;
import org.apereo.cas.web.flow.config.CasCoreWebflowConfiguration;
import org.apereo.cas.web.flow.config.CasMultifactorAuthenticationWebflowConfiguration;
import org.apereo.cas.web.flow.config.CasWebflowContextConfiguration;

import lombok.Data;
import lombok.val;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import org.apache.velocity.app.VelocityEngine;
import org.opensaml.saml.common.binding.artifact.SAMLArtifactMap;
import org.opensaml.saml.metadata.resolver.MetadataResolver;
import org.opensaml.saml.saml2.core.AuthnRequest;
import org.opensaml.saml.saml2.core.Conditions;
import org.opensaml.saml.saml2.core.Issuer;
import org.opensaml.saml.saml2.core.Response;
import org.opensaml.saml.saml2.core.Subject;
import org.opensaml.saml.saml2.metadata.Organization;
import org.opensaml.saml.saml2.metadata.OrganizationName;
import org.pac4j.core.context.session.SessionStore;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.SpringBootConfiguration;
import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.boot.autoconfigure.aop.AopAutoConfiguration;
import org.springframework.boot.autoconfigure.mail.MailSenderAutoConfiguration;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.boot.autoconfigure.web.servlet.WebMvcAutoConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.cloud.autoconfigure.RefreshAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Lazy;

import java.time.Clock;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Map;

import static org.mockito.Mockito.*;

/**
 * This is {@link BaseSamlIdPConfigurationTests}.
 *
 * @author Misagh Moayyed
 * @since 5.1.0
 */
@SpringBootTest(classes = BaseSamlIdPConfigurationTests.SharedTestConfiguration.class,
    properties = {
        "cas.webflow.crypto.encryption.key=qLhvLuaobvfzMmbo9U_bYA",
        "cas.webflow.crypto.signing.key=oZeAR5pEXsolruu4OQYsQKxf-FCvFzSsKlsVaKmfIl6pNzoPm6zPW94NRS1af7vT-0bb3DpPBeksvBXjloEsiA",
        "cas.authn.saml-idp.core.entity-id=https://cas.example.org/idp",
        "cas.authn.saml-idp.metadata.file-system.location=${#systemProperties['java.io.tmpdir']}/idp-metadata"
    })
@EnableConfigurationProperties(CasConfigurationProperties.class)
public abstract class BaseSamlIdPConfigurationTests {
    @Autowired
    @Qualifier("ticketGrantingTicketCookieGenerator")
    protected CasCookieBuilder ticketGrantingTicketCookieGenerator;

    @Autowired
    @Qualifier("webApplicationServiceFactory")
    protected ServiceFactory<WebApplicationService> webApplicationServiceFactory;

    @Autowired
    @Qualifier("casSamlIdPMetadataResolver")
    protected MetadataResolver casSamlIdPMetadataResolver;

    @Autowired
    @Qualifier(OpenSamlConfigBean.DEFAULT_BEAN_NAME)
    protected OpenSamlConfigBean openSamlConfigBean;

    @Autowired
    @Qualifier("shibboleth.VelocityEngine")
    protected VelocityEngine velocityEngine;

    @Autowired
    @Qualifier(SamlIdPObjectSigner.DEFAULT_BEAN_NAME)
    protected SamlIdPObjectSigner samlIdPObjectSigner;

    @Autowired
    @Qualifier("samlObjectEncrypter")
    protected SamlIdPObjectEncrypter samlIdPObjectEncrypter;

    @Autowired
    @Qualifier(SamlRegisteredServiceCachingMetadataResolver.DEFAULT_BEAN_NAME)
    protected SamlRegisteredServiceCachingMetadataResolver samlRegisteredServiceCachingMetadataResolver;

    @Autowired
    @Qualifier("urlValidator")
    protected UrlValidator urlValidator;

    @Autowired
    @Qualifier(ServicesManager.BEAN_NAME)
    protected ServicesManager servicesManager;

    @Autowired
    @Qualifier("samlProfileSamlResponseBuilder")
    protected SamlProfileObjectBuilder<Response> samlProfileSamlResponseBuilder;

    @Autowired
    @Qualifier("samlProfileSamlSubjectBuilder")
    protected SamlProfileObjectBuilder<Subject> samlProfileSamlSubjectBuilder;

    @Autowired
    @Qualifier("samlProfileSamlConditionsBuilder")
    protected SamlProfileObjectBuilder<Conditions> samlProfileSamlConditionsBuilder;

    @Autowired
    @Qualifier(DistributedJEESessionStore.DEFAULT_BEAN_NAME)
    protected SessionStore samlIdPDistributedSessionStore;

    @Autowired
    @Qualifier("samlObjectSignatureValidator")
    protected SamlObjectSignatureValidator samlObjectSignatureValidator;

    @Autowired
    protected CasConfigurationProperties casProperties;

    @Autowired
    @Qualifier(SamlRegisteredServiceCachingMetadataResolver.DEFAULT_BEAN_NAME)
    protected SamlRegisteredServiceCachingMetadataResolver defaultSamlRegisteredServiceCachingMetadataResolver;

    @Autowired
    @Qualifier("samlIdPMetadataLocator")
    protected SamlIdPMetadataLocator samlIdPMetadataLocator;

    @Autowired
    @Qualifier("samlIdPMetadataGenerator")
    protected SamlIdPMetadataGenerator samlIdPMetadataGenerator;

    @Autowired
    @Qualifier(TicketRegistry.BEAN_NAME)
    protected TicketRegistry ticketRegistry;

    @Autowired
    @Qualifier("samlArtifactMap")
    protected SAMLArtifactMap samlArtifactMap;

    @Autowired
    @Qualifier("samlIdPServicesManagerRegisteredServiceLocator")
    protected ServicesManagerRegisteredServiceLocator samlIdPServicesManagerRegisteredServiceLocator;

    protected static AuthenticatedAssertionContext getAssertion() {
        return getAssertion(Map.of());
    }

    protected static AuthenticatedAssertionContext getAssertion(final Map<String, Object> attrs) {
        return getAssertion("casuser", attrs);
    }

    protected static AuthenticatedAssertionContext getAssertion(final String user, final Map<String, Object> attrs) {
        val attributes = new LinkedHashMap<String, Object>(CoreAuthenticationTestUtils.getAttributes());
        attributes.putAll(attrs);

        val permissions = new ArrayList<>();
        permissions.add(new PermissionSamlAttributeValue("admin", "cas-admins", "super-cas"));
        permissions.add(new PermissionSamlAttributeValue("designer", "cas-designers", "cas-ux"));
        attributes.put("permissions", permissions);
        return AuthenticatedAssertionContext.builder()
            .name(user)
            .authenticationDate(ZonedDateTime.now(Clock.systemUTC()))
            .validUntilDate(ZonedDateTime.now(Clock.systemUTC()).plusDays(1))
            .validFromDate(ZonedDateTime.now(Clock.systemUTC()))
            .attributes(attributes)
            .build();
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
        service.setId(RandomUtils.nextInt());
        service.setSignAssertions(signAssertion);
        service.setSignResponses(signResponses);
        service.setEncryptAssertions(encryptAssertions);
        service.setDescription("SAML Service");
        service.setMetadataLocation("classpath:metadata/testshib-providers.xml");
        return service;
    }

    protected static SamlRegisteredService getSamlRegisteredServiceFor(final String entityId) {
        return getSamlRegisteredServiceFor(false, false, false, entityId);
    }

    @TestConfiguration
    @Lazy(false)
    public static class SamlIdPMetadataTestConfiguration implements AuthenticationEventExecutionPlanConfigurer {
        @Autowired
        @Qualifier("defaultPrincipalResolver")
        private PrincipalResolver defaultPrincipalResolver;

        @Autowired
        @Qualifier(OpenSamlConfigBean.DEFAULT_BEAN_NAME)
        private OpenSamlConfigBean openSamlConfigBean;

        @Override
        public void configureAuthenticationExecutionPlan(final AuthenticationEventExecutionPlan plan) {
            plan.registerAuthenticationHandlerWithPrincipalResolver(new SimpleTestUsernamePasswordAuthenticationHandler(), defaultPrincipalResolver);
        }

        @Bean
        public SamlIdPMetadataCustomizer samlIdPMetadataCustomizer() {
            return (entityDescriptor, registeredService) -> {
                val organization = (Organization) openSamlConfigBean.getBuilderFactory()
                    .getBuilder(Organization.DEFAULT_ELEMENT_NAME).buildObject(Organization.DEFAULT_ELEMENT_NAME);
                val orgName = (OrganizationName) openSamlConfigBean.getBuilderFactory()
                    .getBuilder(OrganizationName.DEFAULT_ELEMENT_NAME).buildObject(OrganizationName.DEFAULT_ELEMENT_NAME);
                orgName.setValue("CASOrganizationName");
                organization.getOrganizationNames().add(orgName);
                entityDescriptor.setOrganization(organization);
            };
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
        SecurityAutoConfiguration.class,
        WebMvcAutoConfiguration.class,
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
        CasCoreNotificationsConfiguration.class,
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
        SamlIdPTicketSerializationConfiguration.class,
        CasCoreTicketsConfiguration.class,
        CasCoreAuditConfiguration.class,
        CasCoreTicketCatalogConfiguration.class,
        CasCoreLogoutConfiguration.class,
        CasCookieConfiguration.class,
        CasCoreValidationConfiguration.class,
        CasCoreMultifactorAuthenticationConfiguration.class,
        CasMultifactorAuthenticationWebflowConfiguration.class,
        CasCoreConfiguration.class,
        CasCoreAuthenticationServiceSelectionStrategyConfiguration.class,
        CoreSamlConfiguration.class,
        CasPersonDirectoryConfiguration.class,
        CasCoreUtilSerializationConfiguration.class,
        CasCoreUtilConfiguration.class
    })
    public static class SharedTestConfiguration {
    }
}
