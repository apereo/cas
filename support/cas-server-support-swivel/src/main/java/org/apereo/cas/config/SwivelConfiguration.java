package org.apereo.cas.config;

import org.apereo.cas.adaptors.swivel.web.flow.SwivelAuthenticationWebflowAction;
import org.apereo.cas.adaptors.swivel.web.flow.SwivelAuthenticationWebflowEventResolver;
import org.apereo.cas.adaptors.swivel.web.flow.SwivelMultifactorTrustedDeviceWebflowConfigurer;
import org.apereo.cas.adaptors.swivel.web.flow.SwivelMultifactorWebflowConfigurer;
import org.apereo.cas.adaptors.swivel.web.flow.rest.SwivelTuringImageGeneratorController;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.configuration.features.CasFeatureModule;
import org.apereo.cas.trusted.config.MultifactorAuthnTrustConfiguration;
import org.apereo.cas.util.spring.beans.BeanCondition;
import org.apereo.cas.util.spring.beans.BeanSupplier;
import org.apereo.cas.util.spring.boot.ConditionalOnFeatureEnabled;
import org.apereo.cas.web.flow.CasWebflowConfigurer;
import org.apereo.cas.web.flow.CasWebflowConstants;
import org.apereo.cas.web.flow.CasWebflowExecutionPlanConfigurer;
import org.apereo.cas.web.flow.resolver.CasWebflowEventResolver;
import org.apereo.cas.web.flow.resolver.impl.CasWebflowEventResolutionConfigurationContext;
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
import org.springframework.webflow.config.FlowDefinitionRegistryBuilder;
import org.springframework.webflow.definition.registry.FlowDefinitionRegistry;
import org.springframework.webflow.engine.builder.FlowBuilder;
import org.springframework.webflow.engine.builder.support.FlowBuilderServices;
import org.springframework.webflow.execution.Action;

/**
 * This is {@link SwivelConfiguration}.
 *
 * @author Misagh Moayyed
 * @since 5.2.0
 * @deprecated Since 6.6.
 */
@EnableConfigurationProperties(CasConfigurationProperties.class)
@ConditionalOnFeatureEnabled(feature = CasFeatureModule.FeatureCatalog.MultifactorAuthentication, module = "swivel")
@AutoConfiguration
@Deprecated(since = "6.6")
public class SwivelConfiguration {
    private static final int WEBFLOW_CONFIGURER_ORDER = 100;

    @Configuration(value = "SwivelWebflowConfiguration", proxyBeanMethods = false)
    @EnableConfigurationProperties(CasConfigurationProperties.class)
    public static class SwivelWebflowConfiguration {
        @ConditionalOnMissingBean(name = "swivelMultifactorWebflowConfigurer")
        @Bean
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        public CasWebflowConfigurer swivelMultifactorWebflowConfigurer(
            @Qualifier("swivelAuthenticatorFlowRegistry")
            final FlowDefinitionRegistry swivelAuthenticatorFlowRegistry,
            final ConfigurableApplicationContext applicationContext,
            final CasConfigurationProperties casProperties,
            @Qualifier(CasWebflowConstants.BEAN_NAME_LOGIN_FLOW_DEFINITION_REGISTRY)
            final FlowDefinitionRegistry loginFlowDefinitionRegistry,
            @Qualifier(CasWebflowConstants.BEAN_NAME_FLOW_BUILDER_SERVICES)
            final FlowBuilderServices flowBuilderServices) {
            val cfg = new SwivelMultifactorWebflowConfigurer(flowBuilderServices,
                loginFlowDefinitionRegistry,
                swivelAuthenticatorFlowRegistry, applicationContext, casProperties,
                MultifactorAuthenticationWebflowUtils.getMultifactorAuthenticationWebflowCustomizers(applicationContext));
            cfg.setOrder(WEBFLOW_CONFIGURER_ORDER);
            return cfg;
        }

    }

    @Configuration(value = "SwivelWebflowEventsConfiguration", proxyBeanMethods = false)
    @EnableConfigurationProperties(CasConfigurationProperties.class)
    public static class SwivelWebflowEventsConfiguration {
        @Bean
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        @ConditionalOnMissingBean(name = "swivelAuthenticationWebflowEventResolver")
        public CasWebflowEventResolver swivelAuthenticationWebflowEventResolver(
            @Qualifier("casWebflowConfigurationContext")
            final CasWebflowEventResolutionConfigurationContext casWebflowConfigurationContext) {
            return new SwivelAuthenticationWebflowEventResolver(casWebflowConfigurationContext);
        }

    }

    @Configuration(value = "SwivelWebflowActionsConfiguration", proxyBeanMethods = false)
    @EnableConfigurationProperties(CasConfigurationProperties.class)
    public static class SwivelWebflowActionsConfiguration {
        @Bean
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        @ConditionalOnMissingBean(name = CasWebflowConstants.ACTION_ID_SWIVEL_AUTHENTICATION)
        public Action swivelAuthenticationWebflowAction(
            @Qualifier("swivelAuthenticationWebflowEventResolver")
            final CasWebflowEventResolver swivelAuthenticationWebflowEventResolver) {
            return new SwivelAuthenticationWebflowAction(swivelAuthenticationWebflowEventResolver);
        }

    }

    @Configuration(value = "SwivelWebflowRegistryConfiguration", proxyBeanMethods = false)
    @EnableConfigurationProperties(CasConfigurationProperties.class)
    public static class SwivelWebflowRegistryConfiguration {
        @Bean
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        @ConditionalOnMissingBean(name = "swivelAuthenticatorFlowRegistry")
        public FlowDefinitionRegistry swivelAuthenticatorFlowRegistry(
            final ConfigurableApplicationContext applicationContext,
            @Qualifier(CasWebflowConstants.BEAN_NAME_FLOW_BUILDER_SERVICES)
            final FlowBuilderServices flowBuilderServices,
            @Qualifier(CasWebflowConstants.BEAN_NAME_FLOW_BUILDER)
            final FlowBuilder flowBuilder) {
            val builder = new FlowDefinitionRegistryBuilder(applicationContext, flowBuilderServices);
            builder.addFlowBuilder(flowBuilder, SwivelMultifactorWebflowConfigurer.MFA_SWIVEL_EVENT_ID);
            return builder.build();
        }
    }

    @Configuration(value = "SwivelWebConfiguration", proxyBeanMethods = false)
    @EnableConfigurationProperties(CasConfigurationProperties.class)
    public static class SwivelWebConfiguration {
        @Bean
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        public SwivelTuringImageGeneratorController swivelTuringImageGeneratorController(
            final CasConfigurationProperties casProperties) {
            val swivel = casProperties.getAuthn().getMfa().getSwivel();
            return new SwivelTuringImageGeneratorController(swivel);
        }

    }

    @Configuration(value = "SwivelWebflowExecutionPlanConfiguration", proxyBeanMethods = false)
    @EnableConfigurationProperties(CasConfigurationProperties.class)
    public static class SwivelWebflowExecutionPlanConfiguration {

        @Bean
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        @ConditionalOnMissingBean(name = "swivelCasWebflowExecutionPlanConfigurer")
        public CasWebflowExecutionPlanConfigurer swivelCasWebflowExecutionPlanConfigurer(
            @Qualifier("swivelMultifactorWebflowConfigurer")
            final CasWebflowConfigurer swivelMultifactorWebflowConfigurer) {
            return plan -> plan.registerWebflowConfigurer(swivelMultifactorWebflowConfigurer);
        }

    }

    @ConditionalOnClass(MultifactorAuthnTrustConfiguration.class)
    @Configuration(value = "SwivelMultifactorTrustConfiguration", proxyBeanMethods = false)
    @DependsOn("swivelMultifactorWebflowConfigurer")
    @ConditionalOnFeatureEnabled(feature = CasFeatureModule.FeatureCatalog.MultifactorAuthenticationTrustedDevices, module = "swivel")
    public static class SwivelMultifactorTrustConfiguration {
        private static final BeanCondition CONDITION = BeanCondition.on("cas.authn.mfa.swivel.trusted-device-enabled")
            .isTrue().evenIfMissing();

        @ConditionalOnMissingBean(name = "swivelMultifactorTrustWebflowConfigurer")
        @Bean
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        public CasWebflowConfigurer swivelMultifactorTrustWebflowConfigurer(
            @Qualifier("swivelAuthenticatorFlowRegistry")
            final FlowDefinitionRegistry swivelAuthenticatorFlowRegistry,
            final ConfigurableApplicationContext applicationContext,
            final CasConfigurationProperties casProperties,
            @Qualifier(CasWebflowConstants.BEAN_NAME_LOGIN_FLOW_DEFINITION_REGISTRY)
            final FlowDefinitionRegistry loginFlowDefinitionRegistry,
            @Qualifier(CasWebflowConstants.BEAN_NAME_FLOW_BUILDER_SERVICES)
            final FlowBuilderServices flowBuilderServices) {
            return BeanSupplier.of(CasWebflowConfigurer.class)
                .when(CONDITION.given(applicationContext.getEnvironment()))
                .supply(() -> {
                    val cfg = new SwivelMultifactorTrustedDeviceWebflowConfigurer(flowBuilderServices,
                        loginFlowDefinitionRegistry,
                        swivelAuthenticatorFlowRegistry, applicationContext, casProperties,
                        MultifactorAuthenticationWebflowUtils.getMultifactorAuthenticationWebflowCustomizers(applicationContext));
                    cfg.setOrder(WEBFLOW_CONFIGURER_ORDER + 1);
                    return cfg;
                })
                .otherwiseProxy()
                .get();
        }

        @Bean
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        public CasWebflowExecutionPlanConfigurer swivelAuthenticationCasWebflowExecutionPlanConfigurer(
            final ConfigurableApplicationContext applicationContext,
            @Qualifier("swivelMultifactorTrustWebflowConfigurer")
            final CasWebflowConfigurer swivelMultifactorTrustWebflowConfigurer) {
            return BeanSupplier.of(CasWebflowExecutionPlanConfigurer.class)
                .when(CONDITION.given(applicationContext.getEnvironment()))
                .supply(() -> plan -> plan.registerWebflowConfigurer(swivelMultifactorTrustWebflowConfigurer))
                .otherwiseProxy()
                .get();
        }
    }
}
