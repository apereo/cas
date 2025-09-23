package org.apereo.cas.config;

import org.apereo.cas.authentication.MultifactorAuthenticationProvider;
import org.apereo.cas.authentication.device.MultifactorAuthenticationDeviceManager;
import org.apereo.cas.authentication.principal.PrincipalFactory;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.configuration.features.CasFeatureModule;
import org.apereo.cas.multitenancy.TenantExtractor;
import org.apereo.cas.ticket.TicketFactory;
import org.apereo.cas.ticket.registry.TicketRegistry;
import org.apereo.cas.trusted.web.flow.BasicMultifactorTrustedWebflowConfigurer;
import org.apereo.cas.util.spring.beans.BeanCondition;
import org.apereo.cas.util.spring.beans.BeanSupplier;
import org.apereo.cas.util.spring.boot.ConditionalOnFeatureEnabled;
import org.apereo.cas.web.flow.CasWebflowConfigurer;
import org.apereo.cas.web.flow.CasWebflowConstants;
import org.apereo.cas.web.flow.CasWebflowExecutionPlanConfigurer;
import org.apereo.cas.web.flow.actions.ConsumerExecutionAction;
import org.apereo.cas.web.flow.actions.DefaultMultifactorAuthenticationDeviceProviderAction;
import org.apereo.cas.web.flow.actions.MultifactorAuthenticationDeviceProviderAction;
import org.apereo.cas.web.flow.actions.WebflowActionBeanSupplier;
import org.apereo.cas.web.flow.authentication.FinalMultifactorAuthenticationTransactionWebflowEventResolver;
import org.apereo.cas.web.flow.resolver.CasWebflowEventResolver;
import org.apereo.cas.web.flow.resolver.impl.CasWebflowEventResolutionConfigurationContext;
import org.apereo.cas.web.flow.util.MultifactorAuthenticationWebflowUtils;
import org.apereo.cas.webauthn.storage.WebAuthnCredentialRepository;
import org.apereo.cas.webauthn.web.flow.WebAuthnAccountCheckRegistrationAction;
import org.apereo.cas.webauthn.web.flow.WebAuthnAccountSaveRegistrationAction;
import org.apereo.cas.webauthn.web.flow.WebAuthnAuthenticationWebflowAction;
import org.apereo.cas.webauthn.web.flow.WebAuthnMultifactorWebflowConfigurer;
import org.apereo.cas.webauthn.web.flow.WebAuthnPopulateCsrfTokenAction;
import org.apereo.cas.webauthn.web.flow.WebAuthnStartAuthenticationAction;
import org.apereo.cas.webauthn.web.flow.WebAuthnStartRegistrationAction;
import org.apereo.cas.webauthn.web.flow.WebAuthnValidateSessionCredentialTokenAction;
import org.apereo.cas.webauthn.web.flow.account.WebAuthnMultifactorAccountProfilePrepareAction;
import org.apereo.cas.webauthn.web.flow.account.WebAuthnMultifactorAccountProfileRegistrationAction;
import org.apereo.cas.webauthn.web.flow.account.WebAuthnMultifactorAccountProfileWebflowConfigurer;
import com.yubico.core.RegistrationStorage;
import com.yubico.core.SessionManager;
import lombok.val;
import org.springframework.beans.factory.annotation.Qualifier;
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
@Configuration(value = "WebAuthnWebflowConfiguration", proxyBeanMethods = false)
class WebAuthnWebflowConfiguration {
    private static final int WEBFLOW_CONFIGURER_ORDER = 100;

    private static final BeanCondition CONDITION = BeanCondition.on("cas.authn.mfa.web-authn.core.enabled")
        .isTrue().evenIfMissing();

    @Configuration(value = "WebAuthnWebflowRegistryConfiguration", proxyBeanMethods = false)
    @EnableConfigurationProperties(CasConfigurationProperties.class)
    static class WebAuthnWebflowRegistryConfiguration {
        @Bean
        @ConditionalOnMissingBean(name = "webAuthnFlowRegistry")
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        public FlowDefinitionRegistry webAuthnFlowRegistry(
            @Qualifier(CasWebflowConstants.BEAN_NAME_FLOW_BUILDER_SERVICES)
            final FlowBuilderServices flowBuilderServices,
            @Qualifier(CasWebflowConstants.BEAN_NAME_FLOW_BUILDER)
            final FlowBuilder flowBuilder,
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
    static class WebAuthnWebflowBaseConfiguration {
        @ConditionalOnMissingBean(name = "webAuthnMultifactorWebflowConfigurer")
        @Bean
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        public CasWebflowConfigurer webAuthnMultifactorWebflowConfigurer(
            @Qualifier(CasWebflowConstants.BEAN_NAME_FLOW_DEFINITION_REGISTRY)
            final FlowDefinitionRegistry flowDefinitionRegistry,
            @Qualifier(CasWebflowConstants.BEAN_NAME_FLOW_BUILDER_SERVICES)
            final FlowBuilderServices flowBuilderServices,
            @Qualifier("webAuthnFlowRegistry")
            final FlowDefinitionRegistry webAuthnFlowRegistry,
            final ConfigurableApplicationContext applicationContext,
            final CasConfigurationProperties casProperties) {
            return BeanSupplier.of(CasWebflowConfigurer.class)
                .when(CONDITION.given(applicationContext.getEnvironment()))
                .supply(() -> {
                    val cfg = new WebAuthnMultifactorWebflowConfigurer(flowBuilderServices,
                        flowDefinitionRegistry, webAuthnFlowRegistry,
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
    static class WebAuthnWebflowEventResolutionConfiguration {
        @ConditionalOnMissingBean(name = "webAuthnAuthenticationWebflowEventResolver")
        @Bean
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        public CasWebflowEventResolver webAuthnAuthenticationWebflowEventResolver(
            final ConfigurableApplicationContext applicationContext,
            @Qualifier(CasWebflowEventResolutionConfigurationContext.BEAN_NAME)
            final CasWebflowEventResolutionConfigurationContext casWebflowConfigurationContext) {
            return BeanSupplier.of(CasWebflowEventResolver.class)
                .when(CONDITION.given(applicationContext.getEnvironment()))
                .supply(() -> new FinalMultifactorAuthenticationTransactionWebflowEventResolver(casWebflowConfigurationContext))
                .otherwiseProxy()
                .get();
        }

    }

    @Configuration(value = "WebAuthnWebflowExecutionPlanConfiguration", proxyBeanMethods = false)
    @EnableConfigurationProperties(CasConfigurationProperties.class)
    static class WebAuthnWebflowExecutionPlanConfiguration {
        @ConditionalOnMissingBean(name = "webAuthnCasWebflowExecutionPlanConfigurer")
        @Bean
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        public CasWebflowExecutionPlanConfigurer webAuthnCasWebflowExecutionPlanConfigurer(
            final ConfigurableApplicationContext applicationContext,
            @Qualifier("webAuthnMultifactorWebflowConfigurer")
            final CasWebflowConfigurer webAuthnMultifactorWebflowConfigurer) {
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
    static class WebAuthnMultifactorTrustConfiguration {
        private static final BeanCondition CONDITION = BeanCondition.on("cas.authn.mfa.web-authn.trusted-device-enabled")
            .isTrue().evenIfMissing();

        @ConditionalOnMissingBean(name = "webAuthnMultifactorTrustWebflowConfigurer")
        @Bean
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        public CasWebflowConfigurer webAuthnMultifactorTrustWebflowConfigurer(
            @Qualifier("webAuthnFlowRegistry")
            final FlowDefinitionRegistry webAuthnFlowRegistry,
            @Qualifier(CasWebflowConstants.BEAN_NAME_FLOW_DEFINITION_REGISTRY)
            final FlowDefinitionRegistry flowDefinitionRegistry,
            @Qualifier(CasWebflowConstants.BEAN_NAME_FLOW_BUILDER_SERVICES)
            final FlowBuilderServices flowBuilderServices,
            final ConfigurableApplicationContext applicationContext,
            final CasConfigurationProperties casProperties) {
            return BeanSupplier.of(CasWebflowConfigurer.class)
                .when(WebAuthnWebflowConfiguration.CONDITION.given(applicationContext.getEnvironment()))
                .and(WebAuthnMultifactorTrustConfiguration.CONDITION.given(applicationContext.getEnvironment()))
                .supply(() -> {
                    val cfg = new BasicMultifactorTrustedWebflowConfigurer(
                        flowBuilderServices,
                        flowDefinitionRegistry,
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
            @Qualifier("webAuthnMultifactorTrustWebflowConfigurer")
            final CasWebflowConfigurer webAuthnMultifactorTrustWebflowConfigurer) {
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
    static class WebAuthnWebflowActionConfiguration {

        @ConditionalOnMissingBean(name = CasWebflowConstants.ACTION_ID_WEBAUTHN_POPULATE_CSRF_TOKEN)
        @Bean
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        public Action webAuthnPopulateCsrfTokenAction(
            @Qualifier("webAuthnCsrfTokenRepository")
            final CsrfTokenRepository webAuthnCsrfTokenRepository,
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
            final CasConfigurationProperties casProperties,
            @Qualifier(TenantExtractor.BEAN_NAME)
            final TenantExtractor tenantExtractor,
            @Qualifier(TicketFactory.BEAN_NAME)
            final TicketFactory ticketFactory,
            @Qualifier(TicketRegistry.BEAN_NAME)
            final TicketRegistry ticketRegistry,
            final ConfigurableApplicationContext applicationContext,
            @Qualifier(WebAuthnCredentialRepository.BEAN_NAME)
            final RegistrationStorage webAuthnCredentialRepository) {
            return BeanSupplier.of(Action.class)
                .when(CONDITION.given(applicationContext.getEnvironment()))
                .supply(() -> new WebAuthnStartAuthenticationAction(casProperties,
                    ticketRegistry, ticketFactory, webAuthnCredentialRepository, tenantExtractor))
                .otherwise(() -> ConsumerExecutionAction.NONE)
                .get();
        }

        @ConditionalOnMissingBean(name = CasWebflowConstants.ACTION_ID_WEB_AUTHN_START_REGISTRATION)
        @Bean
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        public Action webAuthnStartRegistrationAction(
            @Qualifier(TenantExtractor.BEAN_NAME)
            final TenantExtractor tenantExtractor,
            final ConfigurableApplicationContext applicationContext,
            final CasConfigurationProperties casProperties) {
            return BeanSupplier.of(Action.class)
                .when(CONDITION.given(applicationContext.getEnvironment()))
                .supply(() -> new WebAuthnStartRegistrationAction(casProperties, tenantExtractor))
                .otherwise(() -> ConsumerExecutionAction.NONE)
                .get();
        }

        @ConditionalOnMissingBean(name = CasWebflowConstants.ACTION_ID_WEBAUTHN_CHECK_ACCOUNT_REGISTRATION)
        @Bean
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        public Action webAuthnCheckAccountRegistrationAction(
            @Qualifier(TenantExtractor.BEAN_NAME)
            final TenantExtractor tenantExtractor,
            final ConfigurableApplicationContext applicationContext,
            @Qualifier(WebAuthnCredentialRepository.BEAN_NAME)
            final RegistrationStorage webAuthnCredentialRepository) {
            return BeanSupplier.of(Action.class)
                .when(CONDITION.given(applicationContext.getEnvironment()))
                .supply(() -> new WebAuthnAccountCheckRegistrationAction(webAuthnCredentialRepository, tenantExtractor))
                .otherwise(() -> ConsumerExecutionAction.NONE)
                .get();
        }

        @ConditionalOnMissingBean(name = CasWebflowConstants.ACTION_ID_WEBAUTHN_SAVE_ACCOUNT_REGISTRATION)
        @Bean
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        public Action webAuthnSaveAccountRegistrationAction(
            @Qualifier(TenantExtractor.BEAN_NAME)
            final TenantExtractor tenantExtractor,
            final ConfigurableApplicationContext applicationContext,
            @Qualifier(SessionManager.BEAN_NAME)
            final SessionManager webAuthnSessionManager,
            @Qualifier(WebAuthnCredentialRepository.BEAN_NAME)
            final RegistrationStorage webAuthnCredentialRepository) {
            return BeanSupplier.of(Action.class)
                .when(CONDITION.given(applicationContext.getEnvironment()))
                .supply(() -> new WebAuthnAccountSaveRegistrationAction(
                    webAuthnCredentialRepository, webAuthnSessionManager, tenantExtractor))
                .otherwise(() -> ConsumerExecutionAction.NONE)
                .get();
        }

        @ConditionalOnMissingBean(name = CasWebflowConstants.ACTION_ID_WEBAUTHN_AUTHENTICATION_WEBFLOW)
        @Bean
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        public Action webAuthnAuthenticationWebflowAction(
            final ConfigurableApplicationContext applicationContext,
            @Qualifier("webAuthnAuthenticationWebflowEventResolver")
            final CasWebflowEventResolver webAuthnAuthenticationWebflowEventResolver) {
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
            @Qualifier(TenantExtractor.BEAN_NAME)
            final TenantExtractor tenantExtractor,
            final ConfigurableApplicationContext applicationContext,
            @Qualifier(SessionManager.BEAN_NAME)
            final SessionManager webAuthnSessionManager,
            @Qualifier("webAuthnPrincipalFactory")
            final PrincipalFactory webAuthnPrincipalFactory,
            @Qualifier(WebAuthnCredentialRepository.BEAN_NAME)
            final RegistrationStorage webAuthnCredentialRepository) {
            return BeanSupplier.of(Action.class)
                .when(CONDITION.given(applicationContext.getEnvironment()))
                .supply(() -> new WebAuthnValidateSessionCredentialTokenAction(webAuthnCredentialRepository,
                    webAuthnSessionManager, webAuthnPrincipalFactory, tenantExtractor))
                .otherwise(() -> ConsumerExecutionAction.NONE)
                .get();
        }
    }

    @Configuration(value = "WebAuthnAccountProfileWebflowConfiguration", proxyBeanMethods = false)
    @EnableConfigurationProperties(CasConfigurationProperties.class)
    @ConditionalOnFeatureEnabled(feature = CasFeatureModule.FeatureCatalog.AccountManagement, enabledByDefault = false)
    @AutoConfigureOrder(Ordered.LOWEST_PRECEDENCE)
    static class WebAuthnAccountProfileWebflowConfiguration {
        @ConditionalOnMissingBean(name = "webAuthnAccountProfileWebflowConfigurer")
        @Bean
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        public CasWebflowConfigurer webAuthnAccountProfileWebflowConfigurer(
            final CasConfigurationProperties casProperties,
            final ConfigurableApplicationContext applicationContext,
            @Qualifier(CasWebflowConstants.BEAN_NAME_FLOW_DEFINITION_REGISTRY)
            final FlowDefinitionRegistry flowDefinitionRegistry,
            @Qualifier(CasWebflowConstants.BEAN_NAME_FLOW_BUILDER_SERVICES)
            final FlowBuilderServices flowBuilderServices) {
            return BeanSupplier.of(CasWebflowConfigurer.class)
                .when(CONDITION.given(applicationContext.getEnvironment()))
                .supply(() -> new WebAuthnMultifactorAccountProfileWebflowConfigurer(flowBuilderServices,
                    flowDefinitionRegistry, applicationContext, casProperties))
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
        @ConditionalOnMissingBean(name = "webAuthnDeviceProviderAction")
        public MultifactorAuthenticationDeviceProviderAction webAuthnDeviceProviderAction(
            final ConfigurableApplicationContext applicationContext,
            @Qualifier("webAuthnMultifactorAuthenticationDeviceManager")
            final MultifactorAuthenticationDeviceManager webAuthnMultifactorAuthenticationDeviceManager) {
            return BeanSupplier.of(MultifactorAuthenticationDeviceProviderAction.class)
                .when(CONDITION.given(applicationContext.getEnvironment()))
                .supply(() -> new DefaultMultifactorAuthenticationDeviceProviderAction(webAuthnMultifactorAuthenticationDeviceManager))
                .otherwiseProxy()
                .get();
        }

        @Bean
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        @ConditionalOnMissingBean(name = CasWebflowConstants.ACTION_ID_ACCOUNT_PROFILE_WEBAUTHN_MFA_PREPARE)
        public Action webAuthnAccountProfilePrepareAction(
            final ConfigurableApplicationContext applicationContext,
            @Qualifier("webAuthnMultifactorAuthenticationProvider")
            final MultifactorAuthenticationProvider webAuthnMultifactorAuthenticationProvider,
            final CasConfigurationProperties casProperties,
            @Qualifier(WebAuthnCredentialRepository.BEAN_NAME)
            final RegistrationStorage webAuthnCredentialRepository) {
            return WebflowActionBeanSupplier.builder()
                .withApplicationContext(applicationContext)
                .withProperties(casProperties)
                .withAction(() -> new WebAuthnMultifactorAccountProfilePrepareAction(webAuthnCredentialRepository,
                            webAuthnMultifactorAuthenticationProvider, casProperties))
                .withId(CasWebflowConstants.ACTION_ID_ACCOUNT_PROFILE_WEBAUTHN_MFA_PREPARE)
                .build()
                .get();
        }

        @Bean
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        @ConditionalOnMissingBean(name = CasWebflowConstants.ACTION_ID_ACCOUNT_PROFILE_WEBAUTHN_REGISTRATION)
        public Action webAuthnAccountProfileRegistrationAction(
            final ConfigurableApplicationContext applicationContext,
            final CasConfigurationProperties casProperties,
            @Qualifier("webAuthnMultifactorAuthenticationProvider")
            final MultifactorAuthenticationProvider webAuthnMultifactorAuthenticationProvider) {
            return WebflowActionBeanSupplier.builder()
                .withApplicationContext(applicationContext)
                .withProperties(casProperties)
                .withAction(() -> new WebAuthnMultifactorAccountProfileRegistrationAction(webAuthnMultifactorAuthenticationProvider))
                .withId(CasWebflowConstants.ACTION_ID_ACCOUNT_PROFILE_WEBAUTHN_REGISTRATION)
                .build()
                .get();
        }

    }
}
