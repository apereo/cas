package org.apereo.cas.web.flow.config;

import org.apereo.cas.authentication.adaptive.AdaptiveAuthenticationPolicy;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.services.ServicesManager;
import org.apereo.cas.web.DefaultTokenRequestExtractor;
import org.apereo.cas.web.TokenRequestExtractor;
import org.apereo.cas.web.flow.CasWebflowConfigurer;
import org.apereo.cas.web.flow.CasWebflowConstants;
import org.apereo.cas.web.flow.CasWebflowExecutionPlanConfigurer;
import org.apereo.cas.web.flow.TokenAuthenticationAction;
import org.apereo.cas.web.flow.TokenWebflowConfigurer;
import org.apereo.cas.web.flow.resolver.CasDelegatingWebflowEventResolver;
import org.apereo.cas.web.flow.resolver.CasWebflowEventResolver;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.webflow.definition.registry.FlowDefinitionRegistry;
import org.springframework.webflow.engine.builder.support.FlowBuilderServices;
import org.springframework.webflow.execution.Action;

/**
 * This is {@link TokenAuthenticationWebflowConfiguration}.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 */
@EnableConfigurationProperties(CasConfigurationProperties.class)
@Configuration(value = "tokenAuthenticationWebflowConfiguration", proxyBeanMethods = false)
public class TokenAuthenticationWebflowConfiguration {

    @ConditionalOnMissingBean(name = "tokenWebflowConfigurer")
    @Bean
    @Autowired
    public CasWebflowConfigurer tokenWebflowConfigurer(
        @Qualifier(CasWebflowConstants.BEAN_NAME_LOGIN_FLOW_DEFINITION_REGISTRY)
        final FlowDefinitionRegistry loginFlowDefinitionRegistry,
        @Qualifier(CasWebflowConstants.BEAN_NAME_FLOW_BUILDER_SERVICES)
        final FlowBuilderServices flowBuilderServices, final CasConfigurationProperties casProperties,
        final ConfigurableApplicationContext applicationContext) {
        return new TokenWebflowConfigurer(flowBuilderServices, loginFlowDefinitionRegistry, applicationContext, casProperties);
    }

    @Bean
    @ConditionalOnMissingBean(name = "tokenRequestExtractor")
    public TokenRequestExtractor tokenRequestExtractor() {
        return new DefaultTokenRequestExtractor();
    }

    @Bean
    @ConditionalOnMissingBean(name = "tokenAuthenticationAction")
    public Action tokenAuthenticationAction(
        @Qualifier("tokenRequestExtractor")
        final TokenRequestExtractor tokenRequestExtractor,
        @Qualifier("adaptiveAuthenticationPolicy")
        final AdaptiveAuthenticationPolicy adaptiveAuthenticationPolicy,
        @Qualifier("serviceTicketRequestWebflowEventResolver")
        final CasWebflowEventResolver serviceTicketRequestWebflowEventResolver,
        @Qualifier("initialAuthenticationAttemptWebflowEventResolver")
        final CasDelegatingWebflowEventResolver initialAuthenticationAttemptWebflowEventResolver,
        @Qualifier(ServicesManager.BEAN_NAME)
        final ServicesManager servicesManager) {
        return new TokenAuthenticationAction(initialAuthenticationAttemptWebflowEventResolver,
            serviceTicketRequestWebflowEventResolver, adaptiveAuthenticationPolicy, tokenRequestExtractor,
            servicesManager);
    }

    @Bean
    @ConditionalOnMissingBean(name = "tokenCasWebflowExecutionPlanConfigurer")
    public CasWebflowExecutionPlanConfigurer tokenCasWebflowExecutionPlanConfigurer(
        @Qualifier("tokenWebflowConfigurer")
        final CasWebflowConfigurer tokenWebflowConfigurer) {
        return plan -> plan.registerWebflowConfigurer(tokenWebflowConfigurer);
    }
}
