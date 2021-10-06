package org.apereo.cas.config;

import org.apereo.cas.adaptors.radius.web.flow.RadiusAuthenticationWebflowAction;
import org.apereo.cas.adaptors.radius.web.flow.RadiusAuthenticationWebflowEventResolver;
import org.apereo.cas.adaptors.radius.web.flow.RadiusMultifactorTrustedDeviceWebflowConfigurer;
import org.apereo.cas.adaptors.radius.web.flow.RadiusMultifactorWebflowConfigurer;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.trusted.config.ConditionalOnMultifactorTrustedDevicesEnabled;
import org.apereo.cas.trusted.config.MultifactorAuthnTrustConfiguration;
import org.apereo.cas.web.flow.CasWebflowConfigurer;
import org.apereo.cas.web.flow.CasWebflowConstants;
import org.apereo.cas.web.flow.CasWebflowExecutionPlanConfigurer;
import org.apereo.cas.web.flow.resolver.CasWebflowEventResolver;
import org.apereo.cas.web.flow.resolver.impl.CasWebflowEventResolutionConfigurationContext;
import org.apereo.cas.web.flow.util.MultifactorAuthenticationWebflowUtils;

import lombok.val;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.DependsOn;
import org.springframework.context.annotation.ScopedProxyMode;
import org.springframework.webflow.config.FlowDefinitionRegistryBuilder;
import org.springframework.webflow.definition.registry.FlowDefinitionRegistry;
import org.springframework.webflow.engine.builder.FlowBuilder;
import org.springframework.webflow.engine.builder.support.FlowBuilderServices;
import org.springframework.webflow.execution.Action;

/**
 * This is {@link RadiusMultifactorConfiguration}.
 *
 * @author Misagh Moayyed
 * @author Nagai Takayuki
 * @since 5.0.0
 */
@EnableConfigurationProperties(CasConfigurationProperties.class)
@ConditionalOnProperty(name = "cas.authn.mfa.radius.client.inet-address")
@Configuration(value = "radiusMfaConfiguration", proxyBeanMethods = false)
public class RadiusMultifactorConfiguration {

    private static final int WEBFLOW_CONFIGURER_ORDER = 100;

    @Configuration(value = "RadiusMultifactorRegistryConfiguration", proxyBeanMethods = false)
    @EnableConfigurationProperties(CasConfigurationProperties.class)
    public static class RadiusMultifactorRegistryConfiguration {
        @Bean
        @ConditionalOnMissingBean(name = "radiusFlowRegistry")
        @Autowired
        public FlowDefinitionRegistry radiusFlowRegistry(
            final ConfigurableApplicationContext applicationContext,
            @Qualifier(CasWebflowConstants.BEAN_NAME_FLOW_BUILDER_SERVICES)
            final FlowBuilderServices flowBuilderServices,
            @Qualifier(CasWebflowConstants.BEAN_NAME_FLOW_BUILDER)
            final FlowBuilder flowBuilder) {
            val builder = new FlowDefinitionRegistryBuilder(applicationContext, flowBuilderServices);
            builder.addFlowBuilder(flowBuilder, RadiusMultifactorWebflowConfigurer.MFA_RADIUS_EVENT_ID);
            return builder.build();
        }

    }

    @Configuration(value = "RadiusMultifactorWebflowActionConfiguration", proxyBeanMethods = false)
    @EnableConfigurationProperties(CasConfigurationProperties.class)
    public static class RadiusMultifactorWebflowActionConfiguration {
        @Bean
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        @ConditionalOnMissingBean(name = "radiusAuthenticationWebflowAction")
        public Action radiusAuthenticationWebflowAction(
            @Qualifier("radiusAuthenticationWebflowEventResolver")
            final CasWebflowEventResolver radiusAuthenticationWebflowEventResolver) {
            return new RadiusAuthenticationWebflowAction(radiusAuthenticationWebflowEventResolver);
        }

    }

    @Configuration(value = "RadiusMultifactorWebflowEventConfiguration", proxyBeanMethods = false)
    @EnableConfigurationProperties(CasConfigurationProperties.class)
    public static class RadiusMultifactorWebflowEventConfiguration {
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        @Bean
        @ConditionalOnMissingBean(name = "radiusAuthenticationWebflowEventResolver")
        @Autowired
        public CasWebflowEventResolver radiusAuthenticationWebflowEventResolver(
            final CasConfigurationProperties casProperties,
            @Qualifier("casWebflowConfigurationContext")
            final CasWebflowEventResolutionConfigurationContext casWebflowConfigurationContext) {
            return new RadiusAuthenticationWebflowEventResolver(casWebflowConfigurationContext,
                casProperties.getAuthn().getMfa().getRadius().getAllowedAuthenticationAttempts());
        }
    }

    @Configuration(value = "RadiusMultifactorWebflowConfiguration", proxyBeanMethods = false)
    @EnableConfigurationProperties(CasConfigurationProperties.class)
    public static class RadiusMultifactorWebflowConfiguration {
        @ConditionalOnMissingBean(name = "radiusMultifactorWebflowConfigurer")
        @Bean
        @Autowired
        public CasWebflowConfigurer radiusMultifactorWebflowConfigurer(
            final CasConfigurationProperties casProperties,
            final ConfigurableApplicationContext applicationContext,
            @Qualifier("radiusFlowRegistry")
            final FlowDefinitionRegistry radiusFlowRegistry,
            @Qualifier(CasWebflowConstants.BEAN_NAME_LOGIN_FLOW_DEFINITION_REGISTRY)
            final FlowDefinitionRegistry loginFlowDefinitionRegistry,
            @Qualifier(CasWebflowConstants.BEAN_NAME_FLOW_BUILDER_SERVICES)
            final FlowBuilderServices flowBuilderServices) {
            val cfg = new RadiusMultifactorWebflowConfigurer(flowBuilderServices,
                loginFlowDefinitionRegistry, radiusFlowRegistry, applicationContext, casProperties,
                MultifactorAuthenticationWebflowUtils.getMultifactorAuthenticationWebflowCustomizers(applicationContext));
            cfg.setOrder(WEBFLOW_CONFIGURER_ORDER);
            return cfg;
        }

    }

    @Configuration(value = "RadiusMultifactorWebflowPlanConfiguration", proxyBeanMethods = false)
    @EnableConfigurationProperties(CasConfigurationProperties.class)
    public static class RadiusMultifactorWebflowPlanConfiguration {
        @Bean
        @ConditionalOnMissingBean(name = "radiusMultifactorCasWebflowExecutionPlanConfigurer")
        @Autowired
        public CasWebflowExecutionPlanConfigurer radiusMultifactorCasWebflowExecutionPlanConfigurer(
            @Qualifier("radiusMultifactorWebflowConfigurer")
            final CasWebflowConfigurer radiusMultifactorWebflowConfigurer) {
            return plan -> plan.registerWebflowConfigurer(radiusMultifactorWebflowConfigurer);
        }

    }

    @ConditionalOnClass(value = MultifactorAuthnTrustConfiguration.class)
    @ConditionalOnMultifactorTrustedDevicesEnabled(prefix = "cas.authn.mfa.radius")
    @Configuration(value = "radiusMultifactorTrustConfiguration", proxyBeanMethods = false)
    @DependsOn("radiusMultifactorWebflowConfigurer")
    public static class RadiusMultifactorTrustConfiguration {

        @ConditionalOnMissingBean(name = "radiusMultifactorTrustConfigurer")
        @Bean
        @Autowired
        public CasWebflowConfigurer radiusMultifactorTrustConfigurer(
            final CasConfigurationProperties casProperties,
            final ConfigurableApplicationContext applicationContext,
            @Qualifier("radiusFlowRegistry")
            final FlowDefinitionRegistry radiusFlowRegistry,
            @Qualifier(CasWebflowConstants.BEAN_NAME_LOGIN_FLOW_DEFINITION_REGISTRY)
            final FlowDefinitionRegistry loginFlowDefinitionRegistry,
            @Qualifier(CasWebflowConstants.BEAN_NAME_FLOW_BUILDER_SERVICES)
            final FlowBuilderServices flowBuilderServices) {
            val cfg = new RadiusMultifactorTrustedDeviceWebflowConfigurer(flowBuilderServices,
                loginFlowDefinitionRegistry, radiusFlowRegistry,
                applicationContext, casProperties,
                MultifactorAuthenticationWebflowUtils.getMultifactorAuthenticationWebflowCustomizers(applicationContext));
            cfg.setOrder(WEBFLOW_CONFIGURER_ORDER + 1);
            return cfg;
        }

        @Bean
        @Autowired
        public CasWebflowExecutionPlanConfigurer radiusMultifactorTrustCasWebflowExecutionPlanConfigurer(
            @Qualifier("radiusMultifactorTrustConfigurer")
            final CasWebflowConfigurer radiusMultifactorTrustConfigurer) {
            return plan -> plan.registerWebflowConfigurer(radiusMultifactorTrustConfigurer);
        }
    }
}
