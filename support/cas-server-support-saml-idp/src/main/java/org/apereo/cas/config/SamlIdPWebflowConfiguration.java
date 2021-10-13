package org.apereo.cas.config;

import org.apereo.cas.authentication.AuthenticationServiceSelectionPlan;
import org.apereo.cas.authentication.MultifactorAuthenticationTrigger;
import org.apereo.cas.authentication.attribute.AttributeDefinitionStore;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.consent.ConsentActivationStrategy;
import org.apereo.cas.consent.ConsentEngine;
import org.apereo.cas.consent.ConsentableAttributeBuilder;
import org.apereo.cas.services.ServicesManager;
import org.apereo.cas.support.saml.OpenSamlConfigBean;
import org.apereo.cas.support.saml.services.idp.metadata.cache.SamlRegisteredServiceCachingMetadataResolver;
import org.apereo.cas.support.saml.web.consent.SamlIdPConsentSingleSignOnParticipationStrategy;
import org.apereo.cas.support.saml.web.consent.SamlIdPConsentableAttributeBuilder;
import org.apereo.cas.support.saml.web.flow.SamlIdPMetadataUIAction;
import org.apereo.cas.support.saml.web.flow.SamlIdPWebflowConfigurer;
import org.apereo.cas.support.saml.web.idp.web.SamlIdPMultifactorAuthenticationTrigger;
import org.apereo.cas.support.saml.web.idp.web.SamlIdPSingleSignOnParticipationStrategy;
import org.apereo.cas.ticket.registry.TicketRegistrySupport;
import org.apereo.cas.web.flow.CasWebflowConfigurer;
import org.apereo.cas.web.flow.CasWebflowConstants;
import org.apereo.cas.web.flow.CasWebflowExecutionPlanConfigurer;
import org.apereo.cas.web.flow.SingleSignOnParticipationStrategy;
import org.apereo.cas.web.flow.SingleSignOnParticipationStrategyConfigurer;
import org.apereo.cas.web.flow.login.SessionStoreTicketGrantingTicketAction;
import org.apereo.cas.web.flow.resolver.CasDelegatingWebflowEventResolver;
import org.apereo.cas.web.flow.resolver.CasWebflowEventResolver;
import org.apereo.cas.web.flow.resolver.impl.CasWebflowEventResolutionConfigurationContext;
import org.apereo.cas.web.flow.resolver.impl.mfa.DefaultMultifactorAuthenticationProviderWebflowEventResolver;

import lombok.val;
import org.pac4j.core.context.session.SessionStore;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.ScopedProxyMode;
import org.springframework.webflow.definition.registry.FlowDefinitionRegistry;
import org.springframework.webflow.engine.builder.support.FlowBuilderServices;
import org.springframework.webflow.execution.Action;

import java.util.Objects;

/**
 * This is {@link SamlIdPWebflowConfiguration}.
 *
 * @author Misagh Moayyed
 * @since 5.1.0
 */
@EnableConfigurationProperties(CasConfigurationProperties.class)
@Configuration(value = "samlIdPWebflowConfiguration", proxyBeanMethods = false)
public class SamlIdPWebflowConfiguration {

    @Configuration(value = "SamlIdPWebflowCoreConfiguration", proxyBeanMethods = false)
    @EnableConfigurationProperties(CasConfigurationProperties.class)
    public static class SamlIdPWebflowCoreConfiguration {
        @Bean
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        @ConditionalOnMissingBean(name = "samlIdPCasWebflowExecutionPlanConfigurer")
        public CasWebflowExecutionPlanConfigurer samlIdPCasWebflowExecutionPlanConfigurer(
            @Qualifier("samlIdPWebConfigurer")
            final CasWebflowConfigurer samlIdPWebConfigurer) {
            return plan -> plan.registerWebflowConfigurer(samlIdPWebConfigurer);
        }

        @ConditionalOnMissingBean(name = "samlIdPWebConfigurer")
        @Bean
        @Autowired
        public CasWebflowConfigurer samlIdPWebConfigurer(
            final CasConfigurationProperties casProperties,
            final ConfigurableApplicationContext applicationContext,
            @Qualifier(CasWebflowConstants.BEAN_NAME_LOGIN_FLOW_DEFINITION_REGISTRY)
            final FlowDefinitionRegistry loginFlowDefinitionRegistry,
            @Qualifier(CasWebflowConstants.BEAN_NAME_FLOW_BUILDER_SERVICES)
            final FlowBuilderServices flowBuilderServices) {
            return new SamlIdPWebflowConfigurer(flowBuilderServices, loginFlowDefinitionRegistry, applicationContext, casProperties);
        }
    }

    @Configuration(value = "SamlIdPWebflowActionsConfiguration", proxyBeanMethods = false)
    @EnableConfigurationProperties(CasConfigurationProperties.class)
    public static class SamlIdPWebflowActionsConfiguration {
        @Bean
        @ConditionalOnMissingBean(name = "samlIdPSessionStoreTicketGrantingTicketAction")
        public Action samlIdPSessionStoreTicketGrantingTicketAction(
            @Qualifier("samlIdPDistributedSessionStore")
            final SessionStore samlIdPDistributedSessionStore) {
            return new SessionStoreTicketGrantingTicketAction(samlIdPDistributedSessionStore);
        }

        @ConditionalOnMissingBean(name = "samlIdPMetadataUIParserAction")
        @Bean
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        public Action samlIdPMetadataUIParserAction(
            @Qualifier(ServicesManager.BEAN_NAME)
            final ServicesManager servicesManager,
            @Qualifier(AuthenticationServiceSelectionPlan.BEAN_NAME)
            final AuthenticationServiceSelectionPlan selectionStrategies,
            @Qualifier("defaultSamlRegisteredServiceCachingMetadataResolver")
            final SamlRegisteredServiceCachingMetadataResolver defaultSamlRegisteredServiceCachingMetadataResolver) {
            return new SamlIdPMetadataUIAction(servicesManager, defaultSamlRegisteredServiceCachingMetadataResolver, selectionStrategies);
        }
    }

    @Configuration(value = "SamlIdPWebflowSingleSignOnConfiguration", proxyBeanMethods = false)
    @EnableConfigurationProperties(CasConfigurationProperties.class)
    public static class SamlIdPWebflowSingleSignOnConfiguration {
        @Bean
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        @ConditionalOnMissingBean(name = "samlIdPSingleSignOnParticipationStrategy")
        public SingleSignOnParticipationStrategy samlIdPSingleSignOnParticipationStrategy(
            @Qualifier(ServicesManager.BEAN_NAME)
            final ServicesManager servicesManager,
            @Qualifier(TicketRegistrySupport.BEAN_NAME)
            final TicketRegistrySupport ticketRegistrySupport,
            @Qualifier(AuthenticationServiceSelectionPlan.BEAN_NAME)
            final AuthenticationServiceSelectionPlan selectionStrategies) {
            return new SamlIdPSingleSignOnParticipationStrategy(servicesManager, ticketRegistrySupport, selectionStrategies);
        }
    }

    @Configuration(value = "SamlIdPWebflowSingleSignOnPlanConfiguration", proxyBeanMethods = false)
    @EnableConfigurationProperties(CasConfigurationProperties.class)
    public static class SamlIdPWebflowSingleSignOnPlanConfiguration {
        @Bean
        @ConditionalOnMissingBean(name = "samlIdPSingleSignOnParticipationStrategyConfigurer")
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        public SingleSignOnParticipationStrategyConfigurer samlIdPSingleSignOnParticipationStrategyConfigurer(
            @Qualifier("samlIdPSingleSignOnParticipationStrategy")
            final SingleSignOnParticipationStrategy samlIdPSingleSignOnParticipationStrategy) {
            return chain -> chain.addStrategy(samlIdPSingleSignOnParticipationStrategy);
        }
    }

    @Configuration(value = "SamlIdPWebflowMultifactorAuthenticationConfiguration", proxyBeanMethods = false)
    @EnableConfigurationProperties(CasConfigurationProperties.class)
    public static class SamlIdPWebflowMultifactorAuthenticationConfiguration {
        @Bean
        @ConditionalOnMissingBean(name = "samlIdPMultifactorAuthenticationTrigger")
        @Autowired
        public MultifactorAuthenticationTrigger samlIdPMultifactorAuthenticationTrigger(
            final CasConfigurationProperties casProperties,
            final ConfigurableApplicationContext applicationContext,
            @Qualifier(OpenSamlConfigBean.DEFAULT_BEAN_NAME)
            final OpenSamlConfigBean openSamlConfigBean,
            @Qualifier("samlIdPDistributedSessionStore")
            final SessionStore samlIdPDistributedSessionStore) {
            return new SamlIdPMultifactorAuthenticationTrigger(openSamlConfigBean, samlIdPDistributedSessionStore, applicationContext, casProperties);
        }
    }

    @Configuration(value = "SamlIdPWebflowEventsConfiguration", proxyBeanMethods = false)
    @EnableConfigurationProperties(CasConfigurationProperties.class)
    public static class SamlIdPWebflowEventsConfiguration {
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        @Bean
        @ConditionalOnMissingBean(name = "samlIdPAuthenticationContextWebflowEventResolver")
        public CasWebflowEventResolver samlIdPAuthenticationContextWebflowEventResolver(
            @Qualifier("samlIdPMultifactorAuthenticationTrigger")
            final MultifactorAuthenticationTrigger samlIdPMultifactorAuthenticationTrigger,
            @Qualifier("initialAuthenticationAttemptWebflowEventResolver")
            final CasDelegatingWebflowEventResolver initialAuthenticationAttemptWebflowEventResolver,
            @Qualifier("casWebflowConfigurationContext")
            final CasWebflowEventResolutionConfigurationContext casWebflowConfigurationContext) {
            val r = new DefaultMultifactorAuthenticationProviderWebflowEventResolver(casWebflowConfigurationContext, samlIdPMultifactorAuthenticationTrigger);
            Objects.requireNonNull(initialAuthenticationAttemptWebflowEventResolver).addDelegate(r);
            return r;
        }
    }

    @Configuration(value = "SamlIdPConsentWebflowConfiguration", proxyBeanMethods = false)
    @ConditionalOnBean(name = ConsentEngine.BEAN_NAME)
    public static class SamlIdPConsentWebflowConfiguration {
        @Bean
        @ConditionalOnMissingBean(name = "samlIdPConsentableAttributeBuilder")
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        @Autowired
        public ConsentableAttributeBuilder samlIdPConsentableAttributeBuilder(
            @Qualifier("attributeDefinitionStore")
            final AttributeDefinitionStore attributeDefinitionStore) {
            return new SamlIdPConsentableAttributeBuilder(attributeDefinitionStore);
        }
    }

    @Configuration(value = "SamlIdPConsentSingleSignOnWebflowConfiguration", proxyBeanMethods = false)
    @EnableConfigurationProperties(CasConfigurationProperties.class)
    @ConditionalOnBean(name = ConsentEngine.BEAN_NAME)
    public static class SamlIdPConsentSingleSignOnWebflowConfiguration {
        @Bean
        @ConditionalOnMissingBean(name = "samlIdPConsentSingleSignOnParticipationStrategyConfigurer")
        @RefreshScope
        @Autowired
        public SingleSignOnParticipationStrategyConfigurer samlIdPConsentSingleSignOnParticipationStrategyConfigurer(
            @Qualifier(ServicesManager.BEAN_NAME)
            final ServicesManager servicesManager,
            @Qualifier(TicketRegistrySupport.BEAN_NAME)
            final TicketRegistrySupport ticketRegistrySupport,
            @Qualifier(AuthenticationServiceSelectionPlan.BEAN_NAME)
            final AuthenticationServiceSelectionPlan authenticationServiceSelectionPlan,
            @Qualifier(ConsentActivationStrategy.BEAN_NAME)
            final ConsentActivationStrategy consentActivationStrategy) {
            val ssoStrategy = new SamlIdPConsentSingleSignOnParticipationStrategy(servicesManager,
                ticketRegistrySupport, authenticationServiceSelectionPlan, consentActivationStrategy);
            return chain -> chain.addStrategy(ssoStrategy);
        }
    }
}
