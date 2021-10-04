package org.apereo.cas.web.flow.config;

import org.apereo.cas.authentication.AuthenticationSystemSupport;
import org.apereo.cas.authentication.adaptive.AdaptiveAuthenticationPolicy;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.support.wsfederation.WsFederationConfiguration;
import org.apereo.cas.support.wsfederation.WsFederationHelper;
import org.apereo.cas.support.wsfederation.web.WsFederationCookieManager;
import org.apereo.cas.util.spring.BeanContainer;
import org.apereo.cas.web.flow.CasWebflowConfigurer;
import org.apereo.cas.web.flow.CasWebflowConstants;
import org.apereo.cas.web.flow.CasWebflowExecutionPlanConfigurer;
import org.apereo.cas.web.flow.WsFederationAction;
import org.apereo.cas.web.flow.WsFederationRequestBuilder;
import org.apereo.cas.web.flow.WsFederationResponseValidator;
import org.apereo.cas.web.flow.WsFederationWebflowConfigurer;
import org.apereo.cas.web.flow.resolver.CasDelegatingWebflowEventResolver;
import org.apereo.cas.web.flow.resolver.CasWebflowEventResolver;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
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

/**
 * This is {@link WsFederationAuthenticationWebflowConfiguration}.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 */
@EnableConfigurationProperties(CasConfigurationProperties.class)
@Configuration(value = "wsFederationAuthenticationWebflowConfiguration", proxyBeanMethods = false)
public class WsFederationAuthenticationWebflowConfiguration {

    @ConditionalOnMissingBean(name = "wsFederationWebflowConfigurer")
    @Bean
    @Autowired
    public CasWebflowConfigurer wsFederationWebflowConfigurer(
        final CasConfigurationProperties casProperties,
        final ConfigurableApplicationContext applicationContext,
        @Qualifier(CasWebflowConstants.BEAN_NAME_LOGIN_FLOW_DEFINITION_REGISTRY)
        final FlowDefinitionRegistry loginFlowDefinitionRegistry,
        @Qualifier(CasWebflowConstants.BEAN_NAME_FLOW_BUILDER_SERVICES)
        final FlowBuilderServices flowBuilderServices) {
        return new WsFederationWebflowConfigurer(flowBuilderServices,
            loginFlowDefinitionRegistry, applicationContext, casProperties);
    }

    @Bean
    @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
    @ConditionalOnMissingBean(name = "wsFederationAction")
    public Action wsFederationAction(
        @Qualifier("wsFederationRequestBuilder")
        final WsFederationRequestBuilder wsFederationRequestBuilder,
        @Qualifier("wsFederationResponseValidator")
        final WsFederationResponseValidator wsFederationResponseValidator,
        @Qualifier("adaptiveAuthenticationPolicy")
        final AdaptiveAuthenticationPolicy adaptiveAuthenticationPolicy,
        @Qualifier("serviceTicketRequestWebflowEventResolver")
        final CasWebflowEventResolver serviceTicketRequestWebflowEventResolver,
        @Qualifier("initialAuthenticationAttemptWebflowEventResolver")
        final CasDelegatingWebflowEventResolver initialAuthenticationAttemptWebflowEventResolver) {
        return new WsFederationAction(initialAuthenticationAttemptWebflowEventResolver,
            serviceTicketRequestWebflowEventResolver, adaptiveAuthenticationPolicy, wsFederationRequestBuilder,
            wsFederationResponseValidator);
    }

    @Bean
    @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
    @ConditionalOnMissingBean(name = "wsFederationRequestBuilder")
    public WsFederationRequestBuilder wsFederationRequestBuilder(
        @Qualifier("wsFederationConfigurations")
        final BeanContainer<WsFederationConfiguration> wsFederationConfigurations,
        @Qualifier("wsFederationHelper")
        final WsFederationHelper wsFederationHelper) {
        return new WsFederationRequestBuilder(wsFederationConfigurations.toList(), wsFederationHelper);
    }

    @Bean
    @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
    @ConditionalOnMissingBean(name = "wsFederationResponseValidator")
    public WsFederationResponseValidator wsFederationResponseValidator(
        @Qualifier("wsFederationConfigurations")
        final BeanContainer<WsFederationConfiguration> wsFederationConfigurations,
        @Qualifier("wsFederationCookieManager")
        final WsFederationCookieManager wsFederationCookieManager,
        @Qualifier(AuthenticationSystemSupport.BEAN_NAME)
        final AuthenticationSystemSupport authenticationSystemSupport,
        @Qualifier("wsFederationHelper")
        final WsFederationHelper wsFederationHelper) {
        return new WsFederationResponseValidator(wsFederationHelper,
            wsFederationConfigurations.toList(), authenticationSystemSupport, wsFederationCookieManager);
    }

    @Bean
    @ConditionalOnMissingBean(name = "wsFederationCasWebflowExecutionPlanConfigurer")
    public CasWebflowExecutionPlanConfigurer wsFederationCasWebflowExecutionPlanConfigurer(
        @Qualifier("wsFederationWebflowConfigurer")
        final CasWebflowConfigurer wsFederationWebflowConfigurer) {
        return plan -> plan.registerWebflowConfigurer(wsFederationWebflowConfigurer);
    }
}
