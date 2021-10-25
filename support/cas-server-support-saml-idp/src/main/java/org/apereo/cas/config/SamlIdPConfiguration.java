package org.apereo.cas.config;

import org.apereo.cas.CentralAuthenticationService;
import org.apereo.cas.audit.AuditActionResolvers;
import org.apereo.cas.audit.AuditPrincipalIdProvider;
import org.apereo.cas.audit.AuditResourceResolvers;
import org.apereo.cas.audit.AuditTrailConstants;
import org.apereo.cas.audit.AuditTrailRecordResolutionPlanConfigurer;
import org.apereo.cas.authentication.attribute.AttributeDefinitionStore;
import org.apereo.cas.authentication.principal.PersistentIdGenerator;
import org.apereo.cas.authentication.principal.ServiceFactory;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.logout.slo.SingleLogoutServiceLogoutUrlBuilder;
import org.apereo.cas.logout.slo.SingleLogoutServiceLogoutUrlBuilderConfigurer;
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
import org.apereo.cas.support.saml.web.idp.profile.builders.enc.DefaultSamlIdPObjectSigner;
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
import org.pac4j.core.context.session.SessionStore;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.ScopedProxyMode;

import java.time.Duration;

/**
 * The {@link SamlIdPConfiguration}.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 */
@EnableConfigurationProperties(CasConfigurationProperties.class)
@Configuration(value = "samlIdPConfiguration", proxyBeanMethods = false)
public class SamlIdPConfiguration {

    @Configuration(value = "SamlIdPProfileBuilderConfiguration", proxyBeanMethods = false)
    @EnableConfigurationProperties(CasConfigurationProperties.class)
    public static class SamlIdPProfileBuilderConfiguration {
        @ConditionalOnMissingBean(name = "samlProfileSamlAttributeQueryFaultResponseBuilder")
        @Bean
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        public SamlProfileObjectBuilder<org.opensaml.saml.saml2.core.Response> samlProfileSamlAttributeQueryFaultResponseBuilder(
            final CasConfigurationProperties casProperties,
            @Qualifier("casSamlIdPMetadataResolver")
            final MetadataResolver casSamlIdPMetadataResolver,
            @Qualifier(OpenSamlConfigBean.DEFAULT_BEAN_NAME)
            final OpenSamlConfigBean openSamlConfigBean,
            @Qualifier("samlObjectSigner")
            final SamlIdPObjectSigner samlObjectSigner,
            @Qualifier("velocityEngineFactory")
            final VelocityEngine velocityEngineFactory,
            @Qualifier("samlProfileSamlAssertionBuilder")
            final SamlProfileObjectBuilder<Assertion> samlProfileSamlAssertionBuilder,
            @Qualifier("samlObjectEncrypter")
            final SamlIdPObjectEncrypter samlObjectEncrypter,
            @Qualifier("ticketGrantingTicketCookieGenerator")
            final CasCookieBuilder ticketGrantingTicketCookieGenerator,
            @Qualifier(TicketRegistry.BEAN_NAME)
            final TicketRegistry ticketRegistry,
            @Qualifier("samlIdPDistributedSessionStore")
            final SessionStore samlIdPDistributedSessionStore,
            @Qualifier("samlArtifactTicketFactory")
            final SamlArtifactTicketFactory samlArtifactTicketFactory,
            @Qualifier("samlArtifactMap")
            final SAMLArtifactMap samlArtifactMap,
            @Qualifier(CentralAuthenticationService.BEAN_NAME)
            final CentralAuthenticationService centralAuthenticationService,
            @Qualifier("samlAttributeQueryTicketFactory")
            final SamlAttributeQueryTicketFactory samlAttributeQueryTicketFactory,
            @Qualifier("samlProfileSamlResponseBuilder")
            final SamlProfileObjectBuilder<org.opensaml.saml.saml2.core.Response> samlProfileSamlResponseBuilder) {
            val context = SamlProfileSamlResponseBuilderConfigurationContext.builder()
                .samlIdPMetadataResolver(casSamlIdPMetadataResolver)
                .openSamlConfigBean(openSamlConfigBean)
                .samlObjectSigner(samlObjectSigner)
                .velocityEngineFactory(velocityEngineFactory)
                .samlProfileSamlAssertionBuilder(samlProfileSamlAssertionBuilder)
                .samlObjectEncrypter(samlObjectEncrypter)
                .ticketGrantingTicketCookieGenerator(ticketGrantingTicketCookieGenerator)
                .ticketRegistry(ticketRegistry)
                .sessionStore(samlIdPDistributedSessionStore)
                .samlArtifactTicketFactory(samlArtifactTicketFactory)
                .samlArtifactMap(samlArtifactMap)
                .centralAuthenticationService(centralAuthenticationService)
                .samlAttributeQueryTicketFactory(samlAttributeQueryTicketFactory)
                .casProperties(casProperties)
                .samlSoapResponseBuilder(samlProfileSamlResponseBuilder)
                .build();
            return new SamlProfileAttributeQueryFaultResponseBuilder(context);
        }

        @ConditionalOnMissingBean(name = "samlProfileSamlAttributeQueryResponseBuilder")
        @Bean
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        public SamlProfileObjectBuilder<org.opensaml.saml.saml2.core.Response> samlProfileSamlAttributeQueryResponseBuilder(
            final CasConfigurationProperties casProperties,
            @Qualifier("casSamlIdPMetadataResolver")
            final MetadataResolver casSamlIdPMetadataResolver,
            @Qualifier(OpenSamlConfigBean.DEFAULT_BEAN_NAME)
            final OpenSamlConfigBean openSamlConfigBean,
            @Qualifier("samlObjectSigner")
            final SamlIdPObjectSigner samlObjectSigner,
            @Qualifier("velocityEngineFactory")
            final VelocityEngine velocityEngineFactory,
            @Qualifier("samlProfileSamlAssertionBuilder")
            final SamlProfileObjectBuilder<Assertion> samlProfileSamlAssertionBuilder,
            @Qualifier("samlObjectEncrypter")
            final SamlIdPObjectEncrypter samlObjectEncrypter,
            @Qualifier("ticketGrantingTicketCookieGenerator")
            final CasCookieBuilder ticketGrantingTicketCookieGenerator,
            @Qualifier(TicketRegistry.BEAN_NAME)
            final TicketRegistry ticketRegistry,
            @Qualifier("samlIdPDistributedSessionStore")
            final SessionStore samlIdPDistributedSessionStore,
            @Qualifier("samlArtifactTicketFactory")
            final SamlArtifactTicketFactory samlArtifactTicketFactory,
            @Qualifier("samlArtifactMap")
            final SAMLArtifactMap samlArtifactMap,
            @Qualifier(CentralAuthenticationService.BEAN_NAME)
            final CentralAuthenticationService centralAuthenticationService,
            @Qualifier("samlAttributeQueryTicketFactory")
            final SamlAttributeQueryTicketFactory samlAttributeQueryTicketFactory,
            @Qualifier("samlProfileSamlResponseBuilder")
            final SamlProfileObjectBuilder<org.opensaml.saml.saml2.core.Response> samlProfileSamlResponseBuilder) {
            val context = SamlProfileSamlResponseBuilderConfigurationContext.builder()
                .samlIdPMetadataResolver(casSamlIdPMetadataResolver)
                .openSamlConfigBean(openSamlConfigBean)
                .samlObjectSigner(samlObjectSigner)
                .velocityEngineFactory(velocityEngineFactory)
                .samlProfileSamlAssertionBuilder(samlProfileSamlAssertionBuilder)
                .samlObjectEncrypter(samlObjectEncrypter)
                .ticketGrantingTicketCookieGenerator(ticketGrantingTicketCookieGenerator)
                .ticketRegistry(ticketRegistry)
                .sessionStore(samlIdPDistributedSessionStore)
                .samlArtifactTicketFactory(samlArtifactTicketFactory)
                .samlArtifactMap(samlArtifactMap)
                .centralAuthenticationService(centralAuthenticationService)
                .samlAttributeQueryTicketFactory(samlAttributeQueryTicketFactory)
                .casProperties(casProperties)
                .samlSoapResponseBuilder(samlProfileSamlResponseBuilder)
                .build();
            return new SamlProfileAttributeQueryResponseBuilder(context);
        }


        @ConditionalOnMissingBean(name = "samlProfileSamlSubjectBuilder")
        @Bean
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        @Autowired
        public SamlProfileObjectBuilder<Subject> samlProfileSamlSubjectBuilder(
            final CasConfigurationProperties casProperties,
            @Qualifier("samlProfileSamlNameIdBuilder")
            final SamlProfileObjectBuilder<NameID> samlProfileSamlNameIdBuilder,
            @Qualifier("samlObjectEncrypter")
            final SamlIdPObjectEncrypter samlObjectEncrypter,
            @Qualifier(OpenSamlConfigBean.DEFAULT_BEAN_NAME)
            final OpenSamlConfigBean openSamlConfigBean) {
            return new SamlProfileSamlSubjectBuilder(openSamlConfigBean, samlProfileSamlNameIdBuilder, casProperties, samlObjectEncrypter);
        }

        @ConditionalOnMissingBean(name = "samlProfileSamlSoap11FaultResponseBuilder")
        @Bean
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        public SamlProfileObjectBuilder<Response> samlProfileSamlSoap11FaultResponseBuilder(
            final CasConfigurationProperties casProperties,
            @Qualifier("casSamlIdPMetadataResolver")
            final MetadataResolver casSamlIdPMetadataResolver,
            @Qualifier(OpenSamlConfigBean.DEFAULT_BEAN_NAME)
            final OpenSamlConfigBean openSamlConfigBean,
            @Qualifier("samlObjectSigner")
            final SamlIdPObjectSigner samlObjectSigner,
            @Qualifier("velocityEngineFactory")
            final VelocityEngine velocityEngineFactory,
            @Qualifier("samlProfileSamlAssertionBuilder")
            final SamlProfileObjectBuilder<Assertion> samlProfileSamlAssertionBuilder,
            @Qualifier("samlObjectEncrypter")
            final SamlIdPObjectEncrypter samlObjectEncrypter,
            @Qualifier("ticketGrantingTicketCookieGenerator")
            final CasCookieBuilder ticketGrantingTicketCookieGenerator,
            @Qualifier(TicketRegistry.BEAN_NAME)
            final TicketRegistry ticketRegistry,
            @Qualifier("samlIdPDistributedSessionStore")
            final SessionStore samlIdPDistributedSessionStore,
            @Qualifier("samlArtifactTicketFactory")
            final SamlArtifactTicketFactory samlArtifactTicketFactory,
            @Qualifier("samlArtifactMap")
            final SAMLArtifactMap samlArtifactMap,
            @Qualifier(CentralAuthenticationService.BEAN_NAME)
            final CentralAuthenticationService centralAuthenticationService,
            @Qualifier("samlAttributeQueryTicketFactory")
            final SamlAttributeQueryTicketFactory samlAttributeQueryTicketFactory,
            @Qualifier("samlProfileSamlResponseBuilder")
            final SamlProfileObjectBuilder<org.opensaml.saml.saml2.core.Response> samlProfileSamlResponseBuilder) {
            val context = SamlProfileSamlResponseBuilderConfigurationContext.builder()
                .samlIdPMetadataResolver(casSamlIdPMetadataResolver)
                .openSamlConfigBean(openSamlConfigBean)
                .samlObjectSigner(samlObjectSigner)
                .velocityEngineFactory(velocityEngineFactory)
                .samlProfileSamlAssertionBuilder(samlProfileSamlAssertionBuilder)
                .samlObjectEncrypter(samlObjectEncrypter)
                .ticketGrantingTicketCookieGenerator(ticketGrantingTicketCookieGenerator)
                .ticketRegistry(ticketRegistry)
                .sessionStore(samlIdPDistributedSessionStore)
                .samlArtifactTicketFactory(samlArtifactTicketFactory)
                .samlArtifactMap(samlArtifactMap)
                .centralAuthenticationService(centralAuthenticationService)
                .samlAttributeQueryTicketFactory(samlAttributeQueryTicketFactory)
                .casProperties(casProperties)
                .samlSoapResponseBuilder(samlProfileSamlResponseBuilder)
                .build();
            return new SamlProfileSamlSoap11FaultResponseBuilder(context);
        }

        @ConditionalOnMissingBean(name = "samlProfileSamlSoap11ResponseBuilder")
        @Bean
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        public SamlProfileObjectBuilder<Response> samlProfileSamlSoap11ResponseBuilder(
            final CasConfigurationProperties casProperties,
            @Qualifier("casSamlIdPMetadataResolver")
            final MetadataResolver casSamlIdPMetadataResolver,
            @Qualifier(OpenSamlConfigBean.DEFAULT_BEAN_NAME)
            final OpenSamlConfigBean openSamlConfigBean,
            @Qualifier("samlObjectSigner")
            final SamlIdPObjectSigner samlObjectSigner,
            @Qualifier("velocityEngineFactory")
            final VelocityEngine velocityEngineFactory,
            @Qualifier("samlProfileSamlAssertionBuilder")
            final SamlProfileObjectBuilder<Assertion> samlProfileSamlAssertionBuilder,
            @Qualifier("samlObjectEncrypter")
            final SamlIdPObjectEncrypter samlObjectEncrypter,
            @Qualifier("ticketGrantingTicketCookieGenerator")
            final CasCookieBuilder ticketGrantingTicketCookieGenerator,
            @Qualifier(TicketRegistry.BEAN_NAME)
            final TicketRegistry ticketRegistry,
            @Qualifier("samlIdPDistributedSessionStore")
            final SessionStore samlIdPDistributedSessionStore,
            @Qualifier("samlArtifactTicketFactory")
            final SamlArtifactTicketFactory samlArtifactTicketFactory,
            @Qualifier("samlArtifactMap")
            final SAMLArtifactMap samlArtifactMap,
            @Qualifier(CentralAuthenticationService.BEAN_NAME)
            final CentralAuthenticationService centralAuthenticationService,
            @Qualifier("samlAttributeQueryTicketFactory")
            final SamlAttributeQueryTicketFactory samlAttributeQueryTicketFactory,
            @Qualifier("samlProfileSamlResponseBuilder")
            final SamlProfileObjectBuilder<org.opensaml.saml.saml2.core.Response> samlProfileSamlResponseBuilder) {
            val context = SamlProfileSamlResponseBuilderConfigurationContext.builder()
                .samlIdPMetadataResolver(casSamlIdPMetadataResolver)
                .openSamlConfigBean(openSamlConfigBean)
                .samlObjectSigner(samlObjectSigner)
                .velocityEngineFactory(velocityEngineFactory)
                .samlProfileSamlAssertionBuilder(samlProfileSamlAssertionBuilder)
                .samlObjectEncrypter(samlObjectEncrypter)
                .ticketGrantingTicketCookieGenerator(ticketGrantingTicketCookieGenerator)
                .ticketRegistry(ticketRegistry)
                .sessionStore(samlIdPDistributedSessionStore)
                .samlArtifactTicketFactory(samlArtifactTicketFactory)
                .samlArtifactMap(samlArtifactMap)
                .centralAuthenticationService(centralAuthenticationService)
                .samlAttributeQueryTicketFactory(samlAttributeQueryTicketFactory)
                .casProperties(casProperties)
                .samlSoapResponseBuilder(samlProfileSamlResponseBuilder)
                .build();
            return new SamlProfileSamlSoap11ResponseBuilder(context);
        }

        @ConditionalOnMissingBean(name = "samlProfileSamlArtifactFaultResponseBuilder")
        @Bean
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        public SamlProfileObjectBuilder<org.opensaml.saml.saml2.core.Response> samlProfileSamlArtifactFaultResponseBuilder(
            final CasConfigurationProperties casProperties,
            @Qualifier("casSamlIdPMetadataResolver")
            final MetadataResolver casSamlIdPMetadataResolver,
            @Qualifier(OpenSamlConfigBean.DEFAULT_BEAN_NAME)
            final OpenSamlConfigBean openSamlConfigBean,
            @Qualifier("samlObjectSigner")
            final SamlIdPObjectSigner samlObjectSigner,
            @Qualifier("velocityEngineFactory")
            final VelocityEngine velocityEngineFactory,
            @Qualifier("samlProfileSamlAssertionBuilder")
            final SamlProfileObjectBuilder<Assertion> samlProfileSamlAssertionBuilder,
            @Qualifier("samlObjectEncrypter")
            final SamlIdPObjectEncrypter samlObjectEncrypter,
            @Qualifier("ticketGrantingTicketCookieGenerator")
            final CasCookieBuilder ticketGrantingTicketCookieGenerator,
            @Qualifier(TicketRegistry.BEAN_NAME)
            final TicketRegistry ticketRegistry,
            @Qualifier("samlIdPDistributedSessionStore")
            final SessionStore samlIdPDistributedSessionStore,
            @Qualifier("samlArtifactTicketFactory")
            final SamlArtifactTicketFactory samlArtifactTicketFactory,
            @Qualifier("samlArtifactMap")
            final SAMLArtifactMap samlArtifactMap,
            @Qualifier(CentralAuthenticationService.BEAN_NAME)
            final CentralAuthenticationService centralAuthenticationService,
            @Qualifier("samlAttributeQueryTicketFactory")
            final SamlAttributeQueryTicketFactory samlAttributeQueryTicketFactory,
            @Qualifier("samlProfileSamlResponseBuilder")
            final SamlProfileObjectBuilder<org.opensaml.saml.saml2.core.Response> samlProfileSamlResponseBuilder) {
            val context = SamlProfileSamlResponseBuilderConfigurationContext.builder()
                .samlIdPMetadataResolver(casSamlIdPMetadataResolver)
                .openSamlConfigBean(openSamlConfigBean)
                .samlObjectSigner(samlObjectSigner)
                .velocityEngineFactory(velocityEngineFactory)
                .samlProfileSamlAssertionBuilder(samlProfileSamlAssertionBuilder)
                .samlObjectEncrypter(samlObjectEncrypter)
                .ticketGrantingTicketCookieGenerator(ticketGrantingTicketCookieGenerator)
                .ticketRegistry(ticketRegistry)
                .sessionStore(samlIdPDistributedSessionStore)
                .samlArtifactTicketFactory(samlArtifactTicketFactory)
                .samlArtifactMap(samlArtifactMap)
                .centralAuthenticationService(centralAuthenticationService)
                .samlAttributeQueryTicketFactory(samlAttributeQueryTicketFactory)
                .casProperties(casProperties)
                .samlSoapResponseBuilder(samlProfileSamlResponseBuilder)
                .build();
            return new SamlProfileArtifactFaultResponseBuilder(context);
        }

        @ConditionalOnMissingBean(name = "samlProfileSamlArtifactResponseBuilder")
        @Bean
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        public SamlProfileObjectBuilder<org.opensaml.saml.saml2.core.Response> samlProfileSamlArtifactResponseBuilder(
            final CasConfigurationProperties casProperties,
            @Qualifier("casSamlIdPMetadataResolver")
            final MetadataResolver casSamlIdPMetadataResolver,
            @Qualifier(OpenSamlConfigBean.DEFAULT_BEAN_NAME)
            final OpenSamlConfigBean openSamlConfigBean,
            @Qualifier("samlObjectSigner")
            final SamlIdPObjectSigner samlObjectSigner,
            @Qualifier("velocityEngineFactory")
            final VelocityEngine velocityEngineFactory,
            @Qualifier("samlProfileSamlAssertionBuilder")
            final SamlProfileObjectBuilder<Assertion> samlProfileSamlAssertionBuilder,
            @Qualifier("samlObjectEncrypter")
            final SamlIdPObjectEncrypter samlObjectEncrypter,
            @Qualifier("ticketGrantingTicketCookieGenerator")
            final CasCookieBuilder ticketGrantingTicketCookieGenerator,
            @Qualifier(TicketRegistry.BEAN_NAME)
            final TicketRegistry ticketRegistry,
            @Qualifier("samlIdPDistributedSessionStore")
            final SessionStore samlIdPDistributedSessionStore,
            @Qualifier("samlArtifactTicketFactory")
            final SamlArtifactTicketFactory samlArtifactTicketFactory,
            @Qualifier("samlArtifactMap")
            final SAMLArtifactMap samlArtifactMap,
            @Qualifier(CentralAuthenticationService.BEAN_NAME)
            final CentralAuthenticationService centralAuthenticationService,
            @Qualifier("samlAttributeQueryTicketFactory")
            final SamlAttributeQueryTicketFactory samlAttributeQueryTicketFactory,
            @Qualifier("samlProfileSamlResponseBuilder")
            final SamlProfileObjectBuilder<org.opensaml.saml.saml2.core.Response> samlProfileSamlResponseBuilder) {
            val context = SamlProfileSamlResponseBuilderConfigurationContext.builder()
                .samlIdPMetadataResolver(casSamlIdPMetadataResolver)
                .openSamlConfigBean(openSamlConfigBean)
                .samlObjectSigner(samlObjectSigner)
                .velocityEngineFactory(velocityEngineFactory)
                .samlProfileSamlAssertionBuilder(samlProfileSamlAssertionBuilder)
                .samlObjectEncrypter(samlObjectEncrypter)
                .ticketGrantingTicketCookieGenerator(ticketGrantingTicketCookieGenerator)
                .ticketRegistry(ticketRegistry)
                .sessionStore(samlIdPDistributedSessionStore)
                .samlArtifactTicketFactory(samlArtifactTicketFactory)
                .samlArtifactMap(samlArtifactMap)
                .centralAuthenticationService(centralAuthenticationService)
                .samlAttributeQueryTicketFactory(samlAttributeQueryTicketFactory)
                .casProperties(casProperties)
                .samlSoapResponseBuilder(samlProfileSamlResponseBuilder)
                .build();
            return new SamlProfileArtifactResponseBuilder(context);
        }

        @ConditionalOnMissingBean(name = "samlProfileSamlNameIdBuilder")
        @Bean
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        public SamlProfileObjectBuilder<NameID> samlProfileSamlNameIdBuilder(
            @Qualifier("casSamlIdPMetadataResolver")
            final MetadataResolver casSamlIdPMetadataResolver,
            @Qualifier("shibbolethCompatiblePersistentIdGenerator")
            final PersistentIdGenerator shibbolethCompatiblePersistentIdGenerator,
            @Qualifier(OpenSamlConfigBean.DEFAULT_BEAN_NAME)
            final OpenSamlConfigBean openSamlConfigBean) {
            return new SamlProfileSamlNameIdBuilder(openSamlConfigBean, shibbolethCompatiblePersistentIdGenerator,
                casSamlIdPMetadataResolver);
        }

        @ConditionalOnMissingBean(name = "samlProfileSamlConditionsBuilder")
        @Bean
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        @Autowired
        public SamlProfileObjectBuilder<Conditions> samlProfileSamlConditionsBuilder(
            final CasConfigurationProperties casProperties,
            @Qualifier(OpenSamlConfigBean.DEFAULT_BEAN_NAME)
            final OpenSamlConfigBean openSamlConfigBean) {
            return new SamlProfileSamlConditionsBuilder(openSamlConfigBean, casProperties);
        }

        @ConditionalOnMissingBean(name = "defaultAuthnContextClassRefBuilder")
        @Bean
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        @Autowired
        public AuthnContextClassRefBuilder defaultAuthnContextClassRefBuilder(final CasConfigurationProperties casProperties) {
            return new DefaultAuthnContextClassRefBuilder(casProperties);
        }

        @ConditionalOnMissingBean(name = "samlProfileSamlAssertionBuilder")
        @Bean
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        public SamlProfileObjectBuilder<Assertion> samlProfileSamlAssertionBuilder(
            @Qualifier("samlProfileSamlAuthNStatementBuilder")
            final SamlProfileObjectBuilder<AuthnStatement> samlProfileSamlAuthNStatementBuilder,
            @Qualifier("samlProfileSamlAttributeStatementBuilder")
            final SamlProfileObjectBuilder<AttributeStatement> samlProfileSamlAttributeStatementBuilder,
            @Qualifier("samlProfileSamlSubjectBuilder")
            final SamlProfileObjectBuilder<Subject> samlProfileSamlSubjectBuilder,
            @Qualifier("samlProfileSamlConditionsBuilder")
            final SamlProfileObjectBuilder<Conditions> samlProfileSamlConditionsBuilder,
            @Qualifier("samlObjectSigner")
            final SamlIdPObjectSigner samlObjectSigner,
            @Qualifier("casSamlIdPMetadataResolver")
            final MetadataResolver casSamlIdPMetadataResolver,
            @Qualifier(OpenSamlConfigBean.DEFAULT_BEAN_NAME)
            final OpenSamlConfigBean openSamlConfigBean) {
            return new SamlProfileSamlAssertionBuilder(openSamlConfigBean, samlProfileSamlAuthNStatementBuilder,
                samlProfileSamlAttributeStatementBuilder, samlProfileSamlSubjectBuilder,
                samlProfileSamlConditionsBuilder, samlObjectSigner, casSamlIdPMetadataResolver);
        }

        @ConditionalOnMissingBean(name = "samlProfileSamlAuthNStatementBuilder")
        @Bean
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        @Autowired
        public SamlProfileObjectBuilder<AuthnStatement> samlProfileSamlAuthNStatementBuilder(
            final CasConfigurationProperties casProperties,
            @Qualifier("defaultAuthnContextClassRefBuilder")
            final AuthnContextClassRefBuilder defaultAuthnContextClassRefBuilder,
            @Qualifier(OpenSamlConfigBean.DEFAULT_BEAN_NAME)
            final OpenSamlConfigBean openSamlConfigBean) {
            return new SamlProfileSamlAuthNStatementBuilder(openSamlConfigBean, defaultAuthnContextClassRefBuilder, casProperties);
        }

        @ConditionalOnMissingBean(name = "samlProfileSamlAttributeStatementBuilder")
        @Bean
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        @Autowired
        public SamlProfileObjectBuilder<AttributeStatement> samlProfileSamlAttributeStatementBuilder(
            final CasConfigurationProperties casProperties,
            @Qualifier("samlObjectEncrypter")
            final SamlIdPObjectEncrypter samlObjectEncrypter,
            @Qualifier("samlProfileSamlNameIdBuilder")
            final SamlProfileObjectBuilder<NameID> samlProfileSamlNameIdBuilder,
            @Qualifier(OpenSamlConfigBean.DEFAULT_BEAN_NAME)
            final OpenSamlConfigBean openSamlConfigBean,
            @Qualifier("samlIdPServiceFactory")
            final ServiceFactory samlIdPServiceFactory,
            @Qualifier("attributeDefinitionStore")
            final AttributeDefinitionStore attributeDefinitionStore,
            @Qualifier("casSamlIdPMetadataResolver")
            final MetadataResolver casSamlIdPMetadataResolver) {
            return new SamlProfileSamlAttributeStatementBuilder(openSamlConfigBean,
                casProperties.getAuthn().getSamlIdp(), samlObjectEncrypter,
                attributeDefinitionStore, samlIdPServiceFactory,
                samlProfileSamlNameIdBuilder, casSamlIdPMetadataResolver);
        }


        @ConditionalOnMissingBean(name = "samlProfileSamlResponseBuilder")
        @Bean
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        @Autowired
        public SamlProfileObjectBuilder<org.opensaml.saml.saml2.core.Response> samlProfileSamlResponseBuilder(
            final CasConfigurationProperties casProperties,
            @Qualifier("casSamlIdPMetadataResolver")
            final MetadataResolver casSamlIdPMetadataResolver,
            @Qualifier(OpenSamlConfigBean.DEFAULT_BEAN_NAME)
            final OpenSamlConfigBean openSamlConfigBean,
            @Qualifier("samlObjectSigner")
            final SamlIdPObjectSigner samlObjectSigner,
            @Qualifier("velocityEngineFactory")
            final VelocityEngine velocityEngineFactory,
            @Qualifier("samlProfileSamlAssertionBuilder")
            final SamlProfileObjectBuilder<Assertion> samlProfileSamlAssertionBuilder,
            @Qualifier("samlObjectEncrypter")
            final SamlIdPObjectEncrypter samlObjectEncrypter,
            @Qualifier("ticketGrantingTicketCookieGenerator")
            final CasCookieBuilder ticketGrantingTicketCookieGenerator,
            @Qualifier(TicketRegistry.BEAN_NAME)
            final TicketRegistry ticketRegistry,
            @Qualifier("samlIdPDistributedSessionStore")
            final SessionStore samlIdPDistributedSessionStore,
            @Qualifier("samlArtifactTicketFactory")
            final SamlArtifactTicketFactory samlArtifactTicketFactory,
            @Qualifier("samlArtifactMap")
            final SAMLArtifactMap samlArtifactMap,
            @Qualifier(CentralAuthenticationService.BEAN_NAME)
            final CentralAuthenticationService centralAuthenticationService,
            @Qualifier("samlAttributeQueryTicketFactory")
            final SamlAttributeQueryTicketFactory samlAttributeQueryTicketFactory) {
            val context = SamlProfileSamlResponseBuilderConfigurationContext.builder()
                .samlIdPMetadataResolver(casSamlIdPMetadataResolver)
                .openSamlConfigBean(openSamlConfigBean)
                .samlObjectSigner(samlObjectSigner)
                .velocityEngineFactory(velocityEngineFactory)
                .samlProfileSamlAssertionBuilder(samlProfileSamlAssertionBuilder)
                .samlObjectEncrypter(samlObjectEncrypter)
                .ticketGrantingTicketCookieGenerator(ticketGrantingTicketCookieGenerator)
                .ticketRegistry(ticketRegistry)
                .sessionStore(samlIdPDistributedSessionStore)
                .samlArtifactTicketFactory(samlArtifactTicketFactory)
                .samlArtifactMap(samlArtifactMap)
                .centralAuthenticationService(centralAuthenticationService)
                .samlAttributeQueryTicketFactory(samlAttributeQueryTicketFactory)
                .casProperties(casProperties)
                .build();
            return new SamlProfileSaml2ResponseBuilder(context);
        }

    }

    @Configuration(value = "SamlIdPTicketFactoryPlanConfiguration", proxyBeanMethods = false)
    @EnableConfigurationProperties(CasConfigurationProperties.class)
    public static class SamlIdPTicketFactoryPlanConfiguration {
        @ConditionalOnMissingBean(name = "samlAttributeQueryTicketFactoryConfigurer")
        @Bean
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        @Autowired
        public TicketFactoryExecutionPlanConfigurer samlAttributeQueryTicketFactoryConfigurer(
            @Qualifier("samlAttributeQueryTicketFactory")
            final SamlAttributeQueryTicketFactory samlAttributeQueryTicketFactory) {
            return () -> samlAttributeQueryTicketFactory;
        }

        @ConditionalOnMissingBean(name = "samlArtifactTicketFactoryConfigurer")
        @Bean
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        @Autowired
        public TicketFactoryExecutionPlanConfigurer samlArtifactTicketFactoryConfigurer(
            @Qualifier("samlArtifactTicketFactory")
            final SamlArtifactTicketFactory samlArtifactTicketFactory) {
            return () -> samlArtifactTicketFactory;
        }
    }

    @Configuration(value = "SamlIdPTicketExpirationPolicyConfiguration", proxyBeanMethods = false)
    @EnableConfigurationProperties(CasConfigurationProperties.class)
    public static class SamlIdPTicketExpirationPolicyConfiguration {
        @ConditionalOnMissingBean(name = "samlAttributeQueryTicketExpirationPolicy")
        @Bean
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        @Autowired
        public ExpirationPolicyBuilder samlAttributeQueryTicketExpirationPolicy(final CasConfigurationProperties casProperties) {
            return new SamlAttributeQueryTicketExpirationPolicyBuilder(casProperties);
        }

        @ConditionalOnMissingBean(name = "samlArtifactTicketExpirationPolicy")
        @Bean
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        @Autowired
        public ExpirationPolicyBuilder samlArtifactTicketExpirationPolicy(final CasConfigurationProperties casProperties) {
            return new SamlArtifactTicketExpirationPolicyBuilder(casProperties);
        }
    }

    @Configuration(value = "SamlIdPTicketConfiguration", proxyBeanMethods = false)
    @EnableConfigurationProperties(CasConfigurationProperties.class)
    public static class SamlIdPTicketConfiguration {
        @ConditionalOnMissingBean(name = "samlAttributeQueryTicketFactory")
        @Bean
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        public SamlAttributeQueryTicketFactory samlAttributeQueryTicketFactory(
            @Qualifier("samlAttributeQueryTicketExpirationPolicy")
            final ExpirationPolicyBuilder samlAttributeQueryTicketExpirationPolicy,
            @Qualifier(OpenSamlConfigBean.DEFAULT_BEAN_NAME)
            final OpenSamlConfigBean openSamlConfigBean,
            @Qualifier("samlIdPServiceFactory")
            final ServiceFactory samlIdPServiceFactory) {
            return new DefaultSamlAttributeQueryTicketFactory(samlAttributeQueryTicketExpirationPolicy,
                samlIdPServiceFactory, openSamlConfigBean);
        }

        @ConditionalOnMissingBean(name = "samlArtifactTicketFactory")
        @Bean
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        @Autowired
        public SamlArtifactTicketFactory samlArtifactTicketFactory(
            @Qualifier("samlArtifactTicketExpirationPolicy")
            final ExpirationPolicyBuilder samlArtifactTicketExpirationPolicy,
            @Qualifier(OpenSamlConfigBean.DEFAULT_BEAN_NAME)
            final OpenSamlConfigBean openSamlConfigBean,
            @Qualifier("samlIdPServiceFactory")
            final ServiceFactory samlIdPServiceFactory) {
            return new DefaultSamlArtifactTicketFactory(samlArtifactTicketExpirationPolicy, openSamlConfigBean, samlIdPServiceFactory);
        }

        @Bean(initMethod = "initialize", destroyMethod = "destroy")
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        public SAMLArtifactMap samlArtifactMap(
            @Qualifier("samlArtifactTicketFactory")
            final SamlArtifactTicketFactory samlArtifactTicketFactory,
            @Qualifier("samlArtifactTicketExpirationPolicy")
            final ExpirationPolicyBuilder samlArtifactTicketExpirationPolicy,
            @Qualifier("ticketGrantingTicketCookieGenerator")
            final CasCookieBuilder ticketGrantingTicketCookieGenerator,
            @Qualifier(TicketRegistry.BEAN_NAME)
            final TicketRegistry ticketRegistry,
            @Qualifier("samlIdPDistributedSessionStore")
            final SessionStore samlIdPDistributedSessionStore,
            @Qualifier(CentralAuthenticationService.BEAN_NAME)
            final CentralAuthenticationService centralAuthenticationService) {
            val map = new CasSamlArtifactMap(ticketRegistry, samlArtifactTicketFactory,
                ticketGrantingTicketCookieGenerator, samlIdPDistributedSessionStore, centralAuthenticationService);
            val expirationPolicy = samlArtifactTicketExpirationPolicy.buildTicketExpirationPolicy();
            map.setArtifactLifetime(Duration.ofSeconds(expirationPolicy.getTimeToLive()));
            return map;
        }

    }

    @Configuration(value = "SamlIdPLogoutConfiguration", proxyBeanMethods = false)
    @EnableConfigurationProperties(CasConfigurationProperties.class)
    public static class SamlIdPLogoutConfiguration {
        @ConditionalOnMissingBean(name = "samlSingleLogoutServiceLogoutUrlBuilder")
        @Bean
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        @Autowired
        public SingleLogoutServiceLogoutUrlBuilder samlSingleLogoutServiceLogoutUrlBuilder(
            @Qualifier("defaultSamlRegisteredServiceCachingMetadataResolver")
            final SamlRegisteredServiceCachingMetadataResolver defaultSamlRegisteredServiceCachingMetadataResolver,
            @Qualifier(ServicesManager.BEAN_NAME)
            final ServicesManager servicesManager,
            @Qualifier("urlValidator")
            final UrlValidator urlValidator) {
            return new SamlIdPSingleLogoutServiceLogoutUrlBuilder(servicesManager, defaultSamlRegisteredServiceCachingMetadataResolver, urlValidator);
        }

        @ConditionalOnMissingBean(name = "samlSingleLogoutServiceLogoutUrlBuilderConfigurer")
        @Bean
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        @Autowired
        public SingleLogoutServiceLogoutUrlBuilderConfigurer samlSingleLogoutServiceLogoutUrlBuilderConfigurer(
            @Qualifier("samlSingleLogoutServiceLogoutUrlBuilder")
            final SingleLogoutServiceLogoutUrlBuilder samlSingleLogoutServiceLogoutUrlBuilder) {
            return () -> samlSingleLogoutServiceLogoutUrlBuilder;
        }
    }

    @Configuration(value = "SamlIdPCryptoConfiguration", proxyBeanMethods = false)
    @EnableConfigurationProperties(CasConfigurationProperties.class)
    public static class SamlIdPCryptoConfiguration {
        @ConditionalOnMissingBean(name = "samlObjectEncrypter")
        @Bean
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        @Autowired
        public SamlIdPObjectEncrypter samlObjectEncrypter(
            @Qualifier("samlIdPMetadataLocator")
            final SamlIdPMetadataLocator samlIdPMetadataLocator,
            final CasConfigurationProperties casProperties) {
            return new SamlIdPObjectEncrypter(casProperties.getAuthn().getSamlIdp(), samlIdPMetadataLocator);
        }

        @ConditionalOnMissingBean(name = SamlIdPObjectSigner.DEFAULT_BEAN_NAME)
        @Bean
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        @Autowired
        public SamlIdPObjectSigner samlObjectSigner(
            final CasConfigurationProperties casProperties,
            @Qualifier("casSamlIdPMetadataResolver")
            final MetadataResolver casSamlIdPMetadataResolver,
            @Qualifier("samlIdPMetadataLocator")
            final SamlIdPMetadataLocator samlIdPMetadataLocator) {
            return new DefaultSamlIdPObjectSigner(casSamlIdPMetadataResolver, casProperties, samlIdPMetadataLocator);
        }
    }

    @Configuration(value = "SamlIdPAuditConfiguration", proxyBeanMethods = false)
    @EnableConfigurationProperties(CasConfigurationProperties.class)
    public static class SamlIdPAuditConfiguration {
        @Bean
        @ConditionalOnMissingBean(name = "samlResponseAuditPrincipalIdProvider")
        public AuditPrincipalIdProvider samlResponseAuditPrincipalIdProvider() {
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
    }
}
