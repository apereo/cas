package org.apereo.cas.web.flow.config;

import org.apereo.cas.authentication.AuthenticationSystemSupport;
import org.apereo.cas.authentication.adaptive.AdaptiveAuthenticationPolicy;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.support.wsfederation.WsFederationConfiguration;
import org.apereo.cas.support.wsfederation.WsFederationHelper;
import org.apereo.cas.support.wsfederation.web.WsFederationCookieManager;
import org.apereo.cas.web.flow.CasWebflowConfigurer;
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
import org.springframework.webflow.definition.registry.FlowDefinitionRegistry;
import org.springframework.webflow.engine.builder.support.FlowBuilderServices;
import org.springframework.webflow.execution.Action;

import java.util.Collection;

/**
 * This is {@link WsFederationAuthenticationWebflowConfiguration}.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 */
@EnableConfigurationProperties(CasConfigurationProperties.class)
@Configuration(value = "wsFederationAuthenticationWebflowConfiguration", proxyBeanMethods = false)
public class WsFederationAuthenticationWebflowConfiguration {

    @Autowired
    @Qualifier("wsFederationConfigurations")
    private Collection<WsFederationConfiguration> wsFederationConfigurations;

    @ConditionalOnMissingBean(name = "wsFederationWebflowConfigurer")
    @Bean
    @Autowired
    public CasWebflowConfigurer wsFederationWebflowConfigurer(final CasConfigurationProperties casProperties, final ConfigurableApplicationContext applicationContext,
                                                              @Qualifier("loginFlowDefinitionRegistry")
                                                              final FlowDefinitionRegistry loginFlowDefinitionRegistry,
                                                              @Qualifier("flowBuilderServices")
                                                              final FlowBuilderServices flowBuilderServices) {
        return new WsFederationWebflowConfigurer(flowBuilderServices, loginFlowDefinitionRegistry, applicationContext, casProperties);
    }

    @Bean
    @RefreshScope
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
        return new WsFederationAction(initialAuthenticationAttemptWebflowEventResolver, serviceTicketRequestWebflowEventResolver, adaptiveAuthenticationPolicy, wsFederationRequestBuilder,
            wsFederationResponseValidator);
    }

    @Bean
    @RefreshScope
    @ConditionalOnMissingBean(name = "wsFederationRequestBuilder")
    public WsFederationRequestBuilder wsFederationRequestBuilder(
        @Qualifier("wsFederationHelper")
        final WsFederationHelper wsFederationHelper) {
        return new WsFederationRequestBuilder(wsFederationConfigurations, wsFederationHelper);
    }

    @Bean
    @RefreshScope
    @ConditionalOnMissingBean(name = "wsFederationResponseValidator")
    public WsFederationResponseValidator wsFederationResponseValidator(
        @Qualifier("wsFederationCookieManager")
        final WsFederationCookieManager wsFederationCookieManager,
        @Qualifier("defaultAuthenticationSystemSupport")
        final AuthenticationSystemSupport authenticationSystemSupport,
        @Qualifier("wsFederationHelper")
        final WsFederationHelper wsFederationHelper) {
        return new WsFederationResponseValidator(wsFederationHelper, wsFederationConfigurations, authenticationSystemSupport, wsFederationCookieManager);
    }

    @Bean
    @ConditionalOnMissingBean(name = "wsFederationCasWebflowExecutionPlanConfigurer")
    public CasWebflowExecutionPlanConfigurer wsFederationCasWebflowExecutionPlanConfigurer(
        @Qualifier("wsFederationWebflowConfigurer")
        final CasWebflowConfigurer wsFederationWebflowConfigurer) {
        return plan -> plan.registerWebflowConfigurer(wsFederationWebflowConfigurer);
    }
}
