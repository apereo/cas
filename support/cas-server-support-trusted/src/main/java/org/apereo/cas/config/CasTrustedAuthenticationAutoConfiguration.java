package org.apereo.cas.config;

import org.apereo.cas.adaptors.trusted.authentication.handler.support.PrincipalBearingCredentialsAuthenticationHandler;
import org.apereo.cas.adaptors.trusted.authentication.principal.DefaultRemoteRequestPrincipalAttributesExtractor;
import org.apereo.cas.adaptors.trusted.authentication.principal.PrincipalBearingPrincipalResolver;
import org.apereo.cas.adaptors.trusted.authentication.principal.RemoteRequestPrincipalAttributesExtractor;
import org.apereo.cas.adaptors.trusted.web.flow.ChainingPrincipalFromRequestNonInteractiveCredentialsAction;
import org.apereo.cas.adaptors.trusted.web.flow.PrincipalFromRequestExtractorAction;
import org.apereo.cas.adaptors.trusted.web.flow.PrincipalFromRequestHeaderNonInteractiveCredentialsAction;
import org.apereo.cas.adaptors.trusted.web.flow.PrincipalFromRequestRemoteUserNonInteractiveCredentialsAction;
import org.apereo.cas.adaptors.trusted.web.flow.PrincipalFromRequestUserPrincipalNonInteractiveCredentialsAction;
import org.apereo.cas.authentication.AuthenticationEventExecutionPlanConfigurer;
import org.apereo.cas.authentication.AuthenticationHandler;
import org.apereo.cas.authentication.CoreAuthenticationUtils;
import org.apereo.cas.authentication.PrincipalElectionStrategy;
import org.apereo.cas.authentication.adaptive.AdaptiveAuthenticationPolicy;
import org.apereo.cas.authentication.attribute.AttributeDefinitionStore;
import org.apereo.cas.authentication.attribute.AttributeRepositoryResolver;
import org.apereo.cas.authentication.principal.PrincipalFactory;
import org.apereo.cas.authentication.principal.PrincipalFactoryUtils;
import org.apereo.cas.authentication.principal.PrincipalResolver;
import org.apereo.cas.authentication.principal.attribute.PersonAttributeDao;
import org.apereo.cas.authentication.principal.resolvers.ChainingPrincipalResolver;
import org.apereo.cas.authentication.principal.resolvers.EchoingPrincipalResolver;
import org.apereo.cas.authentication.principal.resolvers.PersonDirectoryPrincipalResolver;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.configuration.features.CasFeatureModule;
import org.apereo.cas.multitenancy.TenantExtractor;
import org.apereo.cas.services.ServicesManager;
import org.apereo.cas.util.CollectionUtils;
import org.apereo.cas.util.spring.boot.ConditionalOnFeatureEnabled;
import org.apereo.cas.web.flow.resolver.CasDelegatingWebflowEventResolver;
import org.apereo.cas.web.flow.resolver.CasWebflowEventResolver;
import lombok.val;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.ScopedProxyMode;
import java.util.List;

/**
 * This is {@link CasTrustedAuthenticationAutoConfiguration}.
 *
 * @author Misagh Moayyed
 * @author Dmitriy Kopylenko
 * @since 5.0.0
 */
@EnableConfigurationProperties(CasConfigurationProperties.class)
@ConditionalOnFeatureEnabled(feature = CasFeatureModule.FeatureCatalog.Authentication, module = "trusted")
@AutoConfiguration
public class CasTrustedAuthenticationAutoConfiguration {

    @Configuration(value = "TrustedAuthenticationHandlerConfiguration", proxyBeanMethods = false)
    @EnableConfigurationProperties(CasConfigurationProperties.class)
    static class TrustedAuthenticationHandlerConfiguration {
        @Bean
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        @ConditionalOnMissingBean(name = "principalBearingCredentialsAuthenticationHandler")
        public AuthenticationHandler principalBearingCredentialsAuthenticationHandler(
            final CasConfigurationProperties casProperties,
            @Qualifier("trustedPrincipalFactory")
            final PrincipalFactory trustedPrincipalFactory,
            @Qualifier(ServicesManager.BEAN_NAME)
            final ServicesManager servicesManager) {
            val trusted = casProperties.getAuthn().getTrusted();
            return new PrincipalBearingCredentialsAuthenticationHandler(trusted.getName(),
                servicesManager,
                trustedPrincipalFactory,
                trusted.getOrder());
        }
    }

    @Configuration(value = "TrustedAuthenticationPrincipalConfiguration", proxyBeanMethods = false)
    @EnableConfigurationProperties(CasConfigurationProperties.class)
    static class TrustedAuthenticationPrincipalConfiguration {
        @ConditionalOnMissingBean(name = "trustedPrincipalFactory")
        @Bean
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        public PrincipalFactory trustedPrincipalFactory() {
            return PrincipalFactoryUtils.newPrincipalFactory();
        }

    }

    @Configuration(value = "TrustedAuthenticationPrincipalResolutionConfiguration", proxyBeanMethods = false)
    @EnableConfigurationProperties(CasConfigurationProperties.class)
    static class TrustedAuthenticationPrincipalResolutionConfiguration {

        @Bean
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        @ConditionalOnMissingBean(name = "trustedPrincipalResolver")
        public PrincipalResolver trustedPrincipalResolver(
            @Qualifier(TenantExtractor.BEAN_NAME)
            final TenantExtractor tenantExtractor,
            @Qualifier(AttributeRepositoryResolver.BEAN_NAME)
            final AttributeRepositoryResolver attributeRepositoryResolver,
            final ConfigurableApplicationContext applicationContext,
            @Qualifier(AttributeDefinitionStore.BEAN_NAME)
            final AttributeDefinitionStore attributeDefinitionStore,
            @Qualifier(ServicesManager.BEAN_NAME)
            final ServicesManager servicesManager,
            @Qualifier(PrincipalElectionStrategy.BEAN_NAME)
            final PrincipalElectionStrategy principalElectionStrategy,
            final CasConfigurationProperties casProperties,
            @Qualifier("trustedPrincipalFactory")
            final PrincipalFactory trustedPrincipalFactory,
            @Qualifier(PrincipalResolver.BEAN_NAME_ATTRIBUTE_REPOSITORY)
            final PersonAttributeDao attributeRepository) {
            val personDirectory = casProperties.getPersonDirectory();
            val trusted = casProperties.getAuthn().getTrusted().getPersonDirectory();
            val attributeMerger = CoreAuthenticationUtils.getAttributeMerger(casProperties.getAuthn().getAttributeRepository().getCore().getMerger());
            val bearingPrincipalResolver = PersonDirectoryPrincipalResolver.newPersonDirectoryPrincipalResolver(
                applicationContext, trustedPrincipalFactory,
                attributeRepository, attributeMerger, PrincipalBearingPrincipalResolver.class,
                servicesManager, attributeDefinitionStore, attributeRepositoryResolver, trusted, personDirectory);
            return new ChainingPrincipalResolver(principalElectionStrategy, tenantExtractor,
                List.of(new EchoingPrincipalResolver(), bearingPrincipalResolver),
                casProperties);
        }
    }

    @Configuration(value = "TrustedAuthenticationExtractorConfiguration", proxyBeanMethods = false)
    @EnableConfigurationProperties(CasConfigurationProperties.class)
    static class TrustedAuthenticationExtractorConfiguration {
        @ConditionalOnMissingBean(name = "remoteRequestPrincipalAttributesExtractor")
        @Bean
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        public RemoteRequestPrincipalAttributesExtractor remoteRequestPrincipalAttributesExtractor(final CasConfigurationProperties casProperties) {
            val patterns = CollectionUtils.convertDirectedListToMap(casProperties.getAuthn().getTrusted().getAttributeHeaderPatterns());
            return new DefaultRemoteRequestPrincipalAttributesExtractor(patterns);
        }
    }

    @Configuration(value = "TrustedAuthenticationExecutionPlanConfiguration", proxyBeanMethods = false)
    @EnableConfigurationProperties(CasConfigurationProperties.class)
    static class TrustedAuthenticationExecutionPlanConfiguration {

        @ConditionalOnMissingBean(name = "trustedAuthenticationEventExecutionPlanConfigurer")
        @Bean
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        public AuthenticationEventExecutionPlanConfigurer trustedAuthenticationEventExecutionPlanConfigurer(
            @Qualifier("principalBearingCredentialsAuthenticationHandler")
            final AuthenticationHandler principalBearingCredentialsAuthenticationHandler,
            @Qualifier("trustedPrincipalResolver")
            final PrincipalResolver trustedPrincipalResolver) {
            return plan -> plan.registerAuthenticationHandlerWithPrincipalResolver(principalBearingCredentialsAuthenticationHandler,
                trustedPrincipalResolver);
        }

    }

    @Configuration(value = "TrustedAuthenticationActionConfiguration", proxyBeanMethods = false)
    @EnableConfigurationProperties(CasConfigurationProperties.class)
    static class TrustedAuthenticationActionConfiguration {

        @Bean
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        @ConditionalOnMissingBean(name = "principalFromRemoteUserAction")
        public PrincipalFromRequestExtractorAction principalFromRemoteUserAction(
            @Qualifier("trustedPrincipalFactory")
            final PrincipalFactory trustedPrincipalFactory,
            @Qualifier("remoteRequestPrincipalAttributesExtractor")
            final RemoteRequestPrincipalAttributesExtractor remoteRequestPrincipalAttributesExtractor,
            @Qualifier(AdaptiveAuthenticationPolicy.BEAN_NAME)
            final AdaptiveAuthenticationPolicy adaptiveAuthenticationPolicy,
            @Qualifier(CasWebflowEventResolver.BEAN_NAME_SERVICE_TICKET_EVENT_RESOLVER)
            final CasWebflowEventResolver serviceTicketRequestWebflowEventResolver,
            @Qualifier(CasDelegatingWebflowEventResolver.BEAN_NAME_INITIAL_AUTHENTICATION_EVENT_RESOLVER)
            final CasDelegatingWebflowEventResolver initialAuthenticationAttemptWebflowEventResolver) {
            return new PrincipalFromRequestRemoteUserNonInteractiveCredentialsAction(initialAuthenticationAttemptWebflowEventResolver,
                serviceTicketRequestWebflowEventResolver,
                adaptiveAuthenticationPolicy,
                trustedPrincipalFactory,
                remoteRequestPrincipalAttributesExtractor);
        }

        @Bean
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        @ConditionalOnMissingBean(name = "principalFromRemoteUserPrincipalAction")
        public PrincipalFromRequestExtractorAction principalFromRemoteUserPrincipalAction(
            @Qualifier("trustedPrincipalFactory")
            final PrincipalFactory trustedPrincipalFactory,
            @Qualifier("remoteRequestPrincipalAttributesExtractor")
            final RemoteRequestPrincipalAttributesExtractor remoteRequestPrincipalAttributesExtractor,
            @Qualifier(AdaptiveAuthenticationPolicy.BEAN_NAME)
            final AdaptiveAuthenticationPolicy adaptiveAuthenticationPolicy,
            @Qualifier(CasWebflowEventResolver.BEAN_NAME_SERVICE_TICKET_EVENT_RESOLVER)
            final CasWebflowEventResolver serviceTicketRequestWebflowEventResolver,
            @Qualifier(CasDelegatingWebflowEventResolver.BEAN_NAME_INITIAL_AUTHENTICATION_EVENT_RESOLVER)
            final CasDelegatingWebflowEventResolver initialAuthenticationAttemptWebflowEventResolver) {
            return new PrincipalFromRequestUserPrincipalNonInteractiveCredentialsAction(initialAuthenticationAttemptWebflowEventResolver,
                serviceTicketRequestWebflowEventResolver,
                adaptiveAuthenticationPolicy,
                trustedPrincipalFactory,
                remoteRequestPrincipalAttributesExtractor);
        }

        @Bean
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        @ConditionalOnMissingBean(name = "principalFromRemoteHeaderPrincipalAction")
        public PrincipalFromRequestExtractorAction principalFromRemoteHeaderPrincipalAction(
            final ConfigurableApplicationContext applicationContext,
            final CasConfigurationProperties casProperties,
            @Qualifier("trustedPrincipalFactory")
            final PrincipalFactory trustedPrincipalFactory,
            @Qualifier("remoteRequestPrincipalAttributesExtractor")
            final RemoteRequestPrincipalAttributesExtractor remoteRequestPrincipalAttributesExtractor,
            @Qualifier(AdaptiveAuthenticationPolicy.BEAN_NAME)
            final AdaptiveAuthenticationPolicy adaptiveAuthenticationPolicy,
            @Qualifier(CasWebflowEventResolver.BEAN_NAME_SERVICE_TICKET_EVENT_RESOLVER)
            final CasWebflowEventResolver serviceTicketRequestWebflowEventResolver,
            @Qualifier(CasDelegatingWebflowEventResolver.BEAN_NAME_INITIAL_AUTHENTICATION_EVENT_RESOLVER)
            final CasDelegatingWebflowEventResolver initialAuthenticationAttemptWebflowEventResolver) {
            val trusted = casProperties.getAuthn().getTrusted();
            return new PrincipalFromRequestHeaderNonInteractiveCredentialsAction(initialAuthenticationAttemptWebflowEventResolver,
                serviceTicketRequestWebflowEventResolver,
                adaptiveAuthenticationPolicy,
                trustedPrincipalFactory,
                remoteRequestPrincipalAttributesExtractor,
                trusted.getRemotePrincipalHeader());
        }

        @ConditionalOnMissingBean(name = "remoteUserAuthenticationAction")
        @Bean
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        public PrincipalFromRequestExtractorAction remoteUserAuthenticationAction(
            @Qualifier("trustedPrincipalFactory")
            final PrincipalFactory trustedPrincipalFactory,
            @Qualifier("remoteRequestPrincipalAttributesExtractor")
            final RemoteRequestPrincipalAttributesExtractor remoteRequestPrincipalAttributesExtractor,
            @Qualifier("principalFromRemoteUserAction")
            final PrincipalFromRequestExtractorAction principalFromRemoteUserAction,
            @Qualifier("principalFromRemoteUserPrincipalAction")
            final PrincipalFromRequestExtractorAction principalFromRemoteUserPrincipalAction,
            @Qualifier("principalFromRemoteHeaderPrincipalAction")
            final PrincipalFromRequestExtractorAction principalFromRemoteHeaderPrincipalAction,
            @Qualifier(AdaptiveAuthenticationPolicy.BEAN_NAME)
            final AdaptiveAuthenticationPolicy adaptiveAuthenticationPolicy,
            @Qualifier(CasWebflowEventResolver.BEAN_NAME_SERVICE_TICKET_EVENT_RESOLVER)
            final CasWebflowEventResolver serviceTicketRequestWebflowEventResolver,
            @Qualifier(CasDelegatingWebflowEventResolver.BEAN_NAME_INITIAL_AUTHENTICATION_EVENT_RESOLVER)
            final CasDelegatingWebflowEventResolver initialAuthenticationAttemptWebflowEventResolver) {
            val chain = new ChainingPrincipalFromRequestNonInteractiveCredentialsAction(initialAuthenticationAttemptWebflowEventResolver,
                serviceTicketRequestWebflowEventResolver,
                adaptiveAuthenticationPolicy,
                trustedPrincipalFactory,
                remoteRequestPrincipalAttributesExtractor);
            chain.addAction(principalFromRemoteUserAction);
            chain.addAction(principalFromRemoteUserPrincipalAction);
            chain.addAction(principalFromRemoteHeaderPrincipalAction);
            return chain;
        }

    }
}
