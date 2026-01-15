package org.apereo.cas.config;

import module java.base;
import org.apereo.cas.audit.AuditableExecution;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.configuration.features.CasFeatureModule;
import org.apereo.cas.services.ServicesManager;
import org.apereo.cas.trusted.authentication.DefaultMultifactorAuthenticationTrustedDeviceBypassEvaluator;
import org.apereo.cas.trusted.authentication.MultifactorAuthenticationTrustedDeviceBypassEvaluator;
import org.apereo.cas.trusted.authentication.MultifactorAuthenticationTrustedDeviceNamingStrategy;
import org.apereo.cas.trusted.authentication.api.MultifactorAuthenticationTrustStorage;
import org.apereo.cas.trusted.web.flow.DefaultMultifactorAuthenticationTrustedDeviceProviderAction;
import org.apereo.cas.trusted.web.flow.MultifactorAuthenticationPrepareTrustDeviceViewAction;
import org.apereo.cas.trusted.web.flow.MultifactorAuthenticationRemoveTrustedDeviceAction;
import org.apereo.cas.trusted.web.flow.MultifactorAuthenticationSetTrustAction;
import org.apereo.cas.trusted.web.flow.MultifactorAuthenticationTrustProviderSelectionCriteria;
import org.apereo.cas.trusted.web.flow.MultifactorAuthenticationTrustedDeviceAccountProfileWebflowConfigurer;
import org.apereo.cas.trusted.web.flow.MultifactorAuthenticationVerifyTrustAction;
import org.apereo.cas.trusted.web.flow.fingerprint.DeviceFingerprintStrategy;
import org.apereo.cas.util.spring.beans.BeanSupplier;
import org.apereo.cas.util.spring.boot.ConditionalOnFeatureEnabled;
import org.apereo.cas.web.cookie.CasCookieBuilder;
import org.apereo.cas.web.flow.CasWebflowConfigurer;
import org.apereo.cas.web.flow.CasWebflowConstants;
import org.apereo.cas.web.flow.CasWebflowExecutionPlanConfigurer;
import org.apereo.cas.web.flow.actions.MultifactorAuthenticationTrustedDeviceProviderAction;
import org.apereo.cas.web.flow.actions.WebflowActionBeanSupplier;
import org.apereo.cas.web.flow.actions.composite.MultifactorProviderSelectionCriteria;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.AutoConfigureOrder;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.ScopedProxyMode;
import org.springframework.core.Ordered;
import org.springframework.webflow.definition.registry.FlowDefinitionRegistry;
import org.springframework.webflow.engine.builder.support.FlowBuilderServices;
import org.springframework.webflow.execution.Action;

/**
 * This is {@link MultifactorAuthnTrustWebflowConfiguration}.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 */
@EnableConfigurationProperties(CasConfigurationProperties.class)
@ConditionalOnFeatureEnabled(feature = CasFeatureModule.FeatureCatalog.MultifactorAuthenticationTrustedDevices)
@Configuration(value = "MultifactorAuthnTrustWebflowConfiguration", proxyBeanMethods = false)
class MultifactorAuthnTrustWebflowConfiguration {

    @Configuration(value = "MultifactorAuthnTrustCoreWebflowConfiguration", proxyBeanMethods = false)
    @EnableConfigurationProperties(CasConfigurationProperties.class)
    static class MultifactorAuthnTrustCoreWebflowConfiguration {
        @ConditionalOnMissingBean(name = "mfaTrustedDeviceBypassEvaluator")
        @Bean
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        public MultifactorAuthenticationTrustedDeviceBypassEvaluator mfaTrustedDeviceBypassEvaluator(
            @Qualifier(AuditableExecution.AUDITABLE_EXECUTION_REGISTERED_SERVICE_ACCESS)
            final AuditableExecution registeredServiceAccessStrategyEnforcer) {
            return new DefaultMultifactorAuthenticationTrustedDeviceBypassEvaluator(registeredServiceAccessStrategyEnforcer);
        }

        @ConditionalOnMissingBean(name = CasWebflowConstants.ACTION_ID_MFA_SET_TRUST_ACTION)
        @Bean
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        public Action mfaSetTrustAction(
            final CasConfigurationProperties casProperties,
            final ConfigurableApplicationContext applicationContext,
            @Qualifier("mfaTrustedDeviceBypassEvaluator")
            final MultifactorAuthenticationTrustedDeviceBypassEvaluator mfaTrustedDeviceBypassEvaluator,
            @Qualifier("deviceFingerprintStrategy")
            final DeviceFingerprintStrategy deviceFingerprintStrategy,
            @Qualifier(MultifactorAuthenticationTrustStorage.BEAN_NAME)
            final MultifactorAuthenticationTrustStorage mfaTrustEngine,
            @Qualifier(AuditableExecution.AUDITABLE_EXECUTION_REGISTERED_SERVICE_ACCESS)
            final AuditableExecution registeredServiceAccessStrategyEnforcer) {
            return WebflowActionBeanSupplier.builder()
                .withApplicationContext(applicationContext)
                .withProperties(casProperties)
                .withAction(() -> new MultifactorAuthenticationSetTrustAction(mfaTrustEngine,
                    deviceFingerprintStrategy, casProperties.getAuthn().getMfa().getTrusted(), registeredServiceAccessStrategyEnforcer,
                    mfaTrustedDeviceBypassEvaluator))
                .withId(CasWebflowConstants.ACTION_ID_MFA_SET_TRUST_ACTION)
                .build()
                .get();
        }

        @ConditionalOnMissingBean(name = CasWebflowConstants.ACTION_ID_MFA_VERIFY_TRUST_ACTION)
        @Bean
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        public Action mfaVerifyTrustAction(
            final ConfigurableApplicationContext applicationContext,
            final CasConfigurationProperties casProperties,
            @Qualifier("mfaTrustedDeviceBypassEvaluator")
            final MultifactorAuthenticationTrustedDeviceBypassEvaluator mfaTrustedDeviceBypassEvaluator,
            @Qualifier("deviceFingerprintStrategy")
            final DeviceFingerprintStrategy deviceFingerprintStrategy,
            @Qualifier(MultifactorAuthenticationTrustStorage.BEAN_NAME)
            final MultifactorAuthenticationTrustStorage mfaTrustEngine,
            @Qualifier(AuditableExecution.AUDITABLE_EXECUTION_REGISTERED_SERVICE_ACCESS)
            final AuditableExecution registeredServiceAccessStrategyEnforcer) {
            return WebflowActionBeanSupplier.builder()
                .withApplicationContext(applicationContext)
                .withProperties(casProperties)
                .withAction(() -> new MultifactorAuthenticationVerifyTrustAction(mfaTrustEngine,
                    deviceFingerprintStrategy, casProperties.getAuthn().getMfa().getTrusted(),
                    registeredServiceAccessStrategyEnforcer, mfaTrustedDeviceBypassEvaluator))
                .withId(CasWebflowConstants.ACTION_ID_MFA_VERIFY_TRUST_ACTION)
                .build()
                .get();
        }

        @ConditionalOnMissingBean(name = CasWebflowConstants.ACTION_ID_MFA_PREPARE_TRUST_DEVICE_VIEW_ACTION)
        @Bean
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        public Action mfaPrepareTrustDeviceViewAction(
            final ConfigurableApplicationContext applicationContext,
            final CasConfigurationProperties casProperties,
            @Qualifier("mfaTrustedDeviceBypassEvaluator")
            final MultifactorAuthenticationTrustedDeviceBypassEvaluator mfaTrustedDeviceBypassEvaluator,
            @Qualifier("deviceFingerprintStrategy")
            final DeviceFingerprintStrategy deviceFingerprintStrategy,
            @Qualifier("mfaTrustDeviceNamingStrategy")
            final MultifactorAuthenticationTrustedDeviceNamingStrategy mfaTrustDeviceNamingStrategy,
            @Qualifier(MultifactorAuthenticationTrustStorage.BEAN_NAME)
            final MultifactorAuthenticationTrustStorage mfaTrustEngine,
            @Qualifier(AuditableExecution.AUDITABLE_EXECUTION_REGISTERED_SERVICE_ACCESS)
            final AuditableExecution registeredServiceAccessStrategyEnforcer,
            @Qualifier(ServicesManager.BEAN_NAME)
            final ServicesManager servicesManager) {
            return WebflowActionBeanSupplier.builder()
                .withApplicationContext(applicationContext)
                .withProperties(casProperties)
                .withAction(() -> new MultifactorAuthenticationPrepareTrustDeviceViewAction(mfaTrustEngine,
                    deviceFingerprintStrategy, casProperties.getAuthn().getMfa().getTrusted(),
                    registeredServiceAccessStrategyEnforcer, servicesManager,
                    mfaTrustedDeviceBypassEvaluator, mfaTrustDeviceNamingStrategy))
                .withId(CasWebflowConstants.ACTION_ID_MFA_PREPARE_TRUST_DEVICE_VIEW_ACTION)
                .build()
                .get();
        }

        @ConditionalOnMissingBean(name = "mfaTrustProviderSelectionCriteria")
        @Bean
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        public MultifactorProviderSelectionCriteria mfaTrustProviderSelectionCriteria(
            final CasConfigurationProperties casProperties,
            @Qualifier("deviceFingerprintCookieGenerator")
            final CasCookieBuilder deviceFingerprintCookieGenerator,
            @Qualifier(ServicesManager.BEAN_NAME)
            final ServicesManager servicesManager,
            @Qualifier(MultifactorAuthenticationTrustStorage.BEAN_NAME)
            final MultifactorAuthenticationTrustStorage mfaTrustEngine,
            @Qualifier("mfaTrustDeviceNamingStrategy")
            final MultifactorAuthenticationTrustedDeviceNamingStrategy mfaTrustDeviceNamingStrategy,
            @Qualifier("deviceFingerprintStrategy")
            final DeviceFingerprintStrategy deviceFingerprintStrategy) {
            return new MultifactorAuthenticationTrustProviderSelectionCriteria(servicesManager,
                mfaTrustEngine, mfaTrustDeviceNamingStrategy, deviceFingerprintStrategy, deviceFingerprintCookieGenerator, casProperties);
        }
    }

    @Configuration(value = "TrustedAuthenticationAccountProfileWebflowConfiguration", proxyBeanMethods = false)
    @EnableConfigurationProperties(CasConfigurationProperties.class)
    @ConditionalOnFeatureEnabled(feature = CasFeatureModule.FeatureCatalog.AccountManagement, enabledByDefault = false)
    @AutoConfigureOrder(Ordered.LOWEST_PRECEDENCE)
    static class MultifactorAuthnTrustAccountProfileWebflowConfiguration {
        @Bean
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        @ConditionalOnMissingBean(name = "accountProfileMultifactorTrustedDeviceWebflowExecutionPlanConfigurer")
        public CasWebflowExecutionPlanConfigurer accountProfileMultifactorTrustedDeviceWebflowExecutionPlanConfigurer(
            @Qualifier("accountProfileMultifactorTrustedDeviceWebflowConfigurer")
            final CasWebflowConfigurer accountProfileMultifactorTrustedDeviceWebflowConfigurer) {
            return plan -> plan.registerWebflowConfigurer(accountProfileMultifactorTrustedDeviceWebflowConfigurer);
        }

        @ConditionalOnMissingBean(name = "accountProfileMultifactorTrustedDeviceWebflowConfigurer")
        @Bean
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        public CasWebflowConfigurer accountProfileMultifactorTrustedDeviceWebflowConfigurer(
            final CasConfigurationProperties casProperties,
            final ConfigurableApplicationContext applicationContext,
            @Qualifier(CasWebflowConstants.BEAN_NAME_FLOW_DEFINITION_REGISTRY)
            final FlowDefinitionRegistry flowDefinitionRegistry,
            @Qualifier(CasWebflowConstants.BEAN_NAME_FLOW_BUILDER_SERVICES)
            final FlowBuilderServices flowBuilderServices) {
            return BeanSupplier.of(CasWebflowConfigurer.class)
                .alwaysMatch()
                .supply(() -> new MultifactorAuthenticationTrustedDeviceAccountProfileWebflowConfigurer(flowBuilderServices,
                    flowDefinitionRegistry, applicationContext, casProperties))
                .otherwiseProxy()
                .get();
        }


        @ConditionalOnMissingBean(name = "multifactorAuthenticationTrustedDeviceProviderAction")
        @Bean
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        public MultifactorAuthenticationTrustedDeviceProviderAction multifactorAuthenticationTrustedDeviceProviderAction(
            @Qualifier(MultifactorAuthenticationTrustStorage.BEAN_NAME)
            final MultifactorAuthenticationTrustStorage mfaTrustEngine,
            final ConfigurableApplicationContext applicationContext) {
            return new DefaultMultifactorAuthenticationTrustedDeviceProviderAction(mfaTrustEngine);
        }

        @Bean
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        @ConditionalOnMissingBean(name = CasWebflowConstants.ACTION_ID_ACCOUNT_PROFILE_REMOVE_MFA_TRUSTED_DEVICE)
        public Action accountProfileRemoveMultifactorTrustedDeviceAction(
            @Qualifier(MultifactorAuthenticationTrustStorage.BEAN_NAME)
            final MultifactorAuthenticationTrustStorage mfaTrustEngine,
            final ConfigurableApplicationContext applicationContext,
            final CasConfigurationProperties casProperties) {
            return WebflowActionBeanSupplier.builder()
                .withApplicationContext(applicationContext)
                .withProperties(casProperties)
                .withAction(() -> new MultifactorAuthenticationRemoveTrustedDeviceAction(mfaTrustEngine))
                .withId(CasWebflowConstants.ACTION_ID_ACCOUNT_PROFILE_REMOVE_MFA_TRUSTED_DEVICE)
                .build()
                .get();
        }
    }
}
