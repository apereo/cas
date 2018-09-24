package org.apereo.cas.config;

import org.apereo.cas.audit.AuditTrailConstants;
import org.apereo.cas.audit.AuditTrailRecordResolutionPlan;
import org.apereo.cas.audit.AuditTrailRecordResolutionPlanConfigurer;
import org.apereo.cas.authentication.AuthenticationServiceSelectionPlan;
import org.apereo.cas.authentication.ProtocolAttributeEncoder;
import org.apereo.cas.authentication.principal.PersistentIdGenerator;
import org.apereo.cas.authentication.principal.ServiceFactory;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.logout.LogoutExecutionPlan;
import org.apereo.cas.logout.LogoutExecutionPlanConfigurer;
import org.apereo.cas.logout.LogoutMessageCreator;
import org.apereo.cas.logout.SingleLogoutServiceLogoutUrlBuilder;
import org.apereo.cas.logout.SingleLogoutServiceMessageHandler;
import org.apereo.cas.services.ServicesManager;
import org.apereo.cas.support.saml.OpenSamlConfigBean;
import org.apereo.cas.support.saml.idp.metadata.locator.SamlIdPMetadataLocator;
import org.apereo.cas.support.saml.services.idp.metadata.cache.SamlRegisteredServiceCachingMetadataResolver;
import org.apereo.cas.support.saml.services.logout.SamlIdPSingleLogoutServiceLogoutUrlBuilder;
import org.apereo.cas.support.saml.services.logout.SamlIdPSingleLogoutServiceMessageHandler;
import org.apereo.cas.support.saml.services.logout.SamlProfileLogoutMessageCreator;
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
import org.apereo.cas.support.saml.web.idp.profile.builders.enc.SamlAttributeEncoder;
import org.apereo.cas.support.saml.web.idp.profile.builders.enc.SamlIdPObjectSigner;
import org.apereo.cas.support.saml.web.idp.profile.builders.enc.SamlObjectEncrypter;
import org.apereo.cas.support.saml.web.idp.profile.builders.nameid.SamlProfileSamlNameIdBuilder;
import org.apereo.cas.support.saml.web.idp.profile.builders.response.SamlProfileSaml2ResponseBuilder;
import org.apereo.cas.support.saml.web.idp.profile.builders.response.artifact.SamlProfileArtifactFaultResponseBuilder;
import org.apereo.cas.support.saml.web.idp.profile.builders.response.artifact.SamlProfileArtifactResponseBuilder;
import org.apereo.cas.support.saml.web.idp.profile.builders.response.query.SamlProfileAttributeQueryFaultResponseBuilder;
import org.apereo.cas.support.saml.web.idp.profile.builders.response.query.SamlProfileAttributeQueryResponseBuilder;
import org.apereo.cas.support.saml.web.idp.profile.builders.response.soap.SamlProfileSamlSoap11FaultResponseBuilder;
import org.apereo.cas.support.saml.web.idp.profile.builders.response.soap.SamlProfileSamlSoap11ResponseBuilder;
import org.apereo.cas.support.saml.web.idp.profile.builders.subject.SamlProfileSamlSubjectBuilder;
import org.apereo.cas.ticket.ExpirationPolicy;
import org.apereo.cas.ticket.artifact.DefaultSamlArtifactTicketFactory;
import org.apereo.cas.ticket.artifact.SamlArtifactTicketExpirationPolicy;
import org.apereo.cas.ticket.artifact.SamlArtifactTicketFactory;
import org.apereo.cas.ticket.query.DefaultSamlAttributeQueryTicketFactory;
import org.apereo.cas.ticket.query.SamlAttributeQueryTicketExpirationPolicy;
import org.apereo.cas.ticket.query.SamlAttributeQueryTicketFactory;
import org.apereo.cas.ticket.registry.TicketRegistry;
import org.apereo.cas.util.http.HttpClient;
import org.apereo.cas.web.UrlValidator;
import org.apereo.cas.web.support.CookieRetrievingCookieGenerator;

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

import java.util.concurrent.TimeUnit;

/**
 * The {@link SamlIdPConfiguration}.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 */
@Configuration("samlIdPConfiguration")
@EnableConfigurationProperties(CasConfigurationProperties.class)
public class SamlIdPConfiguration implements AuditTrailRecordResolutionPlanConfigurer, LogoutExecutionPlanConfigurer {

    @Autowired
    @Qualifier("ticketGrantingTicketCookieGenerator")
    private ObjectProvider<CookieRetrievingCookieGenerator> ticketGrantingTicketCookieGenerator;

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
    @Qualifier("webApplicationServiceFactory")
    private ObjectProvider<ServiceFactory> webApplicationServiceFactory;

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

    @ConditionalOnMissingBean(name = "samlSingleLogoutServiceLogoutUrlBuilder")
    @Bean
    public SingleLogoutServiceLogoutUrlBuilder samlSingleLogoutServiceLogoutUrlBuilder() {
        return new SamlIdPSingleLogoutServiceLogoutUrlBuilder(servicesManager.getIfAvailable(),
            defaultSamlRegisteredServiceCachingMetadataResolver.getIfAvailable(),
            urlValidator.getIfAvailable());
    }

    @ConditionalOnMissingBean(name = "samlLogoutBuilder")
    @Bean
    public LogoutMessageCreator samlLogoutBuilder() {
        return new SamlProfileLogoutMessageCreator(
            openSamlConfigBean.getObject(),
            servicesManager.getObject(),
            defaultSamlRegisteredServiceCachingMetadataResolver.getObject(),
            casProperties.getAuthn().getSamlIdp());
    }

    @ConditionalOnMissingBean(name = "samlSingleLogoutServiceMessageHandler")
    @Bean
    public SingleLogoutServiceMessageHandler samlSingleLogoutServiceMessageHandler() {
        return new SamlIdPSingleLogoutServiceMessageHandler(httpClient.getIfAvailable(),
            samlLogoutBuilder(),
            servicesManager.getIfAvailable(),
            samlSingleLogoutServiceLogoutUrlBuilder(),
            casProperties.getSlo().isAsynchronous(),
            authenticationServiceSelectionPlan.getIfAvailable());
    }

    @ConditionalOnMissingBean(name = "samlProfileSamlResponseBuilder")
    @Bean
    @RefreshScope
    public SamlProfileObjectBuilder<org.opensaml.saml.saml2.core.Response> samlProfileSamlResponseBuilder() {
        return new SamlProfileSaml2ResponseBuilder(
            openSamlConfigBean.getIfAvailable(),
            samlObjectSigner(),
            velocityEngineFactory.getIfAvailable(),
            samlProfileSamlAssertionBuilder(),
            samlObjectEncrypter(),
            ticketRegistry.getIfAvailable(),
            samlArtifactTicketFactory(),
            ticketGrantingTicketCookieGenerator.getIfAvailable(),
            samlArtifactMap(),
            samlAttributeQueryTicketFactory());
    }

    @ConditionalOnMissingBean(name = "samlArtifactTicketFactory")
    @Bean
    @RefreshScope
    public SamlArtifactTicketFactory samlArtifactTicketFactory() {
        return new DefaultSamlArtifactTicketFactory(samlArtifactTicketExpirationPolicy(),
            openSamlConfigBean.getIfAvailable(),
            webApplicationServiceFactory.getIfAvailable());
    }

    @ConditionalOnMissingBean(name = "samlArtifactTicketExpirationPolicy")
    @Bean
    public ExpirationPolicy samlArtifactTicketExpirationPolicy() {
        return new SamlArtifactTicketExpirationPolicy(casProperties.getTicket().getSt().getTimeToKillInSeconds());
    }

    @Bean(initMethod = "initialize", destroyMethod = "destroy")
    @RefreshScope
    public SAMLArtifactMap samlArtifactMap() {
        val map = new CasSamlArtifactMap(ticketRegistry.getIfAvailable(),
            samlArtifactTicketFactory(),
            ticketGrantingTicketCookieGenerator.getIfAvailable());
        map.setArtifactLifetime(TimeUnit.SECONDS.toMillis(samlArtifactTicketExpirationPolicy().getTimeToLive()));
        return map;
    }

    @ConditionalOnMissingBean(name = "samlProfileSamlSubjectBuilder")
    @Bean
    @RefreshScope
    public SamlProfileObjectBuilder<Subject> samlProfileSamlSubjectBuilder() {
        return new SamlProfileSamlSubjectBuilder(openSamlConfigBean.getIfAvailable(),
            samlProfileSamlNameIdBuilder(),
            casProperties.getAuthn().getSamlIdp().getResponse().getSkewAllowance(),
            samlObjectEncrypter());
    }

    @ConditionalOnMissingBean(name = "samlProfileSamlSoap11FaultResponseBuilder")
    @Bean
    @RefreshScope
    public SamlProfileObjectBuilder<Response> samlProfileSamlSoap11FaultResponseBuilder() {
        return new SamlProfileSamlSoap11FaultResponseBuilder(
            openSamlConfigBean.getIfAvailable(),
            samlObjectSigner(),
            velocityEngineFactory.getIfAvailable(),
            samlProfileSamlAssertionBuilder(),
            samlProfileSamlResponseBuilder(),
            samlObjectEncrypter());
    }

    @ConditionalOnMissingBean(name = "samlProfileSamlSoap11ResponseBuilder")
    @Bean
    @RefreshScope
    public SamlProfileObjectBuilder<Response> samlProfileSamlSoap11ResponseBuilder() {
        return new SamlProfileSamlSoap11ResponseBuilder(
            openSamlConfigBean.getIfAvailable(),
            samlObjectSigner(),
            velocityEngineFactory.getIfAvailable(),
            samlProfileSamlAssertionBuilder(),
            samlProfileSamlResponseBuilder(),
            samlObjectEncrypter());
    }


    @ConditionalOnMissingBean(name = "samlProfileSamlArtifactFaultResponseBuilder")
    @Bean
    @RefreshScope
    public SamlProfileObjectBuilder<org.opensaml.saml.saml2.core.Response> samlProfileSamlArtifactFaultResponseBuilder() {
        return new SamlProfileArtifactFaultResponseBuilder(
            openSamlConfigBean.getIfAvailable(),
            samlObjectSigner(),
            velocityEngineFactory.getIfAvailable(),
            samlProfileSamlAssertionBuilder(),
            samlProfileSamlResponseBuilder(),
            samlObjectEncrypter());
    }

    @ConditionalOnMissingBean(name = "samlProfileSamlArtifactResponseBuilder")
    @Bean
    @RefreshScope
    public SamlProfileObjectBuilder<org.opensaml.saml.saml2.core.Response> samlProfileSamlArtifactResponseBuilder() {
        return new SamlProfileArtifactResponseBuilder(
            openSamlConfigBean.getIfAvailable(),
            samlObjectSigner(),
            velocityEngineFactory.getIfAvailable(),
            samlProfileSamlAssertionBuilder(),
            samlProfileSamlResponseBuilder(),
            samlObjectEncrypter());
    }

    @ConditionalOnMissingBean(name = "samlProfileSamlNameIdBuilder")
    @Bean
    @RefreshScope
    public SamlProfileObjectBuilder<NameID> samlProfileSamlNameIdBuilder() {
        return new SamlProfileSamlNameIdBuilder(openSamlConfigBean.getIfAvailable(),
            shibbolethCompatiblePersistentIdGenerator.getIfAvailable(),
            samlObjectEncrypter());
    }

    @ConditionalOnMissingBean(name = "samlProfileSamlConditionsBuilder")
    @Bean
    @RefreshScope
    public SamlProfileObjectBuilder<Conditions> samlProfileSamlConditionsBuilder() {
        return new SamlProfileSamlConditionsBuilder(openSamlConfigBean.getIfAvailable());
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
            openSamlConfigBean.getIfAvailable(),
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
        return new SamlProfileSamlAuthNStatementBuilder(openSamlConfigBean.getIfAvailable(), defaultAuthnContextClassRefBuilder());
    }

    @ConditionalOnMissingBean(name = "samlProfileSamlAttributeStatementBuilder")
    @Bean
    @RefreshScope
    public SamlProfileObjectBuilder<AttributeStatement> samlProfileSamlAttributeStatementBuilder() {
        return new SamlProfileSamlAttributeStatementBuilder(
            openSamlConfigBean.getIfAvailable(),
            samlAttributeEncoder(),
            casProperties.getAuthn().getSamlIdp(),
            samlObjectEncrypter());
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
    public SamlObjectEncrypter samlObjectEncrypter() {
        return new SamlObjectEncrypter(casProperties.getAuthn().getSamlIdp());
    }

    @ConditionalOnMissingBean(name = "samlObjectSigner")
    @Bean
    @RefreshScope
    public SamlIdPObjectSigner samlObjectSigner() {
        return new SamlIdPObjectSigner(
            this.casSamlIdPMetadataResolver.getIfAvailable(),
            casProperties,
            this.samlIdPMetadataLocator.getIfAvailable());
    }

    @ConditionalOnMissingBean(name = "samlProfileSamlAttributeQueryFaultResponseBuilder")
    @Bean
    @RefreshScope
    public SamlProfileObjectBuilder<org.opensaml.saml.saml2.core.Response> samlProfileSamlAttributeQueryFaultResponseBuilder() {
        return new SamlProfileAttributeQueryFaultResponseBuilder(
            openSamlConfigBean.getIfAvailable(),
            samlObjectSigner(),
            velocityEngineFactory.getIfAvailable(),
            samlProfileSamlAssertionBuilder(),
            samlProfileSamlResponseBuilder(),
            samlObjectEncrypter());
    }

    @ConditionalOnMissingBean(name = "samlProfileSamlAttributeQueryResponseBuilder")
    @Bean
    @RefreshScope
    public SamlProfileObjectBuilder<org.opensaml.saml.saml2.core.Response> samlProfileSamlAttributeQueryResponseBuilder() {
        return new SamlProfileAttributeQueryResponseBuilder(
            openSamlConfigBean.getIfAvailable(),
            samlObjectSigner(),
            velocityEngineFactory.getIfAvailable(),
            samlProfileSamlAssertionBuilder(),
            samlProfileSamlResponseBuilder(),
            samlObjectEncrypter());
    }

    @ConditionalOnMissingBean(name = "samlAttributeQueryTicketFactory")
    @Bean
    @RefreshScope
    public SamlAttributeQueryTicketFactory samlAttributeQueryTicketFactory() {
        return new DefaultSamlAttributeQueryTicketFactory(samlAttributeQueryTicketExpirationPolicy(),
            webApplicationServiceFactory.getIfAvailable(),
            openSamlConfigBean.getIfAvailable());
    }

    @ConditionalOnMissingBean(name = "samlAttributeQueryTicketExpirationPolicy")
    @Bean
    public ExpirationPolicy samlAttributeQueryTicketExpirationPolicy() {
        return new SamlAttributeQueryTicketExpirationPolicy(casProperties.getTicket().getSt().getTimeToKillInSeconds());
    }

    @Bean
    public SamlResponseAuditPrincipalIdProvider samlResponseAuditPrincipalIdProvider() {
        return new SamlResponseAuditPrincipalIdProvider();
    }

    @Override
    public void configureLogoutExecutionPlan(final LogoutExecutionPlan plan) {
        plan.registerSingleLogoutServiceMessageHandler(samlSingleLogoutServiceMessageHandler());
    }

    @Override
    public void configureAuditTrailRecordResolutionPlan(final AuditTrailRecordResolutionPlan plan) {
        plan.registerAuditResourceResolver("SAML2_RESPONSE_RESOURCE_RESOLVER", new SamlResponseAuditResourceResolver());
        plan.registerAuditActionResolver("SAML2_RESPONSE_ACTION_RESOLVER",
            new DefaultAuditActionResolver(AuditTrailConstants.AUDIT_ACTION_POSTFIX_CREATED, AuditTrailConstants.AUDIT_ACTION_POSTFIX_CREATED));

        plan.registerAuditResourceResolver("SAML2_REQUEST_RESOURCE_RESOLVER", new SamlRequestAuditResourceResolver());
        plan.registerAuditActionResolver("SAML2_REQUEST_ACTION_RESOLVER",
            new DefaultAuditActionResolver(AuditTrailConstants.AUDIT_ACTION_POSTFIX_CREATED, AuditTrailConstants.AUDIT_ACTION_POSTFIX_CREATED));
    }

}
