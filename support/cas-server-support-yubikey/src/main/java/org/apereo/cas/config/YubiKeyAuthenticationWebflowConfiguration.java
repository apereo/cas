package org.apereo.cas.config;

import org.apereo.cas.adaptors.yubikey.YubiKeyAccountRegistry;
import org.apereo.cas.adaptors.yubikey.web.flow.YubiKeyAccountCheckRegistrationAction;
import org.apereo.cas.adaptors.yubikey.web.flow.YubiKeyAccountSaveRegistrationAction;
import org.apereo.cas.adaptors.yubikey.web.flow.YubiKeyAuthenticationPrepareLoginAction;
import org.apereo.cas.adaptors.yubikey.web.flow.YubiKeyAuthenticationWebflowAction;
import org.apereo.cas.adaptors.yubikey.web.flow.YubiKeyMultifactorWebflowConfigurer;
import org.apereo.cas.authentication.device.MultifactorAuthenticationDeviceManager;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.configuration.features.CasFeatureModule;
import org.apereo.cas.multitenancy.TenantExtractor;
import org.apereo.cas.trusted.web.flow.BasicMultifactorTrustedWebflowConfigurer;
import org.apereo.cas.util.spring.beans.BeanCondition;
import org.apereo.cas.util.spring.beans.BeanSupplier;
import org.apereo.cas.util.spring.boot.ConditionalOnFeatureEnabled;
import org.apereo.cas.web.flow.CasWebflowConfigurer;
import org.apereo.cas.web.flow.CasWebflowConstants;
import org.apereo.cas.web.flow.CasWebflowExecutionPlanConfigurer;
import org.apereo.cas.web.flow.actions.DefaultMultifactorAuthenticationDeviceProviderAction;
import org.apereo.cas.web.flow.actions.MultifactorAuthenticationDeviceProviderAction;
import org.apereo.cas.web.flow.actions.WebflowActionBeanSupplier;
import org.apereo.cas.web.flow.authentication.FinalMultifactorAuthenticationTransactionWebflowEventResolver;
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
 * This is {@link YubiKeyAuthenticationWebflowConfiguration}.
 *
 * @author Misagh Moayyed
 * @author Dmitriy Kopylenko
 * @since 5.1.0
 */
@EnableConfigurationProperties(CasConfigurationProperties.class)
@ConditionalOnFeatureEnabled(feature = CasFeatureModule.FeatureCatalog.YubiKey)
@Configuration(value = "YubiKeyAuthenticationWebflowConfiguration", proxyBeanMethods = false)
class YubiKeyAuthenticationWebflowConfiguration {
    private static final int WEBFLOW_CONFIGURER_ORDER = 100;

    @Configuration(value = "YubiKeyAuthenticationWebflowPlanConfiguration", proxyBeanMethods = false)
    @EnableConfigurationProperties(CasConfigurationProperties.class)
    static class YubiKeyAuthenticationWebflowPlanConfiguration {
        @Bean
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        @ConditionalOnMissingBean(name = "yubikeyCasWebflowExecutionPlanConfigurer")
        public CasWebflowExecutionPlanConfigurer yubikeyCasWebflowExecutionPlanConfigurer(
            @Qualifier("yubikeyMultifactorWebflowConfigurer")
            final CasWebflowConfigurer yubikeyMultifactorWebflowConfigurer) {
            return plan -> plan.registerWebflowConfigurer(yubikeyMultifactorWebflowConfigurer);
        }
    }

    @Configuration(value = "YubiKeyAuthenticationWebflowCoreConfiguration", proxyBeanMethods = false)
    @EnableConfigurationProperties(CasConfigurationProperties.class)
    static class YubiKeyAuthenticationWebflowCoreConfiguration {
        @Bean
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        @ConditionalOnMissingBean(name = "yubikeyFlowRegistry")
        public FlowDefinitionRegistry yubikeyFlowRegistry(
            @Qualifier(CasWebflowConstants.BEAN_NAME_FLOW_BUILDER)
            final FlowBuilder flowBuilder,
            final ConfigurableApplicationContext applicationContext,
            @Qualifier(CasWebflowConstants.BEAN_NAME_FLOW_BUILDER_SERVICES)
            final FlowBuilderServices flowBuilderServices) {
            val builder = new FlowDefinitionRegistryBuilder(applicationContext, flowBuilderServices);
            builder.addFlowBuilder(flowBuilder, YubiKeyMultifactorWebflowConfigurer.MFA_YUBIKEY_EVENT_ID);
            return builder.build();
        }

        @Bean
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        @ConditionalOnMissingBean(name = "yubikeyAuthenticationWebflowEventResolver")
        public CasWebflowEventResolver yubikeyAuthenticationWebflowEventResolver(
            @Qualifier(CasWebflowEventResolutionConfigurationContext.BEAN_NAME)
            final CasWebflowEventResolutionConfigurationContext casWebflowConfigurationContext) {
            return new FinalMultifactorAuthenticationTransactionWebflowEventResolver(casWebflowConfigurationContext);
        }


        @ConditionalOnMissingBean(name = "yubikeyMultifactorWebflowConfigurer")
        @Bean
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        public CasWebflowConfigurer yubikeyMultifactorWebflowConfigurer(
            @Qualifier("yubikeyFlowRegistry")
            final FlowDefinitionRegistry yubikeyFlowRegistry,
            final ConfigurableApplicationContext applicationContext,
            final CasConfigurationProperties casProperties,
            @Qualifier(CasWebflowConstants.BEAN_NAME_FLOW_DEFINITION_REGISTRY)
            final FlowDefinitionRegistry flowDefinitionRegistry,
            @Qualifier(CasWebflowConstants.BEAN_NAME_FLOW_BUILDER_SERVICES)
            final FlowBuilderServices flowBuilderServices) {
            val cfg = new YubiKeyMultifactorWebflowConfigurer(flowBuilderServices,
                flowDefinitionRegistry, yubikeyFlowRegistry,
                applicationContext, casProperties,
                MultifactorAuthenticationWebflowUtils.getMultifactorAuthenticationWebflowCustomizers(applicationContext));
            cfg.setOrder(WEBFLOW_CONFIGURER_ORDER);
            return cfg;
        }

    }

    @Configuration(value = "YubiKeyAuthenticationWebflowActionConfiguration", proxyBeanMethods = false)
    @EnableConfigurationProperties(CasConfigurationProperties.class)
    static class YubiKeyAuthenticationWebflowActionConfiguration {

        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        @Bean
        @ConditionalOnMissingBean(name = CasWebflowConstants.ACTION_ID_YUBIKEY_AUTHENTICATION)
        public Action yubikeyAuthenticationWebflowAction(
            final ConfigurableApplicationContext applicationContext,
            final CasConfigurationProperties casProperties,
            @Qualifier("yubikeyAuthenticationWebflowEventResolver")
            final CasWebflowEventResolver yubikeyAuthenticationWebflowEventResolver) {
            return WebflowActionBeanSupplier.builder()
                .withApplicationContext(applicationContext)
                .withProperties(casProperties)
                .withAction(() -> new YubiKeyAuthenticationWebflowAction(yubikeyAuthenticationWebflowEventResolver))
                .withId(CasWebflowConstants.ACTION_ID_YUBIKEY_AUTHENTICATION)
                .build()
                .get();
        }

        @Bean
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        @ConditionalOnMissingBean(name = CasWebflowConstants.ACTION_ID_YUBIKEY_PREPARE_LOGIN)
        public Action prepareYubiKeyAuthenticationLoginAction(
            final ConfigurableApplicationContext applicationContext,
            final CasConfigurationProperties casProperties) {
            return WebflowActionBeanSupplier.builder()
                .withApplicationContext(applicationContext)
                .withProperties(casProperties)
                .withAction(() -> new YubiKeyAuthenticationPrepareLoginAction(casProperties))
                .withId(CasWebflowConstants.ACTION_ID_YUBIKEY_PREPARE_LOGIN)
                .build()
                .get();
        }

        @Bean
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        @ConditionalOnMissingBean(name = CasWebflowConstants.ACTION_ID_YUBIKEY_ACCOUNT_REGISTRATION)
        public Action yubiKeyAccountRegistrationAction(
            final ConfigurableApplicationContext applicationContext,
            final CasConfigurationProperties casProperties,
            @Qualifier(TenantExtractor.BEAN_NAME)
            final TenantExtractor tenantExtractor,
            @Qualifier("yubiKeyAccountRegistry")
            final YubiKeyAccountRegistry yubiKeyAccountRegistry) {
            return WebflowActionBeanSupplier.builder()
                .withApplicationContext(applicationContext)
                .withProperties(casProperties)
                .withAction(() -> new YubiKeyAccountCheckRegistrationAction(yubiKeyAccountRegistry, tenantExtractor))
                .withId(CasWebflowConstants.ACTION_ID_YUBIKEY_ACCOUNT_REGISTRATION)
                .build()
                .get();
        }

        @Bean
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        @ConditionalOnMissingBean(name = CasWebflowConstants.ACTION_ID_YUBIKEY_SAVE_ACCOUNT_REGISTRATION)
        public Action yubiKeySaveAccountRegistrationAction(
            final ConfigurableApplicationContext applicationContext,
            final CasConfigurationProperties casProperties,
            @Qualifier(TenantExtractor.BEAN_NAME)
            final TenantExtractor tenantExtractor,
            @Qualifier("yubiKeyAccountRegistry")
            final YubiKeyAccountRegistry yubiKeyAccountRegistry) {
            return WebflowActionBeanSupplier.builder()
                .withApplicationContext(applicationContext)
                .withProperties(casProperties)
                .withAction(() -> new YubiKeyAccountSaveRegistrationAction(yubiKeyAccountRegistry, tenantExtractor))
                .withId(CasWebflowConstants.ACTION_ID_YUBIKEY_SAVE_ACCOUNT_REGISTRATION)
                .build()
                .get();
        }

        @Bean
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        @ConditionalOnMissingBean(name = "yubiKeyDeviceProviderAction")
        public MultifactorAuthenticationDeviceProviderAction yubiKeyDeviceProviderAction(
            @Qualifier("yubiKeyMultifactorAuthenticatorDeviceManager")
            final MultifactorAuthenticationDeviceManager yubiKeyMultifactorAuthenticatorDeviceManager) {
            return new DefaultMultifactorAuthenticationDeviceProviderAction(yubiKeyMultifactorAuthenticatorDeviceManager);
        }
    }

    @ConditionalOnClass(MultifactorAuthnTrustConfiguration.class)
    @ConditionalOnFeatureEnabled(feature = CasFeatureModule.FeatureCatalog.MultifactorAuthenticationTrustedDevices, module = "yubikey")
    @Configuration(value = "YubiMultifactorTrustConfiguration", proxyBeanMethods = false)
    @DependsOn("yubikeyMultifactorWebflowConfigurer")
    static class YubiKeyMultifactorTrustConfiguration {
        private static final BeanCondition CONDITION = BeanCondition.on("cas.authn.mfa.yubikey.trusted-device-enabled")
            .isTrue().evenIfMissing();

        @ConditionalOnMissingBean(name = "yubiMultifactorTrustWebflowConfigurer")
        @Bean
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        public CasWebflowConfigurer yubiMultifactorTrustWebflowConfigurer(
            @Qualifier("yubikeyFlowRegistry")
            final FlowDefinitionRegistry yubikeyFlowRegistry,
            final ConfigurableApplicationContext applicationContext,
            final CasConfigurationProperties casProperties,
            @Qualifier(CasWebflowConstants.BEAN_NAME_FLOW_DEFINITION_REGISTRY)
            final FlowDefinitionRegistry flowDefinitionRegistry,
            @Qualifier(CasWebflowConstants.BEAN_NAME_FLOW_BUILDER_SERVICES)
            final FlowBuilderServices flowBuilderServices) {
            return BeanSupplier.of(CasWebflowConfigurer.class)
                .when(CONDITION.given(applicationContext.getEnvironment()))
                .supply(() -> {
                    val cfg = new BasicMultifactorTrustedWebflowConfigurer(flowBuilderServices,
                        flowDefinitionRegistry,
                        yubikeyFlowRegistry,
                        applicationContext,
                        casProperties,
                        MultifactorAuthenticationWebflowUtils.getMultifactorAuthenticationWebflowCustomizers(applicationContext));
                    cfg.setOrder(WEBFLOW_CONFIGURER_ORDER + 1);
                    return cfg;
                })
                .otherwiseProxy()
                .get();
        }

        @Bean
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        @ConditionalOnMissingBean(name = "yubiMultifactorCasWebflowExecutionPlanConfigurer")
        public CasWebflowExecutionPlanConfigurer yubiMultifactorCasWebflowExecutionPlanConfigurer(
            final ConfigurableApplicationContext applicationContext,
            @Qualifier("yubiMultifactorTrustWebflowConfigurer")
            final CasWebflowConfigurer yubiMultifactorTrustWebflowConfigurer) {
            return BeanSupplier.of(CasWebflowExecutionPlanConfigurer.class)
                .when(CONDITION.given(applicationContext.getEnvironment()))
                .supply(() -> plan -> plan.registerWebflowConfigurer(yubiMultifactorTrustWebflowConfigurer))
                .otherwiseProxy()
                .get();
        }
    }
}
