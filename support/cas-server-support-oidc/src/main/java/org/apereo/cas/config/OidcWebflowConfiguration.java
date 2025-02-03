package org.apereo.cas.config;

import org.apereo.cas.authentication.AuthenticationServiceSelectionStrategy;
import org.apereo.cas.authentication.MultifactorAuthenticationTrigger;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.configuration.features.CasFeatureModule;
import org.apereo.cas.oidc.OidcConfigurationContext;
import org.apereo.cas.oidc.web.flow.OidcCasWebflowLoginContextProvider;
import org.apereo.cas.oidc.web.flow.OidcRegisteredServiceUIAction;
import org.apereo.cas.oidc.web.flow.OidcUnmetAuthenticationRequirementWebflowExceptionHandler;
import org.apereo.cas.oidc.web.flow.OidcWebflowConfigurer;
import org.apereo.cas.oidc.web.flow.account.OidcAccountProfileAccessTokenAction;
import org.apereo.cas.oidc.web.flow.account.OidcAccountProfileRemoveAccessTokenAction;
import org.apereo.cas.services.ServicesManager;
import org.apereo.cas.ticket.registry.TicketRegistry;
import org.apereo.cas.util.spring.boot.ConditionalOnFeatureEnabled;
import org.apereo.cas.web.flow.CasWebflowConfigurer;
import org.apereo.cas.web.flow.CasWebflowConstants;
import org.apereo.cas.web.flow.CasWebflowExecutionPlanConfigurer;
import org.apereo.cas.web.flow.CasWebflowLoginContextProvider;
import org.apereo.cas.web.flow.actions.WebflowActionBeanSupplier;
import org.apereo.cas.web.flow.authentication.CasWebflowExceptionHandler;
import org.apereo.cas.web.flow.resolver.CasDelegatingWebflowEventResolver;
import org.apereo.cas.web.flow.resolver.CasWebflowEventResolver;
import org.apereo.cas.web.flow.resolver.impl.CasWebflowEventResolutionConfigurationContext;
import org.apereo.cas.web.flow.resolver.impl.mfa.DefaultMultifactorAuthenticationProviderWebflowEventResolver;
import org.apereo.cas.web.support.ArgumentExtractor;
import lombok.val;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;
import org.springframework.context.annotation.ScopedProxyMode;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.webflow.definition.registry.FlowDefinitionRegistry;
import org.springframework.webflow.engine.builder.support.FlowBuilderServices;
import org.springframework.webflow.execution.Action;

/**
 * This is {@link OidcWebflowConfiguration}.
 *
 * @author Misagh Moayyed
 * @since 7.1.0
 */
@EnableConfigurationProperties(CasConfigurationProperties.class)
@ConditionalOnFeatureEnabled(feature = CasFeatureModule.FeatureCatalog.OpenIDConnect)
@Configuration(value = "OidcWebflowConfiguration", proxyBeanMethods = false)
class OidcWebflowConfiguration {
    @ConditionalOnMissingBean(name = "oidcUnmetAuthenticationRequirementWebflowExceptionHandler")
    @Bean
    @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
    public CasWebflowExceptionHandler oidcUnmetAuthenticationRequirementWebflowExceptionHandler(
        @Qualifier(OidcConfigurationContext.BEAN_NAME)
        final OidcConfigurationContext oidcConfigurationContext) {
        return new OidcUnmetAuthenticationRequirementWebflowExceptionHandler(oidcConfigurationContext);
    }

    @ConditionalOnMissingBean(name = "oidcCasWebflowExecutionPlanConfigurer")
    @Bean
    @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
    public CasWebflowExecutionPlanConfigurer oidcCasWebflowExecutionPlanConfigurer(
        @Qualifier("oidcWebflowConfigurer")
        final CasWebflowConfigurer oidcWebflowConfigurer,
        @Qualifier("oidcLocaleChangeInterceptor")
        final HandlerInterceptor oidcLocaleChangeInterceptor,
        @Qualifier("oidcCasWebflowLoginContextProvider")
        final CasWebflowLoginContextProvider oidcCasWebflowLoginContextProvider) {
        return plan -> {
            plan.registerWebflowConfigurer(oidcWebflowConfigurer);
            plan.registerWebflowInterceptor(oidcLocaleChangeInterceptor);
            plan.registerWebflowLoginContextProvider(oidcCasWebflowLoginContextProvider);
        };
    }

    @Bean
    @ConditionalOnMissingBean(name = "oidcCasWebflowLoginContextProvider")
    @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
    public CasWebflowLoginContextProvider oidcCasWebflowLoginContextProvider(
        @Qualifier(ArgumentExtractor.BEAN_NAME)
        final ArgumentExtractor argumentExtractor) {
        return new OidcCasWebflowLoginContextProvider(argumentExtractor);
    }


    @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
    @Bean
    @Lazy(false)
    public CasWebflowEventResolver oidcAuthenticationContextWebflowEventResolver(
        @Qualifier(CasDelegatingWebflowEventResolver.BEAN_NAME_INITIAL_AUTHENTICATION_EVENT_RESOLVER)
        final CasDelegatingWebflowEventResolver initialAuthenticationAttemptWebflowEventResolver,
        @Qualifier(CasWebflowEventResolutionConfigurationContext.BEAN_NAME)
        final CasWebflowEventResolutionConfigurationContext casWebflowConfigurationContext,
        @Qualifier("oidcMultifactorAuthenticationTrigger")
        final MultifactorAuthenticationTrigger oidcMultifactorAuthenticationTrigger) {
        val resolver = new DefaultMultifactorAuthenticationProviderWebflowEventResolver(
            casWebflowConfigurationContext, oidcMultifactorAuthenticationTrigger);
        initialAuthenticationAttemptWebflowEventResolver.addDelegate(resolver);
        return resolver;
    }

    @ConditionalOnMissingBean(name = "oidcWebflowConfigurer")
    @Bean
    @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
    public CasWebflowConfigurer oidcWebflowConfigurer(
        @Qualifier(CasWebflowConstants.BEAN_NAME_FLOW_BUILDER_SERVICES)
        final FlowBuilderServices flowBuilderServices,
        @Qualifier(CasWebflowConstants.BEAN_NAME_FLOW_DEFINITION_REGISTRY)
        final FlowDefinitionRegistry flowDefinitionRegistry,
        final ConfigurableApplicationContext applicationContext,
        final CasConfigurationProperties casProperties) {
        return new OidcWebflowConfigurer(flowBuilderServices, flowDefinitionRegistry, applicationContext, casProperties);
    }

    @ConditionalOnMissingBean(name = CasWebflowConstants.ACTION_ID_OIDC_REGISTERED_SERVICE_UI)
    @Bean
    @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
    public Action oidcRegisteredServiceUIAction(
        final ConfigurableApplicationContext applicationContext,
        final CasConfigurationProperties casProperties,
        @Qualifier("oauth20AuthenticationRequestServiceSelectionStrategy")
        final AuthenticationServiceSelectionStrategy oauth20AuthenticationServiceSelectionStrategy,
        @Qualifier(ServicesManager.BEAN_NAME)
        final ServicesManager servicesManager) {
        return WebflowActionBeanSupplier.builder()
            .withApplicationContext(applicationContext)
            .withProperties(casProperties)
            .withAction(() -> new OidcRegisteredServiceUIAction(servicesManager, oauth20AuthenticationServiceSelectionStrategy))
            .withId(CasWebflowConstants.ACTION_ID_OIDC_REGISTERED_SERVICE_UI)
            .build()
            .get();
    }


    @Bean
    @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
    @ConditionalOnMissingBean(name = CasWebflowConstants.ACTION_ID_ACCOUNT_PROFILE_ACCESS_TOKENS)
    public Action oidcAccountProfileAccessTokensAction(
        final ConfigurableApplicationContext applicationContext,
        final CasConfigurationProperties casProperties,
        @Qualifier(TicketRegistry.BEAN_NAME)
        final TicketRegistry ticketRegistry) {
        return WebflowActionBeanSupplier.builder()
            .withApplicationContext(applicationContext)
            .withProperties(casProperties)
            .withAction(() -> new OidcAccountProfileAccessTokenAction(ticketRegistry))
            .withId(CasWebflowConstants.ACTION_ID_ACCOUNT_PROFILE_ACCESS_TOKENS)
            .build()
            .get();
    }

    @Bean
    @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
    @ConditionalOnMissingBean(name = CasWebflowConstants.ACTION_ID_ACCOUNT_PROFILE_REMOVE_OIDC_ACCESS_TOKEN)
    public Action accountProfileOidcRemoveAccessTokenAction(
        final ConfigurableApplicationContext applicationContext,
        final CasConfigurationProperties casProperties,
        @Qualifier(TicketRegistry.BEAN_NAME)
        final TicketRegistry ticketRegistry) {
        return WebflowActionBeanSupplier.builder()
            .withApplicationContext(applicationContext)
            .withProperties(casProperties)
            .withAction(() -> new OidcAccountProfileRemoveAccessTokenAction(ticketRegistry))
            .withId(CasWebflowConstants.ACTION_ID_ACCOUNT_PROFILE_REMOVE_OIDC_ACCESS_TOKEN)
            .build()
            .get();
    }
}
