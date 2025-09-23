package org.apereo.cas.config;

import org.apereo.cas.adaptors.radius.web.flow.RadiusAuthenticationWebflowAction;
import org.apereo.cas.adaptors.radius.web.flow.RadiusAuthenticationWebflowEventResolver;
import org.apereo.cas.adaptors.radius.web.flow.RadiusMultifactorWebflowConfigurer;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.configuration.features.CasFeatureModule;
import org.apereo.cas.util.spring.beans.BeanCondition;
import org.apereo.cas.util.spring.beans.BeanSupplier;
import org.apereo.cas.util.spring.boot.ConditionalOnFeatureEnabled;
import org.apereo.cas.web.flow.CasWebflowConfigurer;
import org.apereo.cas.web.flow.CasWebflowConstants;
import org.apereo.cas.web.flow.CasWebflowExecutionPlanConfigurer;
import org.apereo.cas.web.flow.actions.ConsumerExecutionAction;
import org.apereo.cas.web.flow.resolver.CasWebflowEventResolver;
import org.apereo.cas.web.flow.resolver.impl.CasWebflowEventResolutionConfigurationContext;
import org.apereo.cas.web.flow.util.MultifactorAuthenticationWebflowUtils;
import lombok.val;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
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
@ConditionalOnFeatureEnabled(feature = CasFeatureModule.FeatureCatalog.RadiusMFA)
@Configuration(value = "RadiusMultifactorConfiguration", proxyBeanMethods = false)
class RadiusMultifactorConfiguration {
    private static final BeanCondition CONDITION = BeanCondition.on("cas.authn.mfa.radius.client.inet-address");

    private static final int WEBFLOW_CONFIGURER_ORDER = 100;

    @Configuration(value = "RadiusMultifactorRegistryConfiguration", proxyBeanMethods = false)
    @EnableConfigurationProperties(CasConfigurationProperties.class)
    static class RadiusMultifactorRegistryConfiguration {
        @Bean
        @ConditionalOnMissingBean(name = "radiusFlowRegistry")
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        public FlowDefinitionRegistry radiusFlowRegistry(
            final ConfigurableApplicationContext applicationContext,
            @Qualifier(CasWebflowConstants.BEAN_NAME_FLOW_BUILDER_SERVICES)
            final FlowBuilderServices flowBuilderServices,
            @Qualifier(CasWebflowConstants.BEAN_NAME_FLOW_BUILDER)
            final FlowBuilder flowBuilder) {
            return BeanSupplier.of(FlowDefinitionRegistry.class)
                .when(CONDITION.given(applicationContext.getEnvironment()))
                .supply(() -> {
                    val builder = new FlowDefinitionRegistryBuilder(applicationContext, flowBuilderServices);
                    builder.addFlowBuilder(flowBuilder, RadiusMultifactorWebflowConfigurer.MFA_RADIUS_EVENT_ID);
                    return builder.build();
                })
                .otherwiseProxy()
                .get();
        }

    }

    @Configuration(value = "RadiusMultifactorWebflowActionConfiguration", proxyBeanMethods = false)
    @EnableConfigurationProperties(CasConfigurationProperties.class)
    static class RadiusMultifactorWebflowActionConfiguration {
        @Bean
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        @ConditionalOnMissingBean(name = CasWebflowConstants.ACTION_ID_RADIUS_AUTHENTICATION)
        public Action radiusAuthenticationWebflowAction(
            final ConfigurableApplicationContext applicationContext,
            @Qualifier("radiusAuthenticationWebflowEventResolver")
            final CasWebflowEventResolver radiusAuthenticationWebflowEventResolver) {
            return BeanSupplier.of(Action.class)
                .when(CONDITION.given(applicationContext.getEnvironment()))
                .supply(() -> new RadiusAuthenticationWebflowAction(radiusAuthenticationWebflowEventResolver))
                .otherwise(() -> ConsumerExecutionAction.NONE)
                .get();
        }

    }

    @Configuration(value = "RadiusMultifactorWebflowEventConfiguration", proxyBeanMethods = false)
    @EnableConfigurationProperties(CasConfigurationProperties.class)
    static class RadiusMultifactorWebflowEventConfiguration {
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        @Bean
        @ConditionalOnMissingBean(name = "radiusAuthenticationWebflowEventResolver")
        public CasWebflowEventResolver radiusAuthenticationWebflowEventResolver(
            final ConfigurableApplicationContext applicationContext,
            final CasConfigurationProperties casProperties,
            @Qualifier(CasWebflowEventResolutionConfigurationContext.BEAN_NAME)
            final CasWebflowEventResolutionConfigurationContext casWebflowConfigurationContext) {
            return BeanSupplier.of(CasWebflowEventResolver.class)
                .when(CONDITION.given(applicationContext.getEnvironment()))
                .supply(() -> new RadiusAuthenticationWebflowEventResolver(casWebflowConfigurationContext,
                    casProperties.getAuthn().getMfa().getRadius().getAllowedAuthenticationAttempts()))
                .otherwiseProxy()
                .get();
        }
    }

    @Configuration(value = "RadiusMultifactorWebflowConfiguration", proxyBeanMethods = false)
    @EnableConfigurationProperties(CasConfigurationProperties.class)
    static class RadiusMultifactorWebflowConfiguration {
        @ConditionalOnMissingBean(name = "radiusMultifactorWebflowConfigurer")
        @Bean
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        public CasWebflowConfigurer radiusMultifactorWebflowConfigurer(
            final CasConfigurationProperties casProperties,
            final ConfigurableApplicationContext applicationContext,
            @Qualifier("radiusFlowRegistry")
            final FlowDefinitionRegistry radiusFlowRegistry,
            @Qualifier(CasWebflowConstants.BEAN_NAME_FLOW_DEFINITION_REGISTRY)
            final FlowDefinitionRegistry flowDefinitionRegistry,
            @Qualifier(CasWebflowConstants.BEAN_NAME_FLOW_BUILDER_SERVICES)
            final FlowBuilderServices flowBuilderServices) {
            return BeanSupplier.of(CasWebflowConfigurer.class)
                .when(CONDITION.given(applicationContext.getEnvironment()))
                .supply(() -> {
                    val cfg = new RadiusMultifactorWebflowConfigurer(flowBuilderServices,
                        flowDefinitionRegistry, radiusFlowRegistry, applicationContext, casProperties,
                        MultifactorAuthenticationWebflowUtils.getMultifactorAuthenticationWebflowCustomizers(applicationContext));
                    cfg.setOrder(WEBFLOW_CONFIGURER_ORDER);
                    return cfg;
                })
                .otherwiseProxy()
                .get();
        }

    }

    @Configuration(value = "RadiusMultifactorWebflowPlanConfiguration", proxyBeanMethods = false)
    @EnableConfigurationProperties(CasConfigurationProperties.class)
    static class RadiusMultifactorWebflowPlanConfiguration {
        @Bean
        @ConditionalOnMissingBean(name = "radiusMultifactorCasWebflowExecutionPlanConfigurer")
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        public CasWebflowExecutionPlanConfigurer radiusMultifactorCasWebflowExecutionPlanConfigurer(
            final ConfigurableApplicationContext applicationContext,
            @Qualifier("radiusMultifactorWebflowConfigurer")
            final CasWebflowConfigurer radiusMultifactorWebflowConfigurer) {
            return BeanSupplier.of(CasWebflowExecutionPlanConfigurer.class)
                .when(CONDITION.given(applicationContext.getEnvironment()))
                .supply(() -> plan -> plan.registerWebflowConfigurer(radiusMultifactorWebflowConfigurer))
                .otherwiseProxy()
                .get();
        }

    }
}
