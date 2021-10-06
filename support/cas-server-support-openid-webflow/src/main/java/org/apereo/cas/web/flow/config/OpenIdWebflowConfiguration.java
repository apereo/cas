package org.apereo.cas.web.flow.config;

import org.apereo.cas.authentication.adaptive.AdaptiveAuthenticationPolicy;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.support.openid.web.flow.OpenIdSingleSignOnAction;
import org.apereo.cas.support.openid.web.support.DefaultOpenIdUserNameExtractor;
import org.apereo.cas.support.openid.web.support.OpenIdUserNameExtractor;
import org.apereo.cas.ticket.registry.TicketRegistrySupport;
import org.apereo.cas.web.flow.CasWebflowConfigurer;
import org.apereo.cas.web.flow.CasWebflowConstants;
import org.apereo.cas.web.flow.CasWebflowExecutionPlanConfigurer;
import org.apereo.cas.web.flow.CasWebflowLoginContextProvider;
import org.apereo.cas.web.flow.OpenIdCasWebflowLoginContextProvider;
import org.apereo.cas.web.flow.OpenIdWebflowConfigurer;
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
 * This is {@link OpenIdWebflowConfiguration}.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 * @deprecated 6.2
 */
@EnableConfigurationProperties(CasConfigurationProperties.class)
@Deprecated(since = "6.2.0")
@Configuration(value = "openIdWebflowConfiguration", proxyBeanMethods = false)
public class OpenIdWebflowConfiguration {

    @Bean
    public OpenIdUserNameExtractor defaultOpenIdUserNameExtractor() {
        return new DefaultOpenIdUserNameExtractor();
    }

    @ConditionalOnMissingBean(name = "openidWebflowConfigurer")
    @Bean
    @Autowired
    public CasWebflowConfigurer openidWebflowConfigurer(
        final CasConfigurationProperties casProperties, final ConfigurableApplicationContext applicationContext,
        @Qualifier(CasWebflowConstants.BEAN_NAME_LOGIN_FLOW_DEFINITION_REGISTRY)
        final FlowDefinitionRegistry loginFlowDefinitionRegistry,
        @Qualifier(CasWebflowConstants.BEAN_NAME_FLOW_BUILDER_SERVICES)
        final FlowBuilderServices flowBuilderServices) {
        return new OpenIdWebflowConfigurer(flowBuilderServices, loginFlowDefinitionRegistry, applicationContext, casProperties);
    }

    @Bean
    public Action openIdSingleSignOnAction(
        @Qualifier("defaultOpenIdUserNameExtractor")
        final OpenIdUserNameExtractor defaultOpenIdUserNameExtractor,
        @Qualifier("adaptiveAuthenticationPolicy")
        final AdaptiveAuthenticationPolicy adaptiveAuthenticationPolicy,
        @Qualifier("serviceTicketRequestWebflowEventResolver")
        final CasWebflowEventResolver serviceTicketRequestWebflowEventResolver,
        @Qualifier("initialAuthenticationAttemptWebflowEventResolver")
        final CasDelegatingWebflowEventResolver initialAuthenticationAttemptWebflowEventResolver,
        @Qualifier(TicketRegistrySupport.BEAN_NAME)
        final TicketRegistrySupport ticketRegistrySupport) {
        return new OpenIdSingleSignOnAction(initialAuthenticationAttemptWebflowEventResolver, serviceTicketRequestWebflowEventResolver, adaptiveAuthenticationPolicy,
            defaultOpenIdUserNameExtractor, ticketRegistrySupport);
    }

    @Bean
    @ConditionalOnMissingBean(name = "openidCasWebflowExecutionPlanConfigurer")
    public CasWebflowExecutionPlanConfigurer openidCasWebflowExecutionPlanConfigurer(
        @Qualifier("openidWebflowConfigurer")
        final CasWebflowConfigurer openidWebflowConfigurer,
        @Qualifier("openidCasWebflowLoginContextProvider")
        final CasWebflowLoginContextProvider openidCasWebflowLoginContextProvider) {
        return plan -> {
            plan.registerWebflowConfigurer(openidWebflowConfigurer);
            plan.registerWebflowLoginContextProvider(openidCasWebflowLoginContextProvider);
        };
    }

    @Bean
    @ConditionalOnMissingBean(name = "openidCasWebflowLoginContextProvider")
    @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
    public CasWebflowLoginContextProvider openidCasWebflowLoginContextProvider() {
        return new OpenIdCasWebflowLoginContextProvider();
    }
}
