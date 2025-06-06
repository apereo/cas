package org.apereo.cas.config;

import org.apereo.cas.CentralAuthenticationService;
import org.apereo.cas.audit.AuditActionResolvers;
import org.apereo.cas.audit.AuditPrincipalIdProvider;
import org.apereo.cas.audit.AuditResourceResolvers;
import org.apereo.cas.audit.AuditTrailConstants;
import org.apereo.cas.audit.AuditTrailRecordResolutionPlanConfigurer;
import org.apereo.cas.authentication.attribute.AttributeDefinitionStore;
import org.apereo.cas.authentication.attribute.AttributeDefinitionStoreConfigurer;
import org.apereo.cas.authentication.attribute.DefaultAttributeDefinitionStore;
import org.apereo.cas.authentication.principal.PersistentIdGenerator;
import org.apereo.cas.authentication.principal.ServiceFactory;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.configuration.features.CasFeatureModule;
import org.apereo.cas.logout.slo.SingleLogoutServiceLogoutUrlBuilder;
import org.apereo.cas.logout.slo.SingleLogoutServiceLogoutUrlBuilderConfigurer;
import org.apereo.cas.services.ServicesManager;
import org.apereo.cas.support.saml.OpenSamlConfigBean;
import org.apereo.cas.support.saml.idp.metadata.locator.SamlIdPMetadataLocator;
import org.apereo.cas.support.saml.services.idp.metadata.cache.SamlRegisteredServiceCachingMetadataResolver;
import org.apereo.cas.support.saml.web.idp.audit.SamlMetadataResolverAuditResourceResolver;
import org.apereo.cas.support.saml.web.idp.audit.SamlRequestAuditResourceResolver;
import org.apereo.cas.support.saml.web.idp.audit.SamlResponseAuditPrincipalIdProvider;
import org.apereo.cas.support.saml.web.idp.audit.SamlResponseAuditResourceResolver;
import org.apereo.cas.support.saml.web.idp.profile.SamlSecurityProvider;
import org.apereo.cas.support.saml.web.idp.profile.artifact.CasSamlArtifactMap;
import org.apereo.cas.support.saml.web.idp.profile.builders.SamlProfileObjectBuilder;
import org.apereo.cas.support.saml.web.idp.profile.builders.assertion.SamlProfileSamlAssertionBuilder;
import org.apereo.cas.support.saml.web.idp.profile.builders.attr.SamlProfileSamlAttributeStatementBuilder;
import org.apereo.cas.support.saml.web.idp.profile.builders.authn.SamlProfileAuthnContextClassRefBuilder;
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
import org.apereo.cas.ticket.TicketFactory;
import org.apereo.cas.ticket.TicketFactoryExecutionPlanConfigurer;
import org.apereo.cas.ticket.artifact.DefaultSamlArtifactTicketFactory;
import org.apereo.cas.ticket.artifact.SamlArtifactTicketExpirationPolicyBuilder;
import org.apereo.cas.ticket.artifact.SamlArtifactTicketFactory;
import org.apereo.cas.ticket.query.DefaultSamlAttributeQueryTicketFactory;
import org.apereo.cas.ticket.query.SamlAttributeQueryTicketExpirationPolicyBuilder;
import org.apereo.cas.ticket.query.SamlAttributeQueryTicketFactory;
import org.apereo.cas.ticket.registry.TicketRegistry;
import org.apereo.cas.ticket.tracking.TicketTrackingPolicy;
import org.apereo.cas.util.scripting.ScriptResourceCacheManager;
import org.apereo.cas.util.spring.boot.ConditionalOnFeatureEnabled;
import org.apereo.cas.web.UrlValidator;
import org.apereo.cas.web.cookie.CasCookieBuilder;
import lombok.val;
import org.apache.velocity.app.VelocityEngine;
import org.apereo.inspektr.audit.spi.AuditActionResolver;
import org.apereo.inspektr.audit.spi.AuditResourceResolver;
import org.apereo.inspektr.audit.spi.support.DefaultAuditActionResolver;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.opensaml.saml.common.SAMLObject;
import org.opensaml.saml.common.binding.artifact.SAMLArtifactMap;
import org.opensaml.saml.metadata.resolver.MetadataResolver;
import org.opensaml.saml.saml2.core.Assertion;
import org.opensaml.saml.saml2.core.AttributeStatement;
import org.opensaml.saml.saml2.core.AuthnContext;
import org.opensaml.saml.saml2.core.AuthnStatement;
import org.opensaml.saml.saml2.core.Conditions;
import org.opensaml.saml.saml2.core.Response;
import org.opensaml.saml.saml2.core.Subject;
import org.opensaml.soap.soap11.Envelope;
import org.pac4j.core.context.session.SessionStore;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Scope;
import org.springframework.context.annotation.ScopedProxyMode;
import org.springframework.core.io.ClassPathResource;
import java.security.Security;
import java.time.Duration;

/**
 * The {@link SamlIdPConfiguration}.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 */
@EnableConfigurationProperties(CasConfigurationProperties.class)
@ConditionalOnFeatureEnabled(feature = CasFeatureModule.FeatureCatalog.SAMLIdentityProvider)
@Configuration(value = "SamlIdPConfiguration", proxyBeanMethods = false)
class SamlIdPConfiguration {

    static {
        Security.addProvider(new BouncyCastleProvider());
        Security.addProvider(new SamlSecurityProvider());
    }

    @Configuration(value = "SamlIdPProfileBuilderConfiguration", proxyBeanMethods = false)
    @EnableConfigurationProperties(CasConfigurationProperties.class)
    static class SamlIdPProfileBuilderConfiguration {
        @ConditionalOnMissingBean(name = "samlResponseBuilderConfigurationContext")
        @Bean
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        @Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
        public SamlProfileSamlResponseBuilderConfigurationContext samlResponseBuilderConfigurationContext(
            final ConfigurableApplicationContext applicationContext,
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
            @Qualifier(CasCookieBuilder.BEAN_NAME_TICKET_GRANTING_COOKIE_BUILDER)
            final CasCookieBuilder ticketGrantingTicketCookieGenerator,
            @Qualifier(TicketRegistry.BEAN_NAME)
            final TicketRegistry ticketRegistry,
            @Qualifier("samlIdPDistributedSessionStore")
            final SessionStore samlIdPDistributedSessionStore,
            @Qualifier("samlArtifactMap")
            final SAMLArtifactMap samlArtifactMap,
            @Qualifier(TicketFactory.BEAN_NAME)
            final TicketFactory ticketFactory,
            @Qualifier(CentralAuthenticationService.BEAN_NAME)
            final CentralAuthenticationService centralAuthenticationService) {
            return SamlProfileSamlResponseBuilderConfigurationContext.builder()
                .applicationContext(applicationContext)
                .samlIdPMetadataResolver(casSamlIdPMetadataResolver)
                .openSamlConfigBean(openSamlConfigBean)
                .samlObjectSigner(samlObjectSigner)
                .velocityEngineFactory(velocityEngineFactory)
                .samlProfileSamlAssertionBuilder(samlProfileSamlAssertionBuilder)
                .samlObjectEncrypter(samlObjectEncrypter)
                .ticketGrantingTicketCookieGenerator(ticketGrantingTicketCookieGenerator)
                .ticketRegistry(ticketRegistry)
                .sessionStore(samlIdPDistributedSessionStore)
                .samlArtifactMap(samlArtifactMap)
                .centralAuthenticationService(centralAuthenticationService)
                .casProperties(casProperties)
                .ticketFactory(ticketFactory)
                .build();
        }

        @ConditionalOnMissingBean(name = "samlProfileSamlAttributeQueryFaultResponseBuilder")
        @Bean
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        public SamlProfileObjectBuilder<Envelope> samlProfileSamlAttributeQueryFaultResponseBuilder(
            @Qualifier("samlProfileSamlResponseBuilder")
            final SamlProfileObjectBuilder<Response> samlProfileSamlResponseBuilder,
            @Qualifier("samlResponseBuilderConfigurationContext")
            final SamlProfileSamlResponseBuilderConfigurationContext samlResponseBuilderConfigurationContext) {
            return new SamlProfileAttributeQueryFaultResponseBuilder(samlResponseBuilderConfigurationContext.withSamlSoapResponseBuilder(samlProfileSamlResponseBuilder));
        }

        @ConditionalOnMissingBean(name = "samlProfileSamlAttributeQueryResponseBuilder")
        @Bean
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        public SamlProfileObjectBuilder<Envelope> samlProfileSamlAttributeQueryResponseBuilder(
            @Qualifier("samlProfileSamlResponseBuilder")
            final SamlProfileObjectBuilder<Response> samlProfileSamlResponseBuilder,
            @Qualifier("samlResponseBuilderConfigurationContext")
            final SamlProfileSamlResponseBuilderConfigurationContext samlResponseBuilderConfigurationContext) {
            return new SamlProfileAttributeQueryResponseBuilder(samlResponseBuilderConfigurationContext.withSamlSoapResponseBuilder(samlProfileSamlResponseBuilder));
        }

        @ConditionalOnMissingBean(name = "samlProfileSamlSubjectBuilder")
        @Bean
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        public SamlProfileObjectBuilder<Subject> samlProfileSamlSubjectBuilder(
            final CasConfigurationProperties casProperties,
            @Qualifier("samlProfileSamlNameIdBuilder")
            final SamlProfileObjectBuilder<SAMLObject> samlProfileSamlNameIdBuilder,
            @Qualifier("samlObjectEncrypter")
            final SamlIdPObjectEncrypter samlObjectEncrypter,
            @Qualifier(OpenSamlConfigBean.DEFAULT_BEAN_NAME)
            final OpenSamlConfigBean openSamlConfigBean) {
            return new SamlProfileSamlSubjectBuilder(openSamlConfigBean, samlProfileSamlNameIdBuilder, casProperties, samlObjectEncrypter);
        }

        @ConditionalOnMissingBean(name = "samlProfileSamlSoap11FaultResponseBuilder")
        @Bean
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        public SamlProfileObjectBuilder<Envelope> samlProfileSamlSoap11FaultResponseBuilder(
            @Qualifier("samlProfileSamlNameIdBuilder")
            final SamlProfileObjectBuilder<SAMLObject> samlProfileSamlNameIdBuilder,
            @Qualifier("samlResponseBuilderConfigurationContext")
            final SamlProfileSamlResponseBuilderConfigurationContext samlResponseBuilderConfigurationContext) {
            return new SamlProfileSamlSoap11FaultResponseBuilder(samlResponseBuilderConfigurationContext.withSamlSoapResponseBuilder(samlProfileSamlNameIdBuilder));
        }

        @ConditionalOnMissingBean(name = "samlProfileSamlSoap11ResponseBuilder")
        @Bean
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        public SamlProfileObjectBuilder<Envelope> samlProfileSamlSoap11ResponseBuilder(
            @Qualifier("samlProfileSamlNameIdBuilder")
            final SamlProfileObjectBuilder<SAMLObject> samlProfileSamlNameIdBuilder,
            @Qualifier("samlResponseBuilderConfigurationContext")
            final SamlProfileSamlResponseBuilderConfigurationContext samlResponseBuilderConfigurationContext) {
            return new SamlProfileSamlSoap11ResponseBuilder(samlResponseBuilderConfigurationContext.withSamlSoapResponseBuilder(samlProfileSamlNameIdBuilder));
        }

        @ConditionalOnMissingBean(name = "samlProfileSamlArtifactFaultResponseBuilder")
        @Bean
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        public SamlProfileObjectBuilder<Envelope> samlProfileSamlArtifactFaultResponseBuilder(
            @Qualifier("samlProfileSamlNameIdBuilder")
            final SamlProfileObjectBuilder<SAMLObject> samlProfileSamlNameIdBuilder,
            @Qualifier("samlResponseBuilderConfigurationContext")
            final SamlProfileSamlResponseBuilderConfigurationContext samlResponseBuilderConfigurationContext) {
            return new SamlProfileArtifactFaultResponseBuilder(samlResponseBuilderConfigurationContext.withSamlSoapResponseBuilder(samlProfileSamlNameIdBuilder));
        }

        @ConditionalOnMissingBean(name = "samlProfileSamlArtifactResponseBuilder")
        @Bean
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        public SamlProfileObjectBuilder<Envelope> samlProfileSamlArtifactResponseBuilder(
            @Qualifier("samlProfileSamlNameIdBuilder")
            final SamlProfileObjectBuilder<SAMLObject> samlProfileSamlNameIdBuilder,
            @Qualifier("samlResponseBuilderConfigurationContext")
            final SamlProfileSamlResponseBuilderConfigurationContext samlResponseBuilderConfigurationContext) {
            return new SamlProfileArtifactResponseBuilder(samlResponseBuilderConfigurationContext.withSamlSoapResponseBuilder(samlProfileSamlNameIdBuilder));
        }

        @ConditionalOnMissingBean(name = "samlProfileSamlNameIdBuilder")
        @Bean
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        public SamlProfileObjectBuilder<SAMLObject> samlProfileSamlNameIdBuilder(
            @Qualifier("casSamlIdPMetadataResolver")
            final MetadataResolver casSamlIdPMetadataResolver,
            @Qualifier("shibbolethCompatiblePersistentIdGenerator")
            final PersistentIdGenerator shibbolethCompatiblePersistentIdGenerator,
            @Qualifier(OpenSamlConfigBean.DEFAULT_BEAN_NAME)
            final OpenSamlConfigBean openSamlConfigBean,
            @Qualifier("samlObjectEncrypter")
            final SamlIdPObjectEncrypter samlObjectEncrypter) {
            return new SamlProfileSamlNameIdBuilder(openSamlConfigBean,
                shibbolethCompatiblePersistentIdGenerator,
                casSamlIdPMetadataResolver, samlObjectEncrypter);
        }

        @ConditionalOnMissingBean(name = "samlProfileSamlConditionsBuilder")
        @Bean
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        public SamlProfileObjectBuilder<Conditions> samlProfileSamlConditionsBuilder(
            final CasConfigurationProperties casProperties,
            @Qualifier(OpenSamlConfigBean.DEFAULT_BEAN_NAME)
            final OpenSamlConfigBean openSamlConfigBean) {
            return new SamlProfileSamlConditionsBuilder(openSamlConfigBean, casProperties);
        }

        @ConditionalOnMissingBean(name = "defaultAuthnContextClassRefBuilder")
        @Bean
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        public SamlProfileObjectBuilder<AuthnContext> defaultAuthnContextClassRefBuilder(
            @Qualifier(ScriptResourceCacheManager.BEAN_NAME)
            final ObjectProvider<ScriptResourceCacheManager> scriptResourceCacheManager,
            @Qualifier(OpenSamlConfigBean.DEFAULT_BEAN_NAME)
            final OpenSamlConfigBean openSamlConfigBean,
            @Qualifier("casSamlIdPMetadataResolver")
            final MetadataResolver casSamlIdPMetadataResolver,
            final CasConfigurationProperties casProperties) {
            return new SamlProfileAuthnContextClassRefBuilder(openSamlConfigBean,
                casSamlIdPMetadataResolver, casProperties, scriptResourceCacheManager);
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
        public SamlProfileObjectBuilder<AuthnStatement> samlProfileSamlAuthNStatementBuilder(
            final CasConfigurationProperties casProperties,
            @Qualifier("defaultAuthnContextClassRefBuilder")
            final SamlProfileObjectBuilder<AuthnContext> defaultAuthnContextClassRefBuilder,
            @Qualifier(OpenSamlConfigBean.DEFAULT_BEAN_NAME)
            final OpenSamlConfigBean openSamlConfigBean) {
            return new SamlProfileSamlAuthNStatementBuilder(openSamlConfigBean,
                defaultAuthnContextClassRefBuilder, casProperties);
        }

        @ConditionalOnMissingBean(name = "samlProfileSamlAttributeStatementBuilder")
        @Bean
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        public SamlProfileObjectBuilder<AttributeStatement> samlProfileSamlAttributeStatementBuilder(
            final CasConfigurationProperties casProperties,
            @Qualifier("samlObjectEncrypter")
            final SamlIdPObjectEncrypter samlObjectEncrypter,
            @Qualifier("samlProfileSamlNameIdBuilder")
            final SamlProfileObjectBuilder<SAMLObject> samlProfileSamlNameIdBuilder,
            @Qualifier(OpenSamlConfigBean.DEFAULT_BEAN_NAME)
            final OpenSamlConfigBean openSamlConfigBean,
            @Qualifier("samlIdPServiceFactory")
            final ServiceFactory samlIdPServiceFactory,
            @Qualifier(AttributeDefinitionStore.BEAN_NAME)
            final AttributeDefinitionStore attributeDefinitionStore,
            @Qualifier("casSamlIdPMetadataResolver")
            final MetadataResolver casSamlIdPMetadataResolver) {
            return new SamlProfileSamlAttributeStatementBuilder(openSamlConfigBean,
                casProperties, samlObjectEncrypter,
                attributeDefinitionStore, samlIdPServiceFactory,
                samlProfileSamlNameIdBuilder, casSamlIdPMetadataResolver);
        }


        @ConditionalOnMissingBean(name = "samlProfileSamlResponseBuilder")
        @Bean
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        public SamlProfileObjectBuilder<Response> samlProfileSamlResponseBuilder(
            @Qualifier("samlResponseBuilderConfigurationContext")
            final SamlProfileSamlResponseBuilderConfigurationContext samlResponseBuilderConfigurationContext) {
            return new SamlProfileSaml2ResponseBuilder(samlResponseBuilderConfigurationContext);
        }

    }

    @Configuration(value = "SamlIdPTicketFactoryPlanConfiguration", proxyBeanMethods = false)
    @EnableConfigurationProperties(CasConfigurationProperties.class)
    static class SamlIdPTicketFactoryPlanConfiguration {
        @ConditionalOnMissingBean(name = "samlAttributeQueryTicketFactoryConfigurer")
        @Bean
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        public TicketFactoryExecutionPlanConfigurer samlAttributeQueryTicketFactoryConfigurer(
            @Qualifier("samlAttributeQueryTicketFactory")
            final SamlAttributeQueryTicketFactory samlAttributeQueryTicketFactory) {
            return () -> samlAttributeQueryTicketFactory;
        }

        @ConditionalOnMissingBean(name = "samlArtifactTicketFactoryConfigurer")
        @Bean
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        public TicketFactoryExecutionPlanConfigurer samlArtifactTicketFactoryConfigurer(
            @Qualifier("samlArtifactTicketFactory")
            final SamlArtifactTicketFactory samlArtifactTicketFactory) {
            return () -> samlArtifactTicketFactory;
        }
    }

    @Configuration(value = "SamlIdPTicketExpirationPolicyConfiguration", proxyBeanMethods = false)
    @EnableConfigurationProperties(CasConfigurationProperties.class)
    static class SamlIdPTicketExpirationPolicyConfiguration {
        @ConditionalOnMissingBean(name = "samlAttributeQueryTicketExpirationPolicy")
        @Bean
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        public ExpirationPolicyBuilder samlAttributeQueryTicketExpirationPolicy(final CasConfigurationProperties casProperties) {
            return new SamlAttributeQueryTicketExpirationPolicyBuilder(casProperties);
        }

        @ConditionalOnMissingBean(name = "samlArtifactTicketExpirationPolicy")
        @Bean
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        public ExpirationPolicyBuilder samlArtifactTicketExpirationPolicy(final CasConfigurationProperties casProperties) {
            return new SamlArtifactTicketExpirationPolicyBuilder(casProperties);
        }
    }

    @Configuration(value = "SamlIdPTicketConfiguration", proxyBeanMethods = false)
    @EnableConfigurationProperties(CasConfigurationProperties.class)
    static class SamlIdPTicketConfiguration {
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
        public SamlArtifactTicketFactory samlArtifactTicketFactory(
            @Qualifier("samlArtifactTicketExpirationPolicy")
            final ExpirationPolicyBuilder samlArtifactTicketExpirationPolicy,
            @Qualifier(OpenSamlConfigBean.DEFAULT_BEAN_NAME)
            final OpenSamlConfigBean openSamlConfigBean,
            @Qualifier("samlIdPServiceFactory")
            final ServiceFactory samlIdPServiceFactory,
            @Qualifier(TicketTrackingPolicy.BEAN_NAME_DESCENDANT_TICKET_TRACKING)
            final TicketTrackingPolicy descendantTicketsTrackingPolicy) {
            return new DefaultSamlArtifactTicketFactory(samlArtifactTicketExpirationPolicy,
                openSamlConfigBean, samlIdPServiceFactory, descendantTicketsTrackingPolicy);
        }

        @Bean(initMethod = "initialize", destroyMethod = "destroy")
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        public SAMLArtifactMap samlArtifactMap(
            @Qualifier(TicketFactory.BEAN_NAME)
            final TicketFactory ticketFactory,
            @Qualifier("samlArtifactTicketExpirationPolicy")
            final ExpirationPolicyBuilder samlArtifactTicketExpirationPolicy,
            @Qualifier(CasCookieBuilder.BEAN_NAME_TICKET_GRANTING_COOKIE_BUILDER)
            final CasCookieBuilder ticketGrantingTicketCookieGenerator,
            @Qualifier(TicketRegistry.BEAN_NAME)
            final TicketRegistry ticketRegistry,
            @Qualifier("samlIdPDistributedSessionStore")
            final SessionStore samlIdPDistributedSessionStore) {
            val map = new CasSamlArtifactMap(ticketRegistry, ticketFactory,
                ticketGrantingTicketCookieGenerator, samlIdPDistributedSessionStore);
            val expirationPolicy = samlArtifactTicketExpirationPolicy.buildTicketExpirationPolicy();
            map.setArtifactLifetime(Duration.ofSeconds(expirationPolicy.getTimeToLive()));
            return map;
        }
    }

    @Configuration(value = "SamlIdPLogoutConfiguration", proxyBeanMethods = false)
    @EnableConfigurationProperties(CasConfigurationProperties.class)
    static class SamlIdPLogoutConfiguration {
        @ConditionalOnMissingBean(name = "samlSingleLogoutServiceLogoutUrlBuilder")
        @Bean
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        public SingleLogoutServiceLogoutUrlBuilder samlSingleLogoutServiceLogoutUrlBuilder(
            @Qualifier(SamlRegisteredServiceCachingMetadataResolver.BEAN_NAME)
            final SamlRegisteredServiceCachingMetadataResolver defaultSamlRegisteredServiceCachingMetadataResolver,
            @Qualifier(ServicesManager.BEAN_NAME)
            final ServicesManager servicesManager,
            @Qualifier(UrlValidator.BEAN_NAME)
            final UrlValidator urlValidator,
            final CasConfigurationProperties casProperties) {
            return new SamlIdPSingleLogoutServiceLogoutUrlBuilder(servicesManager, defaultSamlRegisteredServiceCachingMetadataResolver,
                                                                  urlValidator, casProperties.getAuthn().getSamlIdp());
        }

        @ConditionalOnMissingBean(name = "samlSingleLogoutServiceLogoutUrlBuilderConfigurer")
        @Bean
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        public SingleLogoutServiceLogoutUrlBuilderConfigurer samlSingleLogoutServiceLogoutUrlBuilderConfigurer(
            @Qualifier("samlSingleLogoutServiceLogoutUrlBuilder")
            final SingleLogoutServiceLogoutUrlBuilder samlSingleLogoutServiceLogoutUrlBuilder) {
            return () -> samlSingleLogoutServiceLogoutUrlBuilder;
        }
    }

    @Configuration(value = "SamlIdPCryptoConfiguration", proxyBeanMethods = false)
    @EnableConfigurationProperties(CasConfigurationProperties.class)
    static class SamlIdPCryptoConfiguration {
        @ConditionalOnMissingBean(name = "samlObjectEncrypter")
        @Bean
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        public SamlIdPObjectEncrypter samlObjectEncrypter(
            @Qualifier(SamlIdPMetadataLocator.BEAN_NAME)
            final SamlIdPMetadataLocator samlIdPMetadataLocator,
            final CasConfigurationProperties casProperties) {
            return new SamlIdPObjectEncrypter(casProperties.getAuthn().getSamlIdp(), samlIdPMetadataLocator);
        }

        @ConditionalOnMissingBean(name = SamlIdPObjectSigner.DEFAULT_BEAN_NAME)
        @Bean
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        public SamlIdPObjectSigner samlObjectSigner(
            final CasConfigurationProperties casProperties,
            @Qualifier("casSamlIdPMetadataResolver")
            final MetadataResolver casSamlIdPMetadataResolver,
            @Qualifier(SamlIdPMetadataLocator.BEAN_NAME)
            final SamlIdPMetadataLocator samlIdPMetadataLocator) {
            return new DefaultSamlIdPObjectSigner(casSamlIdPMetadataResolver, casProperties, samlIdPMetadataLocator);
        }
    }

    @Configuration(value = "SamlIdPAuditConfiguration", proxyBeanMethods = false)
    @EnableConfigurationProperties(CasConfigurationProperties.class)
    static class SamlIdPAuditConfiguration {
        @Bean
        @ConditionalOnMissingBean(name = "samlResponseAuditPrincipalIdProvider")
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        public AuditPrincipalIdProvider samlResponseAuditPrincipalIdProvider() {
            return new SamlResponseAuditPrincipalIdProvider();
        }

        @Bean
        @ConditionalOnMissingBean(name = "samlResponseAuditResourceResolver")
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        public AuditResourceResolver samlResponseAuditResourceResolver() {
            return new SamlResponseAuditResourceResolver();
        }

        @Bean
        @ConditionalOnMissingBean(name = "samlRequestAuditResourceResolver")
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        public AuditResourceResolver samlRequestAuditResourceResolver() {
            return new SamlRequestAuditResourceResolver();
        }

        @Bean
        @ConditionalOnMissingBean(name = "samlMetadataResolutionAuditActionResolver")
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        public AuditActionResolver samlMetadataResolutionAuditActionResolver() {
            return new DefaultAuditActionResolver();
        }


        @Bean
        @ConditionalOnMissingBean(name = "samlMetadataResolutionAuditResourceResolver")
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        public AuditResourceResolver samlMetadataResolutionAuditResourceResolver() {
            return new SamlMetadataResolverAuditResourceResolver();
        }

        @Bean
        @ConditionalOnMissingBean(name = "samlRequestAuditActionResolver")
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        public AuditActionResolver samlRequestAuditActionResolver() {
            return new DefaultAuditActionResolver(AuditTrailConstants.AUDIT_ACTION_POSTFIX_CREATED,
                AuditTrailConstants.AUDIT_ACTION_POSTFIX_CREATED);
        }

        @Bean
        @ConditionalOnMissingBean(name = "samlResponseAuditActionResolver")
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        public AuditActionResolver samlResponseAuditActionResolver() {
            return new DefaultAuditActionResolver(AuditTrailConstants.AUDIT_ACTION_POSTFIX_CREATED,
                AuditTrailConstants.AUDIT_ACTION_POSTFIX_CREATED);
        }

        @Bean
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        @ConditionalOnMissingBean(name = "casSamlIdPAuditTrailRecordResolutionPlanConfigurer")
        public AuditTrailRecordResolutionPlanConfigurer casSamlIdPAuditTrailRecordResolutionPlanConfigurer(
            @Qualifier("samlMetadataResolutionAuditResourceResolver")
            final AuditResourceResolver samlMetadataResolutionAuditResourceResolver,
            @Qualifier("samlResponseAuditActionResolver")
            final AuditActionResolver samlResponseAuditActionResolver,
            @Qualifier("samlRequestAuditActionResolver")
            final AuditActionResolver samlRequestAuditActionResolver,
            @Qualifier("samlResponseAuditResourceResolver")
            final AuditResourceResolver samlResponseAuditResourceResolver,
            @Qualifier("samlRequestAuditResourceResolver")
            final AuditResourceResolver samlRequestAuditResourceResolver,
            @Qualifier("samlMetadataResolutionAuditActionResolver")
            final AuditActionResolver samlMetadataResolutionAuditActionResolver) {
            return plan -> {
                plan.registerAuditResourceResolver(AuditResourceResolvers.SAML2_RESPONSE_RESOURCE_RESOLVER, samlResponseAuditResourceResolver);
                plan.registerAuditActionResolver(AuditActionResolvers.SAML2_RESPONSE_ACTION_RESOLVER, samlResponseAuditActionResolver);

                plan.registerAuditResourceResolver(AuditResourceResolvers.SAML2_REQUEST_RESOURCE_RESOLVER, samlRequestAuditResourceResolver);
                plan.registerAuditActionResolver(AuditActionResolvers.SAML2_REQUEST_ACTION_RESOLVER, samlRequestAuditActionResolver);

                plan.registerAuditResourceResolver(AuditResourceResolvers.SAML2_METADATA_RESOLUTION_RESOURCE_RESOLVER, samlMetadataResolutionAuditResourceResolver);
                plan.registerAuditActionResolver(AuditActionResolvers.SAML2_METADATA_RESOLUTION_ACTION_RESOLVER, samlMetadataResolutionAuditActionResolver);
            };
        }
    }

    @Configuration(value = "SamlIdPAttributeDefinitionsConfiguration", proxyBeanMethods = false)
    @EnableConfigurationProperties(CasConfigurationProperties.class)
    static class SamlIdPAttributeDefinitionsConfiguration {
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        @Bean
        @ConditionalOnMissingBean(name = "samlIdPAttributeDefinitionStoreConfigurer")
        public AttributeDefinitionStoreConfigurer samlIdPAttributeDefinitionStoreConfigurer(
            final CasConfigurationProperties casProperties) {
            return () -> DefaultAttributeDefinitionStore.from(new ClassPathResource("samlidp-attribute-definitions.json"));
        }
    }
}
