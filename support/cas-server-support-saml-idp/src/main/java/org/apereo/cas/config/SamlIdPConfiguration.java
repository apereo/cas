package org.apereo.cas.config;

import org.apereo.cas.audit.AuditTrailConstants;
import org.apereo.cas.audit.AuditTrailRecordResolutionPlanConfigurer;
import org.apereo.cas.authentication.AuthenticationServiceSelectionPlan;
import org.apereo.cas.authentication.ProtocolAttributeEncoder;
import org.apereo.cas.authentication.attribute.AttributeDefinitionStore;
import org.apereo.cas.authentication.principal.PersistentIdGenerator;
import org.apereo.cas.authentication.principal.ServiceFactory;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.logout.LogoutExecutionPlanConfigurer;
import org.apereo.cas.logout.slo.SingleLogoutMessageCreator;
import org.apereo.cas.logout.slo.SingleLogoutServiceLogoutUrlBuilder;
import org.apereo.cas.logout.slo.SingleLogoutServiceMessageHandler;
import org.apereo.cas.services.ServicesManager;
import org.apereo.cas.support.saml.OpenSamlConfigBean;
import org.apereo.cas.support.saml.idp.metadata.locator.SamlIdPMetadataLocator;
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
import org.apereo.cas.support.saml.web.idp.profile.builders.enc.attribute.SamlAttributeEncoder;
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
import org.apereo.cas.support.saml.web.idp.profile.slo.SamlIdPProfileSingleLogoutMessageCreator;
import org.apereo.cas.support.saml.web.idp.profile.slo.SamlIdPSingleLogoutServiceLogoutUrlBuilder;
import org.apereo.cas.support.saml.web.idp.profile.slo.SamlIdPSingleLogoutServiceMessageHandler;
import org.apereo.cas.ticket.ExpirationPolicyBuilder;
import org.apereo.cas.ticket.artifact.DefaultSamlArtifactTicketFactory;
import org.apereo.cas.ticket.artifact.SamlArtifactTicketExpirationPolicyBuilder;
import org.apereo.cas.ticket.artifact.SamlArtifactTicketFactory;
import org.apereo.cas.ticket.query.DefaultSamlAttributeQueryTicketFactory;
import org.apereo.cas.ticket.query.SamlAttributeQueryTicketExpirationPolicyBuilder;
import org.apereo.cas.ticket.query.SamlAttributeQueryTicketFactory;
import org.apereo.cas.ticket.registry.TicketRegistry;
import org.apereo.cas.util.http.HttpClient;
import org.apereo.cas.web.UrlValidator;
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
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Duration;

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
    private CasConfigurationProperties casProperties;

    @Autowired
    @Qualifier("defaultSamlRegisteredServiceCachingMetadataResolver")
    private ObjectProvider<SamlRegisteredServiceCachingMetadataResolver> defaultSamlRegisteredServiceCachingMetadataResolver;

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
    @Qualifier("shibboleth.OpenSAMLConfig")
    private ObjectProvider<OpenSamlConfigBean> openSamlConfigBean;

    @Autowired
    @Qualifier("shibboleth.VelocityEngine")
    private ObjectProvider<VelocityEngine> velocityEngineFactory;

    @Autowired
    @Qualifier("samlIdPServiceFactory")
    private ObjectProvider<ServiceFactory> samlIdPServiceFactory;

    @Autowired
    @Qualifier("urlValidator")
    private ObjectProvider<UrlValidator> urlValidator;

    @Autowired
    @Qualifier("samlIdPMetadataLocator")
    private ObjectProvider<SamlIdPMetadataLocator> samlIdPMetadataLocator;

    @Autowired
    @Qualifier("noRedirectHttpClient")
    private ObjectProvider<HttpClient> httpClient;

    @Autowired
    @Qualifier("authenticationServiceSelectionPlan")
    private ObjectProvider<AuthenticationServiceSelectionPlan> authenticationServiceSelectionPlan;

    @Autowired
    @Qualifier("attributeDefinitionStore")
    private ObjectProvider<AttributeDefinitionStore> attributeDefinitionStore;

    @ConditionalOnMissingBean(name = "samlSingleLogoutServiceLogoutUrlBuilder")
    @Bean
    public SingleLogoutServiceLogoutUrlBuilder samlSingleLogoutServiceLogoutUrlBuilder() {
        return new SamlIdPSingleLogoutServiceLogoutUrlBuilder(servicesManager.getObject(),
            defaultSamlRegisteredServiceCachingMetadataResolver.getObject(),
            urlValidator.getObject());
    }

    @ConditionalOnMissingBean(name = "samlLogoutBuilder")
    @Bean
    public SingleLogoutMessageCreator samlLogoutBuilder() {
        return new SamlIdPProfileSingleLogoutMessageCreator(
            openSamlConfigBean.getObject(),
            servicesManager.getObject(),
            defaultSamlRegisteredServiceCachingMetadataResolver.getObject(),
            casProperties.getAuthn().getSamlIdp(),
            samlObjectSigner());
    }

    @ConditionalOnMissingBean(name = "samlSingleLogoutServiceMessageHandler")
    @Bean
    public SingleLogoutServiceMessageHandler samlSingleLogoutServiceMessageHandler() {
        return new SamlIdPSingleLogoutServiceMessageHandler(httpClient.getObject(),
            samlLogoutBuilder(),
            servicesManager.getObject(),
            samlSingleLogoutServiceLogoutUrlBuilder(),
            casProperties.getSlo().isAsynchronous(),
            authenticationServiceSelectionPlan.getObject(),
            defaultSamlRegisteredServiceCachingMetadataResolver.getObject(),
            velocityEngineFactory.getObject());
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
            ticketGrantingTicketCookieGenerator.getObject());
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
            samlAttributeEncoder(),
            casProperties.getAuthn().getSamlIdp(),
            samlObjectEncrypter(),
            attributeDefinitionStore.getObject());
    }

    @ConditionalOnMissingBean(name = "samlAttributeEncoder")
    @Bean
    @RefreshScope
    public ProtocolAttributeEncoder samlAttributeEncoder() {
        return new SamlAttributeEncoder();
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
            this.casSamlIdPMetadataResolver.getObject(),
            casProperties,
            this.samlIdPMetadataLocator.getObject());
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
        return new DefaultSamlAttributeQueryTicketFactory(samlAttributeQueryTicketExpirationPolicy(),
            samlIdPServiceFactory.getObject(),
            openSamlConfigBean.getObject());
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
    public LogoutExecutionPlanConfigurer casSamlIdPLogoutExecutionPlanConfigurer() {
        return plan -> plan.registerSingleLogoutServiceMessageHandler(samlSingleLogoutServiceMessageHandler());
    }

    @Bean
    public AuditTrailRecordResolutionPlanConfigurer casSamlIdPAuditTrailRecordResolutionPlanConfigurer() {
        return plan -> {
            plan.registerAuditResourceResolver("SAML2_RESPONSE_RESOURCE_RESOLVER", new SamlResponseAuditResourceResolver());
            plan.registerAuditActionResolver("SAML2_RESPONSE_ACTION_RESOLVER",
                new DefaultAuditActionResolver(AuditTrailConstants.AUDIT_ACTION_POSTFIX_CREATED, AuditTrailConstants.AUDIT_ACTION_POSTFIX_CREATED));

            plan.registerAuditResourceResolver("SAML2_REQUEST_RESOURCE_RESOLVER", new SamlRequestAuditResourceResolver());
            plan.registerAuditActionResolver("SAML2_REQUEST_ACTION_RESOLVER",
                new DefaultAuditActionResolver(AuditTrailConstants.AUDIT_ACTION_POSTFIX_CREATED, AuditTrailConstants.AUDIT_ACTION_POSTFIX_CREATED));
        };
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
            .samlArtifactTicketFactory(samlArtifactTicketFactory())
            .samlArtifactMap(samlArtifactMap())
            .samlAttributeQueryTicketFactory(samlAttributeQueryTicketFactory())
            .casProperties(casProperties);
    }
}
