package org.apereo.cas.config;

import org.apereo.cas.CentralAuthenticationService;
import org.apereo.cas.audit.AuditActionResolvers;
import org.apereo.cas.audit.AuditResourceResolvers;
import org.apereo.cas.audit.AuditTrailConstants;
import org.apereo.cas.audit.AuditTrailRecordResolutionPlanConfigurer;
import org.apereo.cas.authentication.attribute.AttributeDefinitionStore;
import org.apereo.cas.authentication.principal.PersistentIdGenerator;
import org.apereo.cas.authentication.principal.ServiceFactory;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.jpa.JpaPersistenceProviderConfigurer;
import org.apereo.cas.logout.slo.SingleLogoutServiceLogoutUrlBuilderConfigurer;
import org.apereo.cas.services.ServicesManager;
import org.apereo.cas.support.saml.OpenSamlConfigBean;
import org.apereo.cas.support.saml.idp.metadata.locator.SamlIdPMetadataLocator;
import org.apereo.cas.support.saml.services.SamlRegisteredService;
import org.apereo.cas.support.saml.services.idp.metadata.cache.SamlRegisteredServiceCachingMetadataResolver;
import org.apereo.cas.support.saml.web.idp.audit.SamlRequestAuditResourceResolver;
import org.apereo.cas.support.saml.web.idp.audit.SamlResponseAuditPrincipalIdProvider;
import org.apereo.cas.support.saml.web.idp.audit.SamlResponseAuditResourceResolver;
import org.apereo.cas.support.saml.web.idp.profile.artifact.CasSamlArtifactMap;
import org.apereo.cas.support.saml.web.idp.profile.builders.SamlProfileObjectBuilder;
import org.apereo.cas.support.saml.web.idp.profile.builders.assertion.SamlProfileSamlAssertionBuilder;
import org.apereo.cas.support.saml.web.idp.profile.builders.attr.SamlProfileSamlAttributeStatementBuilder;
import org.apereo.cas.support.saml.web.idp.profile.builders.authn.AuthnContextClassRefBuilder;
import org.apereo.cas.support.saml.web.idp.profile.builders.authn.DefaultAuthnContextClassRefBuilder;
import org.apereo.cas.support.saml.web.idp.profile.builders.authn.SamlProfileSamlAuthNStatementBuilder;
import org.apereo.cas.support.saml.web.idp.profile.builders.conditions.SamlProfileSamlConditionsBuilder;
import org.apereo.cas.support.saml.web.idp.profile.builders.enc.SamlIdPObjectEncrypter;
import org.apereo.cas.support.saml.web.idp.profile.builders.enc.SamlIdPObjectSigner;
import org.apereo.cas.support.saml.web.idp.profile.builders.nameid.SamlProfileSamlNameIdBuilder;
import org.apereo.cas.support.saml.web.idp.profile.builders.response.SamlProfileSaml2ResponseBuilder;
import org.apereo.cas.support.saml.web.idp.profile.builders.response.SamlProfileSamlResponseBuilderConfigurationContext;
import org.apereo.cas.support.saml.web.idp.profile.builders.response.artifact.SamlProfileArtifactFaultResponseBuilder;
import org.apereo.cas.support.saml.web.idp.profile.builders.response.artifact.SamlProfileArtifactResponseBuilder;
import org.apereo.cas.support.saml.web.idp.profile.builders.response.query.SamlProfileAttributeQueryFaultResponseBuilder;
import org.apereo.cas.support.saml.web.idp.profile.builders.response.query.SamlProfileAttributeQueryResponseBuilder;
import org.apereo.cas.support.saml.web.idp.profile.builders.response.soap.SamlProfileSamlSoap11FaultResponseBuilder;
import org.apereo.cas.support.saml.web.idp.profile.builders.response.soap.SamlProfileSamlSoap11ResponseBuilder;
import org.apereo.cas.support.saml.web.idp.profile.builders.subject.SamlProfileSamlSubjectBuilder;
import org.apereo.cas.support.saml.web.idp.profile.slo.SamlIdPSingleLogoutServiceLogoutUrlBuilder;
import org.apereo.cas.ticket.ExpirationPolicyBuilder;
import org.apereo.cas.ticket.TicketFactoryExecutionPlanConfigurer;
import org.apereo.cas.ticket.artifact.DefaultSamlArtifactTicketFactory;
import org.apereo.cas.ticket.artifact.SamlArtifactTicketExpirationPolicyBuilder;
import org.apereo.cas.ticket.artifact.SamlArtifactTicketFactory;
import org.apereo.cas.ticket.query.DefaultSamlAttributeQueryTicketFactory;
import org.apereo.cas.ticket.query.SamlAttributeQueryTicketExpirationPolicyBuilder;
import org.apereo.cas.ticket.query.SamlAttributeQueryTicketFactory;
import org.apereo.cas.ticket.registry.TicketRegistry;
import org.apereo.cas.web.cookie.CasCookieBuilder;

import lombok.val;
import org.apache.velocity.app.VelocityEngine;
import org.apereo.inspektr.audit.spi.support.DefaultAuditActionResolver;
import org.opensaml.saml.common.binding.artifact.SAMLArtifactMap;
import org.opensaml.saml.metadata.resolver.MetadataResolver;
import org.opensaml.saml.saml2.core.Assertion;
import org.opensaml.saml.saml2.core.AttributeStatement;
import org.opensaml.saml.saml2.core.AuthnStatement;
import org.opensaml.saml.saml2.core.Conditions;
import org.opensaml.saml.saml2.core.NameID;
import org.opensaml.saml.saml2.core.Subject;
import org.opensaml.saml.saml2.ecp.Response;
import org.pac4j.core.context.session.SessionStore;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Duration;
import java.util.List;

/**
 * The {@link SamlIdPConfiguration}.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 */
@Configuration("samlIdPConfiguration")
@EnableConfigurationProperties(CasConfigurationProperties.class)
public class SamlIdPConfiguration {

    @Autowired
    @Qualifier("ticketGrantingTicketCookieGenerator")
    private ObjectProvider<CasCookieBuilder> ticketGrantingTicketCookieGenerator;

    @Autowired
    @Qualifier("ticketRegistry")
    private ObjectProvider<TicketRegistry> ticketRegistry;

    @Autowired
    @Qualifier("samlIdPDistributedSessionStore")
    private ObjectProvider<SessionStore> samlIdPDistributedSessionStore;

    @Autowired
    private CasConfigurationProperties casProperties;

    @Autowired
    @Qualifier(SamlRegisteredServiceCachingMetadataResolver.DEFAULT_BEAN_NAME)
    private ObjectProvider<SamlRegisteredServiceCachingMetadataResolver> defaultSamlRegisteredServiceCachingMetadataResolver;

    @Autowired
    @Qualifier("centralAuthenticationService")
    private ObjectProvider<CentralAuthenticationService> centralAuthenticationService;

    @Autowired
    @Qualifier("casSamlIdPMetadataResolver")
    private ObjectProvider<MetadataResolver> casSamlIdPMetadataResolver;

    @Autowired
    @Qualifier("shibbolethCompatiblePersistentIdGenerator")
    private ObjectProvider<PersistentIdGenerator> shibbolethCompatiblePersistentIdGenerator;

    @Autowired
    @Qualifier("servicesManager")
    private ObjectProvider<ServicesManager> servicesManager;

    @Autowired
    @Qualifier(OpenSamlConfigBean.DEFAULT_BEAN_NAME)
    private ObjectProvider<OpenSamlConfigBean> openSamlConfigBean;

    @Autowired
    @Qualifier("shibboleth.VelocityEngine")
    private ObjectProvider<VelocityEngine> velocityEngineFactory;

    @Autowired
    @Qualifier("samlIdPServiceFactory")
    private ObjectProvider<ServiceFactory> samlIdPServiceFactory;

    @Autowired
    @Qualifier("samlIdPMetadataLocator")
    private ObjectProvider<SamlIdPMetadataLocator> samlIdPMetadataLocator;

    @Autowired
    @Qualifier(AttributeDefinitionStore.BEAN_NAME)
    private ObjectProvider<AttributeDefinitionStore> attributeDefinitionStore;

    @ConditionalOnMissingBean(name = "samlSingleLogoutServiceLogoutUrlBuilderConfigurer")
    @Bean
    @RefreshScope
    public SingleLogoutServiceLogoutUrlBuilderConfigurer samlSingleLogoutServiceLogoutUrlBuilderConfigurer() {
        return () -> new SamlIdPSingleLogoutServiceLogoutUrlBuilder(servicesManager.getObject(),
            defaultSamlRegisteredServiceCachingMetadataResolver.getObject());
    }

    @ConditionalOnMissingBean(name = "samlProfileSamlResponseBuilder")
    @Bean
    @RefreshScope
    public SamlProfileObjectBuilder<org.opensaml.saml.saml2.core.Response> samlProfileSamlResponseBuilder() {
        return new SamlProfileSaml2ResponseBuilder(getSamlResponseBuilderConfigurationContextBuilder().build());
    }

    @ConditionalOnMissingBean(name = "samlArtifactTicketFactory")
    @Bean
    @RefreshScope
    public SamlArtifactTicketFactory samlArtifactTicketFactory() {
        return new DefaultSamlArtifactTicketFactory(samlArtifactTicketExpirationPolicy(),
            openSamlConfigBean.getObject(),
            samlIdPServiceFactory.getObject());
    }

    @ConditionalOnMissingBean(name = "samlArtifactTicketFactoryConfigurer")
    @Bean
    @RefreshScope
    public TicketFactoryExecutionPlanConfigurer samlArtifactTicketFactoryConfigurer() {
        return this::samlArtifactTicketFactory;
    }

    @ConditionalOnMissingBean(name = "samlArtifactTicketExpirationPolicy")
    @Bean
    @RefreshScope
    public ExpirationPolicyBuilder samlArtifactTicketExpirationPolicy() {
        return new SamlArtifactTicketExpirationPolicyBuilder(casProperties);
    }

    @Bean(initMethod = "initialize", destroyMethod = "destroy")
    @RefreshScope
    public SAMLArtifactMap samlArtifactMap() {
        val map = new CasSamlArtifactMap(ticketRegistry.getObject(),
            samlArtifactTicketFactory(),
            ticketGrantingTicketCookieGenerator.getObject(),
            samlIdPDistributedSessionStore.getObject(),
            centralAuthenticationService.getObject());
        val expirationPolicy = samlArtifactTicketExpirationPolicy().buildTicketExpirationPolicy();
        map.setArtifactLifetime(Duration.ofSeconds(expirationPolicy.getTimeToLive()));
        return map;
    }

    @ConditionalOnMissingBean(name = "samlProfileSamlSubjectBuilder")
    @Bean
    @RefreshScope
    public SamlProfileObjectBuilder<Subject> samlProfileSamlSubjectBuilder() {
        return new SamlProfileSamlSubjectBuilder(openSamlConfigBean.getObject(),
            samlProfileSamlNameIdBuilder(),
            casProperties,
            samlObjectEncrypter());
    }

    @ConditionalOnMissingBean(name = "samlProfileSamlSoap11FaultResponseBuilder")
    @Bean
    @RefreshScope
    public SamlProfileObjectBuilder<Response> samlProfileSamlSoap11FaultResponseBuilder() {
        val context = getSamlResponseBuilderConfigurationContextBuilder()
            .samlSoapResponseBuilder(samlProfileSamlResponseBuilder())
            .build();
        return new SamlProfileSamlSoap11FaultResponseBuilder(context);
    }

    @ConditionalOnMissingBean(name = "samlProfileSamlSoap11ResponseBuilder")
    @Bean
    @RefreshScope
    public SamlProfileObjectBuilder<Response> samlProfileSamlSoap11ResponseBuilder() {
        val context = getSamlResponseBuilderConfigurationContextBuilder()
            .samlSoapResponseBuilder(samlProfileSamlResponseBuilder())
            .build();
        return new SamlProfileSamlSoap11ResponseBuilder(context);
    }


    @ConditionalOnMissingBean(name = "samlProfileSamlArtifactFaultResponseBuilder")
    @Bean
    @RefreshScope
    public SamlProfileObjectBuilder<org.opensaml.saml.saml2.core.Response> samlProfileSamlArtifactFaultResponseBuilder() {
        val context = getSamlResponseBuilderConfigurationContextBuilder()
            .samlSoapResponseBuilder(samlProfileSamlResponseBuilder())
            .build();
        return new SamlProfileArtifactFaultResponseBuilder(context);
    }

    @ConditionalOnMissingBean(name = "samlProfileSamlArtifactResponseBuilder")
    @Bean
    @RefreshScope
    public SamlProfileObjectBuilder<org.opensaml.saml.saml2.core.Response> samlProfileSamlArtifactResponseBuilder() {
        val context = getSamlResponseBuilderConfigurationContextBuilder()
            .samlSoapResponseBuilder(samlProfileSamlResponseBuilder())
            .build();
        return new SamlProfileArtifactResponseBuilder(context);
    }

    @ConditionalOnMissingBean(name = "samlProfileSamlNameIdBuilder")
    @Bean
    @RefreshScope
    public SamlProfileObjectBuilder<NameID> samlProfileSamlNameIdBuilder() {
        return new SamlProfileSamlNameIdBuilder(openSamlConfigBean.getObject(),
            shibbolethCompatiblePersistentIdGenerator.getObject());
    }

    @ConditionalOnMissingBean(name = "samlProfileSamlConditionsBuilder")
    @Bean
    @RefreshScope
    public SamlProfileObjectBuilder<Conditions> samlProfileSamlConditionsBuilder() {
        return new SamlProfileSamlConditionsBuilder(openSamlConfigBean.getObject(), casProperties);
    }

    @ConditionalOnMissingBean(name = "defaultAuthnContextClassRefBuilder")
    @Bean
    @RefreshScope
    public AuthnContextClassRefBuilder defaultAuthnContextClassRefBuilder() {
        return new DefaultAuthnContextClassRefBuilder(casProperties);
    }

    @ConditionalOnMissingBean(name = "samlProfileSamlAssertionBuilder")
    @Bean
    @RefreshScope
    public SamlProfileObjectBuilder<Assertion> samlProfileSamlAssertionBuilder() {
        return new SamlProfileSamlAssertionBuilder(
            openSamlConfigBean.getObject(),
            casProperties,
            samlProfileSamlAuthNStatementBuilder(),
            samlProfileSamlAttributeStatementBuilder(),
            samlProfileSamlSubjectBuilder(),
            samlProfileSamlConditionsBuilder(),
            samlObjectSigner());
    }

    @ConditionalOnMissingBean(name = "samlProfileSamlAuthNStatementBuilder")
    @Bean
    @RefreshScope
    public SamlProfileObjectBuilder<AuthnStatement> samlProfileSamlAuthNStatementBuilder() {
        return new SamlProfileSamlAuthNStatementBuilder(openSamlConfigBean.getObject(), defaultAuthnContextClassRefBuilder(), casProperties);
    }

    @ConditionalOnMissingBean(name = "samlProfileSamlAttributeStatementBuilder")
    @Bean
    @RefreshScope
    public SamlProfileObjectBuilder<AttributeStatement> samlProfileSamlAttributeStatementBuilder() {
        return new SamlProfileSamlAttributeStatementBuilder(
            openSamlConfigBean.getObject(),
            casProperties.getAuthn().getSamlIdp(),
            samlObjectEncrypter(),
            attributeDefinitionStore.getObject(),
            samlIdPServiceFactory.getObject());
    }

    @ConditionalOnMissingBean(name = "samlObjectEncrypter")
    @Bean
    @RefreshScope
    public SamlIdPObjectEncrypter samlObjectEncrypter() {
        return new SamlIdPObjectEncrypter(casProperties.getAuthn().getSamlIdp());
    }

    @ConditionalOnMissingBean(name = "samlObjectSigner")
    @Bean
    @RefreshScope
    public SamlIdPObjectSigner samlObjectSigner() {
        return new SamlIdPObjectSigner(
            casSamlIdPMetadataResolver.getObject(),
            casProperties,
            samlIdPMetadataLocator.getObject());
    }

    @ConditionalOnMissingBean(name = "samlProfileSamlAttributeQueryFaultResponseBuilder")
    @Bean
    @RefreshScope
    public SamlProfileObjectBuilder<org.opensaml.saml.saml2.core.Response> samlProfileSamlAttributeQueryFaultResponseBuilder() {
        val context = getSamlResponseBuilderConfigurationContextBuilder()
            .samlSoapResponseBuilder(samlProfileSamlResponseBuilder())
            .build();
        return new SamlProfileAttributeQueryFaultResponseBuilder(context);
    }

    @ConditionalOnMissingBean(name = "samlProfileSamlAttributeQueryResponseBuilder")
    @Bean
    @RefreshScope
    public SamlProfileObjectBuilder<org.opensaml.saml.saml2.core.Response> samlProfileSamlAttributeQueryResponseBuilder() {
        val context = getSamlResponseBuilderConfigurationContextBuilder()
            .samlSoapResponseBuilder(samlProfileSamlResponseBuilder())
            .build();
        return new SamlProfileAttributeQueryResponseBuilder(context);
    }

    @ConditionalOnMissingBean(name = "samlAttributeQueryTicketFactory")
    @Bean
    @RefreshScope
    public SamlAttributeQueryTicketFactory samlAttributeQueryTicketFactory() {
        return new DefaultSamlAttributeQueryTicketFactory(
            samlAttributeQueryTicketExpirationPolicy(),
            samlIdPServiceFactory.getObject(),
            openSamlConfigBean.getObject());
    }

    @ConditionalOnMissingBean(name = "samlAttributeQueryTicketFactoryConfigurer")
    @Bean
    @RefreshScope
    public TicketFactoryExecutionPlanConfigurer samlAttributeQueryTicketFactoryConfigurer() {
        return this::samlAttributeQueryTicketFactory;
    }

    @ConditionalOnMissingBean(name = "samlAttributeQueryTicketExpirationPolicy")
    @Bean
    @RefreshScope
    public ExpirationPolicyBuilder samlAttributeQueryTicketExpirationPolicy() {
        return new SamlAttributeQueryTicketExpirationPolicyBuilder(casProperties);
    }

    @Bean
    public SamlResponseAuditPrincipalIdProvider samlResponseAuditPrincipalIdProvider() {
        return new SamlResponseAuditPrincipalIdProvider();
    }

    @Bean
    @ConditionalOnMissingBean(name = "casSamlIdPAuditTrailRecordResolutionPlanConfigurer")
    public AuditTrailRecordResolutionPlanConfigurer casSamlIdPAuditTrailRecordResolutionPlanConfigurer() {
        return plan -> {
            plan.registerAuditResourceResolver(AuditResourceResolvers.SAML2_RESPONSE_RESOURCE_RESOLVER, new SamlResponseAuditResourceResolver());
            plan.registerAuditActionResolver(AuditActionResolvers.SAML2_RESPONSE_ACTION_RESOLVER,
                new DefaultAuditActionResolver(AuditTrailConstants.AUDIT_ACTION_POSTFIX_CREATED, AuditTrailConstants.AUDIT_ACTION_POSTFIX_CREATED));

            plan.registerAuditResourceResolver(AuditResourceResolvers.SAML2_REQUEST_RESOURCE_RESOLVER, new SamlRequestAuditResourceResolver());
            plan.registerAuditActionResolver(AuditActionResolvers.SAML2_REQUEST_ACTION_RESOLVER,
                new DefaultAuditActionResolver(AuditTrailConstants.AUDIT_ACTION_POSTFIX_CREATED, AuditTrailConstants.AUDIT_ACTION_POSTFIX_CREATED));
        };
    }

    @ConditionalOnClass(value = JpaPersistenceProviderConfigurer.class)
    @Configuration("samlIdPJpaServiceRegistryConfiguration")
    public static class SamlIdPJpaServiceRegistryConfiguration {
        @Bean
        @ConditionalOnMissingBean(name = "samlIdPJpaServicePersistenceProviderConfigurer")
        public JpaPersistenceProviderConfigurer samlIdPJpaServicePersistenceProviderConfigurer() {
            return context -> context.getIncludeEntityClasses().addAll(List.of(SamlRegisteredService.class.getName()));
        }
    }

    private SamlProfileSamlResponseBuilderConfigurationContext.
        SamlProfileSamlResponseBuilderConfigurationContextBuilder getSamlResponseBuilderConfigurationContextBuilder() {

        return SamlProfileSamlResponseBuilderConfigurationContext.builder()
            .openSamlConfigBean(openSamlConfigBean.getObject())
            .samlObjectSigner(samlObjectSigner())
            .velocityEngineFactory(velocityEngineFactory.getObject())
            .samlProfileSamlAssertionBuilder(samlProfileSamlAssertionBuilder())
            .samlObjectEncrypter(samlObjectEncrypter())
            .ticketGrantingTicketCookieGenerator(ticketGrantingTicketCookieGenerator.getObject())
            .ticketRegistry(ticketRegistry.getObject())
            .sessionStore(samlIdPDistributedSessionStore.getObject())
            .samlArtifactTicketFactory(samlArtifactTicketFactory())
            .samlArtifactMap(samlArtifactMap())
            .centralAuthenticationService(centralAuthenticationService.getObject())
            .samlAttributeQueryTicketFactory(samlAttributeQueryTicketFactory())
            .casProperties(casProperties);
    }
}
