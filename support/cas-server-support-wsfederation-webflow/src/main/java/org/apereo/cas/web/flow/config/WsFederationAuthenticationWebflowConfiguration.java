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

import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.DependsOn;
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
@Configuration("wsFederationAuthenticationWebflowConfiguration")
@EnableConfigurationProperties(CasConfigurationProperties.class)
public class WsFederationAuthenticationWebflowConfiguration {

    @Autowired
    private ConfigurableApplicationContext applicationContext;

    @Autowired
    private CasConfigurationProperties casProperties;

    @Autowired
    @Qualifier("loginFlowRegistry")
    private ObjectProvider<FlowDefinitionRegistry> loginFlowDefinitionRegistry;

    @Autowired
    private ObjectProvider<FlowBuilderServices> flowBuilderServices;

    @Autowired
    @Qualifier("wsFederationCookieManager")
    private ObjectProvider<WsFederationCookieManager> wsFederationCookieManager;

    @Autowired
    @Qualifier("adaptiveAuthenticationPolicy")
    private ObjectProvider<AdaptiveAuthenticationPolicy> adaptiveAuthenticationPolicy;

    @Autowired
    @Qualifier("defaultAuthenticationSystemSupport")
    private ObjectProvider<AuthenticationSystemSupport> authenticationSystemSupport;

    @Autowired
    @Qualifier("wsFederationConfigurations")
    private Collection<WsFederationConfiguration> wsFederationConfigurations;

    @Autowired
    @Qualifier("wsFederationHelper")
    private ObjectProvider<WsFederationHelper> wsFederationHelper;

    @Autowired
    @Qualifier("serviceTicketRequestWebflowEventResolver")
    private ObjectProvider<CasWebflowEventResolver> serviceTicketRequestWebflowEventResolver;

    @Autowired
    @Qualifier("initialAuthenticationAttemptWebflowEventResolver")
    private ObjectProvider<CasDelegatingWebflowEventResolver> initialAuthenticationAttemptWebflowEventResolver;

    @ConditionalOnMissingBean(name = "wsFederationWebflowConfigurer")
    @Bean
    @DependsOn("defaultWebflowConfigurer")
    public CasWebflowConfigurer wsFederationWebflowConfigurer() {
        return new WsFederationWebflowConfigurer(flowBuilderServices.getObject(),
            loginFlowDefinitionRegistry.getObject(), applicationContext, casProperties);
    }

    @Bean
    @RefreshScope
    @ConditionalOnMissingBean(name = "wsFederationAction")
    public Action wsFederationAction() {
        return new WsFederationAction(initialAuthenticationAttemptWebflowEventResolver.getObject(),
            serviceTicketRequestWebflowEventResolver.getObject(),
            adaptiveAuthenticationPolicy.getObject(),
            wsFederationRequestBuilder(),
            wsFederationResponseValidator());
    }

    @Bean
    @RefreshScope
    @ConditionalOnMissingBean(name = "wsFederationRequestBuilder")
    public WsFederationRequestBuilder wsFederationRequestBuilder() {
        return new WsFederationRequestBuilder(wsFederationConfigurations, wsFederationHelper.getObject());
    }

    @Bean
    @RefreshScope
    @ConditionalOnMissingBean(name = "wsFederationResponseValidator")
    public WsFederationResponseValidator wsFederationResponseValidator() {
        return new WsFederationResponseValidator(wsFederationHelper.getObject(),
            wsFederationConfigurations,
            authenticationSystemSupport.getObject(),
            wsFederationCookieManager.getObject());
    }

    @Bean
    @ConditionalOnMissingBean(name = "wsFederationCasWebflowExecutionPlanConfigurer")
    public CasWebflowExecutionPlanConfigurer wsFederationCasWebflowExecutionPlanConfigurer() {
        return plan -> plan.registerWebflowConfigurer(wsFederationWebflowConfigurer());
    }
}
