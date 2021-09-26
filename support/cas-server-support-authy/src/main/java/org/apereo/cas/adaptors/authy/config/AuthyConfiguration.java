package org.apereo.cas.adaptors.authy.config;

import org.apereo.cas.adaptors.authy.web.flow.AuthyAuthenticationWebflowAction;
import org.apereo.cas.adaptors.authy.web.flow.AuthyAuthenticationWebflowEventResolver;
import org.apereo.cas.adaptors.authy.web.flow.AuthyMultifactorTrustedDeviceWebflowConfigurer;
import org.apereo.cas.adaptors.authy.web.flow.AuthyMultifactorWebflowConfigurer;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.trusted.config.ConditionalOnMultifactorTrustedDevicesEnabled;
import org.apereo.cas.trusted.config.MultifactorAuthnTrustConfiguration;
import org.apereo.cas.web.flow.CasWebflowConfigurer;
import org.apereo.cas.web.flow.CasWebflowExecutionPlanConfigurer;
import org.apereo.cas.web.flow.resolver.CasWebflowEventResolver;
import org.apereo.cas.web.flow.resolver.impl.CasWebflowEventResolutionConfigurationContext;
import org.apereo.cas.web.flow.util.MultifactorAuthenticationWebflowUtils;

import lombok.val;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.webflow.config.FlowDefinitionRegistryBuilder;
import org.springframework.webflow.definition.registry.FlowDefinitionRegistry;
import org.springframework.webflow.engine.builder.FlowBuilder;
import org.springframework.webflow.engine.builder.support.FlowBuilderServices;
import org.springframework.webflow.execution.Action;

/**
 * This is {@link AuthyConfiguration}.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 */
@Configuration(value = "authyConfiguration", proxyBeanMethods = false)
@EnableConfigurationProperties(CasConfigurationProperties.class)
public class AuthyConfiguration {
    private static final int WEBFLOW_CONFIGURER_ORDER = 100;

    @Bean
    @Autowired
    @ConditionalOnMissingBean(name = "authyAuthenticatorFlowRegistry")
    public FlowDefinitionRegistry authyAuthenticatorFlowRegistry(
        final ConfigurableApplicationContext applicationContext,
        @Qualifier("flowBuilderServices")
        final FlowBuilderServices flowBuilderServices,
        @Qualifier("flowBuilder")
        final FlowBuilder flowBuilder) {
        val builder = new FlowDefinitionRegistryBuilder(applicationContext, flowBuilderServices);
        builder.addFlowBuilder(flowBuilder, AuthyMultifactorWebflowConfigurer.MFA_AUTHY_EVENT_ID);
        return builder.build();
    }

    @RefreshScope
    @Bean
    @Autowired
    public CasWebflowEventResolver authyAuthenticationWebflowEventResolver(
        @Qualifier("casWebflowConfigurationContext")
        final CasWebflowEventResolutionConfigurationContext casWebflowConfigurationContext) {
        return new AuthyAuthenticationWebflowEventResolver(casWebflowConfigurationContext);
    }

    @ConditionalOnMissingBean(name = "authyMultifactorWebflowConfigurer")
    @Bean
    @Autowired
    public CasWebflowConfigurer authyMultifactorWebflowConfigurer(
        @Qualifier("authyAuthenticatorFlowRegistry")
        final FlowDefinitionRegistry authyAuthenticatorFlowRegistry,
        final ConfigurableApplicationContext applicationContext,
        final CasConfigurationProperties casProperties,
        @Qualifier("loginFlowRegistry")
        final FlowDefinitionRegistry loginFlowDefinitionRegistry,
        @Qualifier("flowBuilderServices")
        final FlowBuilderServices flowBuilderServices) {
        val cfg = new AuthyMultifactorWebflowConfigurer(flowBuilderServices,
            loginFlowDefinitionRegistry, authyAuthenticatorFlowRegistry,
            applicationContext, casProperties,
            MultifactorAuthenticationWebflowUtils.getMultifactorAuthenticationWebflowCustomizers(applicationContext));
        cfg.setOrder(WEBFLOW_CONFIGURER_ORDER);
        return cfg;
    }

    @RefreshScope
    @Bean
    @Autowired
    public Action authyAuthenticationWebflowAction(
        @Qualifier("authyAuthenticationWebflowEventResolver")
        final CasWebflowEventResolver authyAuthenticationWebflowEventResolver) {
        return new AuthyAuthenticationWebflowAction(authyAuthenticationWebflowEventResolver);
    }

    @Bean
    @ConditionalOnMissingBean(name = "authyCasWebflowExecutionPlanConfigurer")
    @Autowired
    public CasWebflowExecutionPlanConfigurer authyCasWebflowExecutionPlanConfigurer(
        @Qualifier("authyMultifactorWebflowConfigurer")
        final CasWebflowConfigurer authyMultifactorWebflowConfigurer) {
        return plan -> plan.registerWebflowConfigurer(authyMultifactorWebflowConfigurer);
    }

    /**
     * The Authy multifactor trust configuration.
     */
    @ConditionalOnClass(value = MultifactorAuthnTrustConfiguration.class)
    @ConditionalOnMultifactorTrustedDevicesEnabled(prefix = "cas.authn.mfa.authy")
    @Configuration("authyMultifactorTrustConfiguration")
    public static class AuthyMultifactorTrustConfiguration {

        @ConditionalOnMissingBean(name = "authyMultifactorTrustWebflowConfigurer")
        @Bean
        @Autowired
        public CasWebflowConfigurer authyMultifactorTrustWebflowConfigurer(
            @Qualifier("authyAuthenticatorFlowRegistry")
            final FlowDefinitionRegistry authyAuthenticatorFlowRegistry,
            final ConfigurableApplicationContext applicationContext,
            final CasConfigurationProperties casProperties,
            @Qualifier("loginFlowRegistry")
            final FlowDefinitionRegistry loginFlowDefinitionRegistry,
            @Qualifier("flowBuilderServices")
            final FlowBuilderServices flowBuilderServices) {
            val cfg = new AuthyMultifactorTrustedDeviceWebflowConfigurer(flowBuilderServices,
                loginFlowDefinitionRegistry,
                authyAuthenticatorFlowRegistry,
                applicationContext,
                casProperties,
                MultifactorAuthenticationWebflowUtils.getMultifactorAuthenticationWebflowCustomizers(applicationContext));
            cfg.setOrder(WEBFLOW_CONFIGURER_ORDER + 1);
            return cfg;
        }

        @Bean
        @ConditionalOnMissingBean(name = "authyMultifactorTrustCasWebflowExecutionPlanConfigurer")
        @Autowired
        public CasWebflowExecutionPlanConfigurer authyMultifactorTrustCasWebflowExecutionPlanConfigurer(
            @Qualifier("authyMultifactorTrustWebflowConfigurer")
            final CasWebflowConfigurer authyMultifactorTrustWebflowConfigurer) {
            return plan -> plan.registerWebflowConfigurer(authyMultifactorTrustWebflowConfigurer);
        }
    }
}
