package org.apereo.cas.config;

import org.apereo.cas.authentication.adaptive.AdaptiveAuthenticationPolicy;
import org.apereo.cas.authentication.principal.PrincipalFactory;
import org.apereo.cas.authentication.principal.PrincipalFactoryUtils;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.configuration.features.CasFeatureModule;
import org.apereo.cas.util.spring.boot.ConditionalOnFeatureEnabled;
import org.apereo.cas.web.flow.BasicAuthenticationAction;
import org.apereo.cas.web.flow.BasicAuthenticationCasMultifactorWebflowCustomizer;
import org.apereo.cas.web.flow.BasicAuthenticationWebflowConfigurer;
import org.apereo.cas.web.flow.CasWebflowConfigurer;
import org.apereo.cas.web.flow.CasWebflowConstants;
import org.apereo.cas.web.flow.CasWebflowExecutionPlanConfigurer;
import org.apereo.cas.web.flow.configurer.CasMultifactorWebflowCustomizer;
import org.apereo.cas.web.flow.resolver.CasDelegatingWebflowEventResolver;
import org.apereo.cas.web.flow.resolver.CasWebflowEventResolver;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ScopedProxyMode;
import org.springframework.webflow.definition.registry.FlowDefinitionRegistry;
import org.springframework.webflow.engine.builder.support.FlowBuilderServices;
import org.springframework.webflow.execution.Action;

/**
 * This is {@link CasBasicAuthenticationAutoConfiguration}.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 */
@EnableConfigurationProperties(CasConfigurationProperties.class)
@ConditionalOnFeatureEnabled(feature = CasFeatureModule.FeatureCatalog.Authentication, module = "basic")
@AutoConfiguration
public class CasBasicAuthenticationAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean(name = CasWebflowConstants.ACTION_ID_BASIC_AUTHENTICATION)
    @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
    public Action basicAuthenticationAction(
        @Qualifier("adaptiveAuthenticationPolicy")
        final AdaptiveAuthenticationPolicy adaptiveAuthenticationPolicy,
        @Qualifier("serviceTicketRequestWebflowEventResolver")
        final CasWebflowEventResolver serviceTicketRequestWebflowEventResolver,
        @Qualifier("initialAuthenticationAttemptWebflowEventResolver")
        final CasDelegatingWebflowEventResolver initialAuthenticationAttemptWebflowEventResolver) {
        return new BasicAuthenticationAction(initialAuthenticationAttemptWebflowEventResolver,
            serviceTicketRequestWebflowEventResolver, adaptiveAuthenticationPolicy);
    }

    @ConditionalOnMissingBean(name = "basicAuthenticationWebflowConfigurer")
    @Bean
    @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
    public CasWebflowConfigurer basicAuthenticationWebflowConfigurer(
        final CasConfigurationProperties casProperties, final ConfigurableApplicationContext applicationContext,
        @Qualifier(CasWebflowConstants.BEAN_NAME_LOGIN_FLOW_DEFINITION_REGISTRY)
        final FlowDefinitionRegistry loginFlowDefinitionRegistry,
        @Qualifier(CasWebflowConstants.BEAN_NAME_FLOW_BUILDER_SERVICES)
        final FlowBuilderServices flowBuilderServices) {
        return new BasicAuthenticationWebflowConfigurer(flowBuilderServices, loginFlowDefinitionRegistry, applicationContext, casProperties);
    }

    @ConditionalOnMissingBean(name = "basicPrincipalFactory")
    @Bean
    @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
    public PrincipalFactory basicPrincipalFactory() {
        return PrincipalFactoryUtils.newPrincipalFactory();
    }

    @Bean
    @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
    @ConditionalOnMissingBean(name = "basicCasWebflowExecutionPlanConfigurer")
    public CasWebflowExecutionPlanConfigurer basicCasWebflowExecutionPlanConfigurer(
        @Qualifier("basicAuthenticationWebflowConfigurer")
        final CasWebflowConfigurer basicAuthenticationWebflowConfigurer) {
        return plan -> plan.registerWebflowConfigurer(basicAuthenticationWebflowConfigurer);
    }

    @Bean
    @ConditionalOnMissingBean(name = "basicAuthenticationCasMultifactorWebflowCustomizer")
    @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
    public CasMultifactorWebflowCustomizer basicAuthenticationCasMultifactorWebflowCustomizer() {
        return new BasicAuthenticationCasMultifactorWebflowCustomizer();
    }
}
