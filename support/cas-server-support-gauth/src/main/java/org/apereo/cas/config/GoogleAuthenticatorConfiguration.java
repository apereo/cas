package org.apereo.cas.config;

import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.configuration.features.CasFeatureModule;
import org.apereo.cas.gauth.web.flow.GoogleAuthenticatorMultifactorTrustedDeviceWebflowConfigurer;
import org.apereo.cas.gauth.web.flow.GoogleAuthenticatorMultifactorWebflowConfigurer;
import org.apereo.cas.trusted.config.MultifactorAuthnTrustConfiguration;
import org.apereo.cas.util.spring.beans.BeanCondition;
import org.apereo.cas.util.spring.beans.BeanSupplier;
import org.apereo.cas.util.spring.boot.ConditionalOnFeatureEnabled;
import org.apereo.cas.web.flow.CasWebflowConfigurer;
import org.apereo.cas.web.flow.CasWebflowConstants;
import org.apereo.cas.web.flow.CasWebflowExecutionPlanConfigurer;
import org.apereo.cas.web.flow.util.MultifactorAuthenticationWebflowUtils;

import lombok.val;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.DependsOn;
import org.springframework.context.annotation.ScopedProxyMode;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.webflow.config.FlowDefinitionRegistryBuilder;
import org.springframework.webflow.definition.registry.FlowDefinitionRegistry;
import org.springframework.webflow.engine.builder.FlowBuilder;
import org.springframework.webflow.engine.builder.support.FlowBuilderServices;

/**
 * This is {@link GoogleAuthenticatorConfiguration}.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 */
@EnableConfigurationProperties(CasConfigurationProperties.class)
@EnableScheduling
@ConditionalOnFeatureEnabled(feature = CasFeatureModule.FeatureCatalog.GoogleAuthenticator)
@AutoConfiguration
public class GoogleAuthenticatorConfiguration {
    private static final int WEBFLOW_CONFIGURER_ORDER = 100;
    @Configuration(value = "GoogleAuthenticatorMultifactorWebflowConfiguration", proxyBeanMethods = false)
    @EnableConfigurationProperties(CasConfigurationProperties.class)
    public static class GoogleAuthenticatorMultifactorWebflowConfiguration {
        @Bean
        @ConditionalOnMissingBean(name = "googleAuthenticatorFlowRegistry")
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        public FlowDefinitionRegistry googleAuthenticatorFlowRegistry(
            final CasConfigurationProperties casProperties,
            final ConfigurableApplicationContext applicationContext,
            @Qualifier(CasWebflowConstants.BEAN_NAME_FLOW_BUILDER_SERVICES)
            final FlowBuilderServices flowBuilderServices,
            @Qualifier(CasWebflowConstants.BEAN_NAME_FLOW_BUILDER)
            final FlowBuilder flowBuilder) {
            val builder = new FlowDefinitionRegistryBuilder(applicationContext, flowBuilderServices);
            builder.addFlowBuilder(flowBuilder, casProperties.getAuthn().getMfa().getGauth().getId());
            return builder.build();
        }

        @ConditionalOnMissingBean(name = "googleAuthenticatorMultifactorWebflowConfigurer")
        @Bean
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        public CasWebflowConfigurer googleAuthenticatorMultifactorWebflowConfigurer(
            final CasConfigurationProperties casProperties,
            final ConfigurableApplicationContext applicationContext,
            @Qualifier("googleAuthenticatorFlowRegistry")
            final FlowDefinitionRegistry googleAuthenticatorFlowRegistry,
            @Qualifier(CasWebflowConstants.BEAN_NAME_LOGIN_FLOW_DEFINITION_REGISTRY)
            final FlowDefinitionRegistry loginFlowDefinitionRegistry,
            @Qualifier(CasWebflowConstants.BEAN_NAME_FLOW_BUILDER_SERVICES)
            final FlowBuilderServices flowBuilderServices) {
            val cfg = new GoogleAuthenticatorMultifactorWebflowConfigurer(flowBuilderServices,
                loginFlowDefinitionRegistry, googleAuthenticatorFlowRegistry, applicationContext, casProperties,
                MultifactorAuthenticationWebflowUtils.getMultifactorAuthenticationWebflowCustomizers(applicationContext));
            cfg.setOrder(WEBFLOW_CONFIGURER_ORDER);
            return cfg;
        }

        @Bean
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        @ConditionalOnMissingBean(name = "googleCasWebflowExecutionPlanConfigurer")
        public CasWebflowExecutionPlanConfigurer googleCasWebflowExecutionPlanConfigurer(
            @Qualifier("googleAuthenticatorMultifactorWebflowConfigurer")
            final CasWebflowConfigurer googleAuthenticatorMultifactorWebflowConfigurer) {
            return plan -> plan.registerWebflowConfigurer(googleAuthenticatorMultifactorWebflowConfigurer);
        }
    }

    @ConditionalOnClass(MultifactorAuthnTrustConfiguration.class)
    @Configuration(value = "GoogleAuthenticatorMultifactorTrustConfiguration", proxyBeanMethods = false)
    @DependsOn("googleAuthenticatorMultifactorWebflowConfigurer")
    @ConditionalOnFeatureEnabled(feature = CasFeatureModule.FeatureCatalog.MultifactorAuthenticationTrustedDevices, module = "gauth")
    public static class GoogleAuthenticatorMultifactorTrustConfiguration {

        private static final BeanCondition CONDITION = BeanCondition.on("cas.authn.mfa.gauth.trusted-device-enabled")
            .isTrue().evenIfMissing();

        @ConditionalOnMissingBean(name = "gauthMultifactorTrustWebflowConfigurer")
        @Bean
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        public CasWebflowConfigurer gauthMultifactorTrustWebflowConfigurer(
            @Qualifier("googleAuthenticatorFlowRegistry")
            final FlowDefinitionRegistry googleAuthenticatorFlowRegistry,
            final ConfigurableApplicationContext applicationContext,
            final CasConfigurationProperties casProperties,
            @Qualifier(CasWebflowConstants.BEAN_NAME_LOGIN_FLOW_DEFINITION_REGISTRY)
            final FlowDefinitionRegistry loginFlowDefinitionRegistry,
            @Qualifier(CasWebflowConstants.BEAN_NAME_FLOW_BUILDER_SERVICES)
            final FlowBuilderServices flowBuilderServices) {
            return BeanSupplier.of(CasWebflowConfigurer.class)
                .when(CONDITION.given(applicationContext.getEnvironment()))
                .supply(() -> {
                    val cfg = new GoogleAuthenticatorMultifactorTrustedDeviceWebflowConfigurer(flowBuilderServices,
                        loginFlowDefinitionRegistry, googleAuthenticatorFlowRegistry, applicationContext, casProperties,
                        MultifactorAuthenticationWebflowUtils.getMultifactorAuthenticationWebflowCustomizers(applicationContext));
                    cfg.setOrder(WEBFLOW_CONFIGURER_ORDER + 1);
                    return cfg;
                })
                .otherwiseProxy()
                .get();
        }

        @Bean
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        public CasWebflowExecutionPlanConfigurer gauthMultifactorTrustCasWebflowExecutionPlanConfigurer(
            final ConfigurableApplicationContext applicationContext,
            @Qualifier("gauthMultifactorTrustWebflowConfigurer")
            final CasWebflowConfigurer gauthMultifactorTrustWebflowConfigurer) {
            return BeanSupplier.of(CasWebflowExecutionPlanConfigurer.class)
                .when(CONDITION.given(applicationContext.getEnvironment()))
                .supply(() -> plan -> plan.registerWebflowConfigurer(gauthMultifactorTrustWebflowConfigurer))
                .otherwiseProxy()
                .get();
        }
    }
}
