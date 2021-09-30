package org.apereo.cas.web.flow.config;

import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.web.flow.CasWebflowConfigurer;
import org.apereo.cas.web.flow.CasWebflowConstants;
import org.apereo.cas.web.flow.CasWebflowExecutionPlanConfigurer;
import org.apereo.cas.web.flow.SpnegoCasMultifactorWebflowCustomizer;
import org.apereo.cas.web.flow.SpnegoWebflowConfigurer;
import org.apereo.cas.web.flow.configurer.CasMultifactorWebflowCustomizer;

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

/**
 * This is {@link SpnegoWebflowConfiguration}.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 */
@Configuration(value = "spnegoWebflowConfiguration", proxyBeanMethods = false)
@EnableConfigurationProperties(CasConfigurationProperties.class)
public class SpnegoWebflowConfiguration {
    @ConditionalOnMissingBean(name = "spnegoWebflowConfigurer")
    @Bean
    @Autowired
    public CasWebflowConfigurer spnegoWebflowConfigurer(
        final ConfigurableApplicationContext applicationContext,
        final CasConfigurationProperties casProperties,
        @Qualifier(CasWebflowConstants.BEAN_NAME_LOGIN_FLOW_DEFINITION_REGISTRY)
        final FlowDefinitionRegistry loginFlowDefinitionRegistry,
        @Qualifier(CasWebflowConstants.BEAN_NAME_FLOW_BUILDER_SERVICES)
        final FlowBuilderServices flowBuilderServices) {
        return new SpnegoWebflowConfigurer(flowBuilderServices,
            loginFlowDefinitionRegistry,
            applicationContext, casProperties);
    }

    @Bean
    @ConditionalOnMissingBean(name = "spnegoCasMultifactorWebflowCustomizer")
    @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
    public CasMultifactorWebflowCustomizer spnegoCasMultifactorWebflowCustomizer() {
        return new SpnegoCasMultifactorWebflowCustomizer();
    }

    @Bean
    @ConditionalOnMissingBean(name = "spnegoCasWebflowExecutionPlanConfigurer")
    @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
    @Autowired
    public CasWebflowExecutionPlanConfigurer spnegoCasWebflowExecutionPlanConfigurer(
        @Qualifier("spnegoWebflowConfigurer")
        final CasWebflowConfigurer spnegoWebflowConfigurer) {
        return plan -> plan.registerWebflowConfigurer(spnegoWebflowConfigurer);
    }
}
