package org.apereo.cas.adaptors.authy.config;

import org.apereo.cas.adaptors.authy.web.flow.AuthyAuthenticationWebflowAction;
import org.apereo.cas.adaptors.authy.web.flow.AuthyAuthenticationWebflowEventResolver;
import org.apereo.cas.adaptors.authy.web.flow.AuthyMultifactorTrustedDeviceWebflowConfigurer;
import org.apereo.cas.adaptors.authy.web.flow.AuthyMultifactorWebflowConfigurer;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.configuration.support.CasFeatureModule;
import org.apereo.cas.trusted.config.ConditionalOnMultifactorTrustedDevicesEnabled;
import org.apereo.cas.trusted.config.MultifactorAuthnTrustConfiguration;
import org.apereo.cas.util.spring.beans.BeanCondition;
import org.apereo.cas.util.spring.beans.BeanSupplier;
import org.apereo.cas.util.spring.boot.ConditionalOnFeature;
import org.apereo.cas.web.flow.CasWebflowConfigurer;
import org.apereo.cas.web.flow.CasWebflowConstants;
import org.apereo.cas.web.flow.CasWebflowExecutionPlanConfigurer;
import org.apereo.cas.web.flow.resolver.CasWebflowEventResolver;
import org.apereo.cas.web.flow.resolver.impl.CasWebflowEventResolutionConfigurationContext;
import org.apereo.cas.web.flow.util.MultifactorAuthenticationWebflowUtils;

import lombok.val;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
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
 * This is {@link AuthyConfiguration}.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 */
@Configuration(value = "AuthyConfiguration", proxyBeanMethods = false)
@EnableConfigurationProperties(CasConfigurationProperties.class)
@ConditionalOnFeature(feature = CasFeatureModule.FeatureCatalog.Authy)
public class AuthyConfiguration {
    private static final int WEBFLOW_CONFIGURER_ORDER = 100;

    private static final BeanCondition CONDITION = BeanCondition.on("cas.authn.mfa.authy.api-key");

    @Configuration(value = "AuthyWebflowRegistryConfiguration", proxyBeanMethods = false)
    @EnableConfigurationProperties(CasConfigurationProperties.class)
    public static class AuthyWebflowRegistryConfiguration {
        @Bean
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        @ConditionalOnMissingBean(name = "authyAuthenticatorFlowRegistry")
        public FlowDefinitionRegistry authyAuthenticatorFlowRegistry(
            final ConfigurableApplicationContext applicationContext,
            @Qualifier(CasWebflowConstants.BEAN_NAME_FLOW_BUILDER_SERVICES)
            final FlowBuilderServices flowBuilderServices,
            @Qualifier(CasWebflowConstants.BEAN_NAME_FLOW_BUILDER)
            final FlowBuilder flowBuilder) throws Exception {
            return BeanSupplier.of(FlowDefinitionRegistry.class)
                .when(CONDITION.given(applicationContext.getEnvironment()))
                .supply(() -> {
                    val builder = new FlowDefinitionRegistryBuilder(applicationContext, flowBuilderServices);
                    builder.addFlowBuilder(flowBuilder, AuthyMultifactorWebflowConfigurer.MFA_AUTHY_EVENT_ID);
                    return builder.build();
                })
                .otherwiseProxy()
                .get();
        }

    }

    @Configuration(value = "AuthyWebflowCoreConfiguration", proxyBeanMethods = false)
    @EnableConfigurationProperties(CasConfigurationProperties.class)
    public static class AuthyWebflowCoreConfiguration {
        @ConditionalOnMissingBean(name = "authyMultifactorWebflowConfigurer")
        @Bean
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        public CasWebflowConfigurer authyMultifactorWebflowConfigurer(
            @Qualifier("authyAuthenticatorFlowRegistry")
            final FlowDefinitionRegistry authyAuthenticatorFlowRegistry,
            final ConfigurableApplicationContext applicationContext,
            final CasConfigurationProperties casProperties,
            @Qualifier(CasWebflowConstants.BEAN_NAME_LOGIN_FLOW_DEFINITION_REGISTRY)
            final FlowDefinitionRegistry loginFlowDefinitionRegistry,
            @Qualifier(CasWebflowConstants.BEAN_NAME_FLOW_BUILDER_SERVICES)
            final FlowBuilderServices flowBuilderServices) throws Exception {
            return BeanSupplier.of(CasWebflowConfigurer.class)
                .when(CONDITION.given(applicationContext.getEnvironment()))
                .supply(() -> {
                    val cfg = new AuthyMultifactorWebflowConfigurer(flowBuilderServices,
                        loginFlowDefinitionRegistry, authyAuthenticatorFlowRegistry,
                        applicationContext, casProperties,
                        MultifactorAuthenticationWebflowUtils.getMultifactorAuthenticationWebflowCustomizers(applicationContext));
                    cfg.setOrder(WEBFLOW_CONFIGURER_ORDER);
                    return cfg;
                })
                .otherwiseProxy()
                .get();
        }
    }

    @Configuration(value = "AuthyWebflowEventConfiguration", proxyBeanMethods = false)
    @EnableConfigurationProperties(CasConfigurationProperties.class)
    public static class AuthyWebflowEventConfiguration {
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        @Bean
        public CasWebflowEventResolver authyAuthenticationWebflowEventResolver(
            final ConfigurableApplicationContext applicationContext,
            @Qualifier("casWebflowConfigurationContext")
            final CasWebflowEventResolutionConfigurationContext casWebflowConfigurationContext) throws Exception {
            return BeanSupplier.of(CasWebflowEventResolver.class)
                .when(CONDITION.given(applicationContext.getEnvironment()))
                .supply(() -> new AuthyAuthenticationWebflowEventResolver(casWebflowConfigurationContext))
                .otherwiseProxy()
                .get();
        }

    }

    @Configuration(value = "AuthyWebflowActionConfiguration", proxyBeanMethods = false)
    @EnableConfigurationProperties(CasConfigurationProperties.class)
    public static class AuthyWebflowActionConfiguration {
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        @Bean
        public Action authyAuthenticationWebflowAction(
            final ConfigurableApplicationContext applicationContext,
            @Qualifier("authyAuthenticationWebflowEventResolver")
            final CasWebflowEventResolver authyAuthenticationWebflowEventResolver) throws Exception {
            return BeanSupplier.of(Action.class)
                .when(CONDITION.given(applicationContext.getEnvironment()))
                .supply(() -> new AuthyAuthenticationWebflowAction(authyAuthenticationWebflowEventResolver))
                .otherwiseProxy()
                .get();
        }
    }

    @Configuration(value = "AuthyWebflowExecutionPlanConfiguration", proxyBeanMethods = false)
    @EnableConfigurationProperties(CasConfigurationProperties.class)
    public static class AuthyWebflowExecutionPlanConfiguration {

        @Bean
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        @ConditionalOnMissingBean(name = "authyCasWebflowExecutionPlanConfigurer")
        public CasWebflowExecutionPlanConfigurer authyCasWebflowExecutionPlanConfigurer(
            final ConfigurableApplicationContext applicationContext,
            @Qualifier("authyMultifactorWebflowConfigurer")
            final CasWebflowConfigurer authyMultifactorWebflowConfigurer) throws Exception {
            return BeanSupplier.of(CasWebflowExecutionPlanConfigurer.class)
                .when(CONDITION.given(applicationContext.getEnvironment()))
                .supply(() -> plan -> plan.registerWebflowConfigurer(authyMultifactorWebflowConfigurer))
                .otherwiseProxy()
                .get();
        }
    }

    @ConditionalOnClass(value = MultifactorAuthnTrustConfiguration.class)
    @ConditionalOnMultifactorTrustedDevicesEnabled(prefix = "cas.authn.mfa.authy")
    @Configuration(value = "AuthyMultifactorTrustConfiguration", proxyBeanMethods = false)
    @DependsOn("authyMultifactorWebflowConfigurer")
    public static class AuthyMultifactorTrustConfiguration {

        @ConditionalOnMissingBean(name = "authyMultifactorTrustWebflowConfigurer")
        @Bean
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        public CasWebflowConfigurer authyMultifactorTrustWebflowConfigurer(
            @Qualifier("authyAuthenticatorFlowRegistry")
            final FlowDefinitionRegistry authyAuthenticatorFlowRegistry,
            final ConfigurableApplicationContext applicationContext,
            final CasConfigurationProperties casProperties,
            @Qualifier(CasWebflowConstants.BEAN_NAME_LOGIN_FLOW_DEFINITION_REGISTRY)
            final FlowDefinitionRegistry loginFlowDefinitionRegistry,
            @Qualifier(CasWebflowConstants.BEAN_NAME_FLOW_BUILDER_SERVICES)
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
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        @ConditionalOnMissingBean(name = "authyMultifactorTrustCasWebflowExecutionPlanConfigurer")
        public CasWebflowExecutionPlanConfigurer authyMultifactorTrustCasWebflowExecutionPlanConfigurer(
            @Qualifier("authyMultifactorTrustWebflowConfigurer")
            final CasWebflowConfigurer authyMultifactorTrustWebflowConfigurer) {
            return plan -> plan.registerWebflowConfigurer(authyMultifactorTrustWebflowConfigurer);
        }
    }
}
