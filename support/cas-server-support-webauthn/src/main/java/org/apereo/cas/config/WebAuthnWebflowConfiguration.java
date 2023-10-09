package org.apereo.cas.config;

import org.apereo.cas.authentication.MultifactorAuthenticationProvider;
import org.apereo.cas.authentication.principal.PrincipalFactory;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.configuration.features.CasFeatureModule;
import org.apereo.cas.util.spring.beans.BeanCondition;
import org.apereo.cas.util.spring.beans.BeanSupplier;
import org.apereo.cas.util.spring.boot.ConditionalOnFeatureEnabled;
import org.apereo.cas.web.flow.CasWebflowConfigurer;
import org.apereo.cas.web.flow.CasWebflowConstants;
import org.apereo.cas.web.flow.CasWebflowExecutionPlanConfigurer;
import org.apereo.cas.web.flow.actions.ConsumerExecutionAction;
import org.apereo.cas.web.flow.actions.MultifactorAuthenticationDeviceProviderAction;
import org.apereo.cas.web.flow.authentication.FinalMultifactorAuthenticationTransactionWebflowEventResolver;
import org.apereo.cas.web.flow.resolver.CasWebflowEventResolver;
import org.apereo.cas.web.flow.resolver.impl.CasWebflowEventResolutionConfigurationContext;
import org.apereo.cas.web.flow.util.MultifactorAuthenticationWebflowUtils;
import org.apereo.cas.webauthn.storage.WebAuthnCredentialRepository;
import org.apereo.cas.webauthn.web.flow.WebAuthnAccountCheckRegistrationAction;
import org.apereo.cas.webauthn.web.flow.WebAuthnAccountSaveRegistrationAction;
import org.apereo.cas.webauthn.web.flow.WebAuthnAuthenticationWebflowAction;
import org.apereo.cas.webauthn.web.flow.WebAuthnMultifactorTrustWebflowConfigurer;
import org.apereo.cas.webauthn.web.flow.WebAuthnMultifactorWebflowConfigurer;
import org.apereo.cas.webauthn.web.flow.WebAuthnPopulateCsrfTokenAction;
import org.apereo.cas.webauthn.web.flow.WebAuthnStartAuthenticationAction;
import org.apereo.cas.webauthn.web.flow.WebAuthnStartRegistrationAction;
import org.apereo.cas.webauthn.web.flow.WebAuthnValidateSessionCredentialTokenAction;
import org.apereo.cas.webauthn.web.flow.account.WebAuthnMultifactorAccountProfilePrepareAction;
import org.apereo.cas.webauthn.web.flow.account.WebAuthnMultifactorAccountProfileRegistrationAction;
import org.apereo.cas.webauthn.web.flow.account.WebAuthnMultifactorAccountProfileWebflowConfigurer;
import org.apereo.cas.webauthn.web.flow.account.WebAuthnMultifactorDeviceProviderAction;
import com.yubico.core.RegistrationStorage;
import com.yubico.core.SessionManager;
import lombok.val;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.AutoConfigureOrder;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.DependsOn;
import org.springframework.context.annotation.ScopedProxyMode;
import org.springframework.core.Ordered;
import org.springframework.security.web.csrf.CsrfTokenRepository;
import org.springframework.webflow.config.FlowDefinitionRegistryBuilder;
import org.springframework.webflow.definition.registry.FlowDefinitionRegistry;
import org.springframework.webflow.engine.builder.FlowBuilder;
import org.springframework.webflow.engine.builder.support.FlowBuilderServices;
import org.springframework.webflow.execution.Action;

/**
 * This is {@link WebAuthnWebflowConfiguration}.
 *
 * @author Misagh Moayyed
 * @since 6.1.0
 */
@EnableConfigurationProperties(CasConfigurationProperties.class)
@ConditionalOnFeatureEnabled(feature = CasFeatureModule.FeatureCatalog.WebAuthn)
@AutoConfiguration
public class WebAuthnWebflowConfiguration {
    private static final int WEBFLOW_CONFIGURER_ORDER = 100;

    private static final BeanCondition CONDITION = BeanCondition.on("cas.authn.mfa.web-authn.core.enabled")
        .isTrue().evenIfMissing();

    @Configuration(value = "WebAuthnWebflowRegistryConfiguration", proxyBeanMethods = false)
    @EnableConfigurationProperties(CasConfigurationProperties.class)
    public static class WebAuthnWebflowRegistryConfiguration {
        @Bean
        @ConditionalOnMissingBean(name = "webAuthnFlowRegistry")
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        public FlowDefinitionRegistry webAuthnFlowRegistry(
            @Qualifier(CasWebflowConstants.BEAN_NAME_FLOW_BUILDER_SERVICES) final FlowBuilderServices flowBuilderServices,
            @Qualifier(CasWebflowConstants.BEAN_NAME_FLOW_BUILDER) final FlowBuilder flowBuilder,
            final ConfigurableApplicationContext applicationContext) {
            return BeanSupplier.of(FlowDefinitionRegistry.class)
                .when(CONDITION.given(applicationContext.getEnvironment()))
                .supply(() -> {
                    val builder = new FlowDefinitionRegistryBuilder(applicationContext, flowBuilderServices);
                    builder.addFlowBuilder(flowBuilder, WebAuthnMultifactorWebflowConfigurer.FLOW_ID_MFA_WEBAUTHN);
                    return builder.build();
                })
                .otherwiseProxy()
                .get();
        }
    }

    @Configuration(value = "WebAuthnWebflowBaseConfiguration", proxyBeanMethods = false)
    @EnableConfigurationProperties(CasConfigurationProperties.class)
    public static class WebAuthnWebflowBaseConfiguration {
        @ConditionalOnMissingBean(name = "webAuthnMultifactorWebflowConfigurer")
        @Bean
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        public CasWebflowConfigurer webAuthnMultifactorWebflowConfigurer(
            @Qualifier(CasWebflowConstants.BEAN_NAME_LOGIN_FLOW_DEFINITION_REGISTRY) final FlowDefinitionRegistry loginFlowDefinitionRegistry,
            @Qualifier(CasWebflowConstants.BEAN_NAME_FLOW_BUILDER_SERVICES) final FlowBuilderServices flowBuilderServices,
            @Qualifier("webAuthnFlowRegistry") final FlowDefinitionRegistry webAuthnFlowRegistry,
            final ConfigurableApplicationContext applicationContext,
            final CasConfigurationProperties casProperties) {
            return BeanSupplier.of(CasWebflowConfigurer.class)
                .when(CONDITION.given(applicationContext.getEnvironment()))
                .supply(() -> {
                    val cfg = new WebAuthnMultifactorWebflowConfigurer(flowBuilderServices,
                        loginFlowDefinitionRegistry, webAuthnFlowRegistry,
                        applicationContext, casProperties,
                        MultifactorAuthenticationWebflowUtils.getMultifactorAuthenticationWebflowCustomizers(applicationContext));
                    cfg.setOrder(WEBFLOW_CONFIGURER_ORDER);
                    return cfg;
                })
                .otherwiseProxy()
                .get();
        }
    }

    @Configuration(value = "WebAuthnWebflowEventResolutionConfiguration", proxyBeanMethods = false)
    @EnableConfigurationProperties(CasConfigurationProperties.class)
    public static class WebAuthnWebflowEventResolutionConfiguration {
        @ConditionalOnMissingBean(name = "webAuthnAuthenticationWebflowEventResolver")
        @Bean
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        public CasWebflowEventResolver webAuthnAuthenticationWebflowEventResolver(
            final ConfigurableApplicationContext applicationContext,
            @Qualifier("casWebflowConfigurationContext") final CasWebflowEventResolutionConfigurationContext casWebflowConfigurationContext) {
            return BeanSupplier.of(CasWebflowEventResolver.class)
                .when(CONDITION.given(applicationContext.getEnvironment()))
                .supply(() -> new FinalMultifactorAuthenticationTransactionWebflowEventResolver(casWebflowConfigurationContext))
                .otherwiseProxy()
                .get();
        }

    }

    @Configuration(value = "WebAuthnWebflowExecutionPlanConfiguration", proxyBeanMethods = false)
    @EnableConfigurationProperties(CasConfigurationProperties.class)
    public static class WebAuthnWebflowExecutionPlanConfiguration {
        @ConditionalOnMissingBean(name = "webAuthnCasWebflowExecutionPlanConfigurer")
        @Bean
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        public CasWebflowExecutionPlanConfigurer webAuthnCasWebflowExecutionPlanConfigurer(
            final ConfigurableApplicationContext applicationContext,
            @Qualifier("webAuthnMultifactorWebflowConfigurer") final CasWebflowConfigurer webAuthnMultifactorWebflowConfigurer) {
            return BeanSupplier.of(CasWebflowExecutionPlanConfigurer.class)
                .when(CONDITION.given(applicationContext.getEnvironment()))
                .supply(() -> plan -> plan.registerWebflowConfigurer(webAuthnMultifactorWebflowConfigurer))
                .otherwiseProxy()
                .get();
        }

    }

    @ConditionalOnClass(MultifactorAuthnTrustConfiguration.class)
    @ConditionalOnFeatureEnabled(feature = CasFeatureModule.FeatureCatalog.MultifactorAuthenticationTrustedDevices, module = "webauthn")
    @Configuration(value = "WebAuthnMultifactorTrustConfiguration", proxyBeanMethods = false)
    @DependsOn("webAuthnMultifactorWebflowConfigurer")
    public static class WebAuthnMultifactorTrustConfiguration {
        private static final BeanCondition CONDITION = BeanCondition.on("cas.authn.mfa.web-authn.trusted-device-enabled")
            .isTrue().evenIfMissing();

        @ConditionalOnMissingBean(name = "webAuthnMultifactorTrustWebflowConfigurer")
        @Bean
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        public CasWebflowConfigurer webAuthnMultifactorTrustWebflowConfigurer(
            @Qualifier("webAuthnFlowRegistry") final FlowDefinitionRegistry webAuthnFlowRegistry,
            @Qualifier(CasWebflowConstants.BEAN_NAME_LOGIN_FLOW_DEFINITION_REGISTRY) final FlowDefinitionRegistry loginFlowDefinitionRegistry,
            @Qualifier(CasWebflowConstants.BEAN_NAME_FLOW_BUILDER_SERVICES) final FlowBuilderServices flowBuilderServices,
            final ConfigurableApplicationContext applicationContext,
            final CasConfigurationProperties casProperties) {
            return BeanSupplier.of(CasWebflowConfigurer.class)
                .when(WebAuthnWebflowConfiguration.CONDITION.given(applicationContext.getEnvironment()))
                .and(WebAuthnMultifactorTrustConfiguration.CONDITION.given(applicationContext.getEnvironment()))
                .supply(() -> {
                    val cfg = new WebAuthnMultifactorTrustWebflowConfigurer(
                        flowBuilderServices,
                        loginFlowDefinitionRegistry,
                        webAuthnFlowRegistry,
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
        public CasWebflowExecutionPlanConfigurer webAuthnMultifactorTrustCasWebflowExecutionPlanConfigurer(
            final ConfigurableApplicationContext applicationContext,
            @Qualifier("webAuthnMultifactorTrustWebflowConfigurer") final CasWebflowConfigurer webAuthnMultifactorTrustWebflowConfigurer) {
            return BeanSupplier.of(CasWebflowExecutionPlanConfigurer.class)
                .when(WebAuthnWebflowConfiguration.CONDITION.given(applicationContext.getEnvironment()))
                .and(WebAuthnMultifactorTrustConfiguration.CONDITION.given(applicationContext.getEnvironment()))
                .supply(() -> plan -> plan.registerWebflowConfigurer(webAuthnMultifactorTrustWebflowConfigurer))
                .otherwiseProxy()
                .get();
        }
    }

    @Configuration(value = "WebAuthnWebflowActionConfiguration", proxyBeanMethods = false)
    @EnableConfigurationProperties(CasConfigurationProperties.class)
    public static class WebAuthnWebflowActionConfiguration {

        @ConditionalOnMissingBean(name = CasWebflowConstants.ACTION_ID_WEBAUTHN_POPULATE_CSRF_TOKEN)
        @Bean
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        public Action webAuthnPopulateCsrfTokenAction(
            @Qualifier("webAuthnCsrfTokenRepository") final CsrfTokenRepository webAuthnCsrfTokenRepository,
            final ConfigurableApplicationContext applicationContext) {
            return BeanSupplier.of(Action.class)
                .when(CONDITION.given(applicationContext.getEnvironment()))
                .supply(() -> new WebAuthnPopulateCsrfTokenAction(webAuthnCsrfTokenRepository))
                .otherwise(() -> ConsumerExecutionAction.NONE)
                .get();
        }

        @ConditionalOnMissingBean(name = CasWebflowConstants.ACTION_ID_WEBAUTHN_START_AUTHENTICATION)
        @Bean
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        public Action webAuthnStartAuthenticationAction(
            final ConfigurableApplicationContext applicationContext,
            @Qualifier(WebAuthnCredentialRepository.BEAN_NAME) final RegistrationStorage webAuthnCredentialRepository) {
            return BeanSupplier.of(Action.class)
                .when(CONDITION.given(applicationContext.getEnvironment()))
                .supply(() -> new WebAuthnStartAuthenticationAction(webAuthnCredentialRepository))
                .otherwise(() -> ConsumerExecutionAction.NONE)
                .get();
        }

        @ConditionalOnMissingBean(name = CasWebflowConstants.ACTION_ID_WEB_AUTHN_START_REGISTRATION)
        @Bean
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        public Action webAuthnStartRegistrationAction(
            final ConfigurableApplicationContext applicationContext,
            final CasConfigurationProperties casProperties) {
            return BeanSupplier.of(Action.class)
                .when(CONDITION.given(applicationContext.getEnvironment()))
                .supply(() -> new WebAuthnStartRegistrationAction(casProperties))
                .otherwise(() -> ConsumerExecutionAction.NONE)
                .get();
        }

        @ConditionalOnMissingBean(name = CasWebflowConstants.ACTION_ID_WEBAUTHN_CHECK_ACCOUNT_REGISTRATION)
        @Bean
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        public Action webAuthnCheckAccountRegistrationAction(
            final ConfigurableApplicationContext applicationContext,
            @Qualifier(WebAuthnCredentialRepository.BEAN_NAME) final RegistrationStorage webAuthnCredentialRepository) {
            return BeanSupplier.of(Action.class)
                .when(CONDITION.given(applicationContext.getEnvironment()))
                .supply(() -> new WebAuthnAccountCheckRegistrationAction(webAuthnCredentialRepository))
                .otherwise(() -> ConsumerExecutionAction.NONE)
                .get();
        }

        @ConditionalOnMissingBean(name = CasWebflowConstants.ACTION_ID_WEBAUTHN_SAVE_ACCOUNT_REGISTRATION)
        @Bean
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        public Action webAuthnSaveAccountRegistrationAction(
            final ConfigurableApplicationContext applicationContext,
            @Qualifier("webAuthnSessionManager") final SessionManager webAuthnSessionManager,
            @Qualifier(WebAuthnCredentialRepository.BEAN_NAME) final RegistrationStorage webAuthnCredentialRepository) {
            return BeanSupplier.of(Action.class)
                .when(CONDITION.given(applicationContext.getEnvironment()))
                .supply(() -> new WebAuthnAccountSaveRegistrationAction(webAuthnCredentialRepository, webAuthnSessionManager))
                .otherwise(() -> ConsumerExecutionAction.NONE)
                .get();
        }

        @ConditionalOnMissingBean(name = CasWebflowConstants.ACTION_ID_WEBAUTHN_AUTHENTICATION_WEBFLOW)
        @Bean
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        public Action webAuthnAuthenticationWebflowAction(
            final ConfigurableApplicationContext applicationContext,
            @Qualifier("webAuthnAuthenticationWebflowEventResolver") final CasWebflowEventResolver webAuthnAuthenticationWebflowEventResolver) {
            return BeanSupplier.of(Action.class)
                .when(CONDITION.given(applicationContext.getEnvironment()))
                .supply(() -> new WebAuthnAuthenticationWebflowAction(webAuthnAuthenticationWebflowEventResolver))
                .otherwise(() -> ConsumerExecutionAction.NONE)
                .get();
        }

        @ConditionalOnMissingBean(name = CasWebflowConstants.ACTION_ID_WEBAUTHN_VALIDATE_SESSION_CREDENTIAL_TOKEN)
        @Bean
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        public Action webAuthnValidateSessionCredentialTokenAction(
            final ConfigurableApplicationContext applicationContext,
            @Qualifier("webAuthnSessionManager") final SessionManager webAuthnSessionManager,
            @Qualifier("webAuthnPrincipalFactory") final PrincipalFactory webAuthnPrincipalFactory,
            @Qualifier(WebAuthnCredentialRepository.BEAN_NAME) final RegistrationStorage webAuthnCredentialRepository) {
            return BeanSupplier.of(Action.class)
                .when(CONDITION.given(applicationContext.getEnvironment()))
                .supply(() -> new WebAuthnValidateSessionCredentialTokenAction(webAuthnCredentialRepository,
                    webAuthnSessionManager, webAuthnPrincipalFactory))
                .otherwise(() -> ConsumerExecutionAction.NONE)
                .get();
        }
    }

    @Configuration(value = "WebAuthnAccountProfileWebflowConfiguration", proxyBeanMethods = false)
    @EnableConfigurationProperties(CasConfigurationProperties.class)
    @ConditionalOnFeatureEnabled(feature = CasFeatureModule.FeatureCatalog.AccountManagement, enabledByDefault = false)
    @AutoConfigureOrder(Ordered.LOWEST_PRECEDENCE)
    public static class WebAuthnAccountProfileWebflowConfiguration {
        @ConditionalOnMissingBean(name = "webAuthnAccountProfileWebflowConfigurer")
        @Bean
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        public CasWebflowConfigurer webAuthnAccountProfileWebflowConfigurer(
            final CasConfigurationProperties casProperties,
            final ConfigurableApplicationContext applicationContext,
            @Qualifier(CasWebflowConstants.BEAN_NAME_ACCOUNT_PROFILE_FLOW_DEFINITION_REGISTRY) final FlowDefinitionRegistry accountProfileFlowRegistry,
            @Qualifier(CasWebflowConstants.BEAN_NAME_FLOW_BUILDER_SERVICES) final FlowBuilderServices flowBuilderServices) {
            return BeanSupplier.of(CasWebflowConfigurer.class)
                .when(CONDITION.given(applicationContext.getEnvironment()))
                .supply(() -> new WebAuthnMultifactorAccountProfileWebflowConfigurer(flowBuilderServices,
                    accountProfileFlowRegistry, applicationContext, casProperties))
                .otherwiseProxy()
                .get();
        }

        @Bean
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        @ConditionalOnMissingBean(name = "webAuthnAccountCasWebflowExecutionPlanConfigurer")
        public CasWebflowExecutionPlanConfigurer webAuthnAccountCasWebflowExecutionPlanConfigurer(
            @Qualifier("webAuthnAccountProfileWebflowConfigurer")
            final CasWebflowConfigurer webAuthnAccountProfileWebflowConfigurer) {
            return plan -> plan.registerWebflowConfigurer(webAuthnAccountProfileWebflowConfigurer);
        }

        @Bean
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        @ConditionalOnMissingBean(name = CasWebflowConstants.ACTION_ID_ACCOUNT_PROFILE_WEBAUTHN_MFA_DEVICE_PROVIDER)
        public MultifactorAuthenticationDeviceProviderAction webAuthnDeviceProviderAction(
            final ConfigurableApplicationContext applicationContext,
            @Qualifier(WebAuthnCredentialRepository.BEAN_NAME) final RegistrationStorage webAuthnCredentialRepository) {
            return BeanSupplier.of(MultifactorAuthenticationDeviceProviderAction.class)
                .when(CONDITION.given(applicationContext.getEnvironment()))
                .supply(() -> new WebAuthnMultifactorDeviceProviderAction(webAuthnCredentialRepository))
                .otherwiseProxy()
                .get();
        }

        @Bean
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        @ConditionalOnMissingBean(name = CasWebflowConstants.ACTION_ID_ACCOUNT_PROFILE_WEBAUTHN_MFA_PREPARE)
        public Action webAuthnAccountProfilePrepareAction(
            @Qualifier("webAuthnMultifactorAuthenticationProvider")
            final MultifactorAuthenticationProvider webAuthnMultifactorAuthenticationProvider,
            final CasConfigurationProperties casProperties,
            @Qualifier(WebAuthnCredentialRepository.BEAN_NAME) final RegistrationStorage webAuthnCredentialRepository) {
            return new WebAuthnMultifactorAccountProfilePrepareAction(webAuthnCredentialRepository,
                webAuthnMultifactorAuthenticationProvider, casProperties);
        }

        @Bean
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        @ConditionalOnMissingBean(name = CasWebflowConstants.ACTION_ID_ACCOUNT_PROFILE_WEBAUTHN_REGISTRATION)
        public Action webAuthnAccountProfileRegistrationAction(
            @Qualifier("webAuthnMultifactorAuthenticationProvider")
            final MultifactorAuthenticationProvider webAuthnMultifactorAuthenticationProvider) {
            return new WebAuthnMultifactorAccountProfileRegistrationAction(webAuthnMultifactorAuthenticationProvider);
        }

    }
}
