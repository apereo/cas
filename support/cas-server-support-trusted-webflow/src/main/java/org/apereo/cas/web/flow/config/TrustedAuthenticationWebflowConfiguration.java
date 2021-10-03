package org.apereo.cas.web.flow.config;

import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.web.flow.CasWebflowConfigurer;
import org.apereo.cas.web.flow.CasWebflowConstants;
import org.apereo.cas.web.flow.CasWebflowExecutionPlanConfigurer;
import org.apereo.cas.web.flow.TrustedAuthenticationWebflowConfigurer;

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
 * This is {@link TrustedAuthenticationWebflowConfiguration}.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 */
@Configuration(value = "trustedAuthenticationWebflowConfiguration", proxyBeanMethods = false)
@EnableConfigurationProperties(CasConfigurationProperties.class)
public class TrustedAuthenticationWebflowConfiguration {


    @Configuration(value = "TrustedAuthenticationWebflowBaseConfiguration", proxyBeanMethods = false)
    @EnableConfigurationProperties(CasConfigurationProperties.class)
    public static class TrustedAuthenticationWebflowBaseConfiguration {
        @ConditionalOnMissingBean(name = "trustedWebflowConfigurer")
        @Bean
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        @Autowired
        public CasWebflowConfigurer trustedWebflowConfigurer(
            final ConfigurableApplicationContext applicationContext,
            final CasConfigurationProperties casProperties,
            @Qualifier(CasWebflowConstants.BEAN_NAME_LOGIN_FLOW_DEFINITION_REGISTRY)
            final FlowDefinitionRegistry loginFlowRegistry,
            @Qualifier(CasWebflowConstants.BEAN_NAME_FLOW_BUILDER_SERVICES)
            final FlowBuilderServices flowBuilderServices) {
            return new TrustedAuthenticationWebflowConfigurer(flowBuilderServices,
                loginFlowRegistry,
                applicationContext, casProperties);
        }

    }

    @Configuration(value = "TrustedAuthenticationWebflowPlanConfiguration", proxyBeanMethods = false)
    @EnableConfigurationProperties(CasConfigurationProperties.class)
    public static class TrustedAuthenticationWebflowPlanConfiguration {
        @Bean
        @Autowired
        @ConditionalOnMissingBean(name = "trustedCasWebflowExecutionPlanConfigurer")
        public CasWebflowExecutionPlanConfigurer trustedCasWebflowExecutionPlanConfigurer(
            @Qualifier("trustedWebflowConfigurer")
            final CasWebflowConfigurer trustedWebflowConfigurer) {
            return plan -> plan.registerWebflowConfigurer(trustedWebflowConfigurer);
        }
    }
}
