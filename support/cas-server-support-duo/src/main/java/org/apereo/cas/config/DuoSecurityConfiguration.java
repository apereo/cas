package org.apereo.cas.config;

import org.apereo.cas.adaptors.duo.authn.DuoSecurityAuthenticationService;
import org.apereo.cas.adaptors.duo.web.flow.DuoSecurityAuthenticationWebflowEventResolver;
import org.apereo.cas.adaptors.duo.web.flow.action.DuoSecurityAuthenticationWebflowAction;
import org.apereo.cas.adaptors.duo.web.flow.action.DuoSecurityDirectAuthenticationAction;
import org.apereo.cas.adaptors.duo.web.flow.action.DuoSecurityMultifactorAuthenticationDeviceProviderAction;
import org.apereo.cas.adaptors.duo.web.flow.action.DuoSecurityUniversalPromptPrepareLoginAction;
import org.apereo.cas.adaptors.duo.web.flow.action.DuoSecurityUniversalPromptValidateLoginAction;
import org.apereo.cas.authentication.AuthenticationSystemSupport;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.configuration.features.CasFeatureModule;
import org.apereo.cas.pac4j.BrowserWebStorageSessionStore;
import org.apereo.cas.util.crypto.CipherExecutor;
import org.apereo.cas.util.spring.beans.BeanSupplier;
import org.apereo.cas.util.spring.boot.ConditionalOnFeatureEnabled;
import org.apereo.cas.web.flow.CasWebflowConfigurer;
import org.apereo.cas.web.flow.CasWebflowConstants;
import org.apereo.cas.web.flow.CasWebflowExecutionPlanConfigurer;
import org.apereo.cas.web.flow.actions.MultifactorAuthenticationDeviceProviderAction;
import org.apereo.cas.web.flow.actions.WebflowActionBeanSupplier;
import org.apereo.cas.web.flow.configurer.MultifactorAuthenticationAccountProfileWebflowConfigurer;
import org.apereo.cas.web.flow.resolver.CasWebflowEventResolver;
import org.apereo.cas.web.flow.resolver.impl.CasWebflowEventResolutionConfigurationContext;
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
 * This is {@link DuoSecurityConfiguration}.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 */
@EnableConfigurationProperties(CasConfigurationProperties.class)
@ConditionalOnFeatureEnabled(feature = CasFeatureModule.FeatureCatalog.MultifactorAuthentication, module = "duo")
@Configuration(value = "DuoSecurityConfiguration", proxyBeanMethods = false)
class DuoSecurityConfiguration {
    @Configuration(value = "DuoSecurityCoreWebflowConfiguration", proxyBeanMethods = false)
    @EnableConfigurationProperties(CasConfigurationProperties.class)
    static class DuoSecurityCoreWebflowConfiguration {

        @ConditionalOnMissingBean(name = "duoUniversalPromptSessionStore")
        @Bean
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        public BrowserWebStorageSessionStore duoUniversalPromptSessionStore(@Qualifier("webflowCipherExecutor")
                                                                            final CipherExecutor webflowCipherExecutor) {
            return new BrowserWebStorageSessionStore(webflowCipherExecutor, "DuoSecuritySessionContext");
        }

        @ConditionalOnMissingBean(name = CasWebflowConstants.ACTION_ID_DUO_NON_WEB_AUTHENTICATION)
        @Bean
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        public Action duoNonWebAuthenticationAction(
            final CasConfigurationProperties casProperties,
            final ConfigurableApplicationContext applicationContext) {
            return WebflowActionBeanSupplier.builder()
                .withApplicationContext(applicationContext)
                .withProperties(casProperties)
                .withAction(() -> BeanSupplier.of(Action.class)
                    .when(DuoSecurityAuthenticationService.CONDITION.given(applicationContext.getEnvironment()))
                    .supply(DuoSecurityDirectAuthenticationAction::new)
                    .otherwiseProxy()
                    .get())
                .withId(CasWebflowConstants.ACTION_ID_DUO_NON_WEB_AUTHENTICATION)
                .build()
                .get();
        }

        @ConditionalOnMissingBean(name = CasWebflowConstants.ACTION_ID_DUO_AUTHENTICATION_WEBFLOW)
        @Bean
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        public Action duoAuthenticationWebflowAction(
            final CasConfigurationProperties casProperties,
            final ConfigurableApplicationContext applicationContext,
            @Qualifier("duoAuthenticationWebflowEventResolver")
            final CasWebflowEventResolver duoAuthenticationWebflowEventResolver) {
            return WebflowActionBeanSupplier.builder()
                .withApplicationContext(applicationContext)
                .withProperties(casProperties)
                .withAction(() -> new DuoSecurityAuthenticationWebflowAction(duoAuthenticationWebflowEventResolver))
                .withId(CasWebflowConstants.ACTION_ID_DUO_AUTHENTICATION_WEBFLOW)
                .build()
                .get();
        }

        @ConditionalOnMissingBean(name = CasWebflowConstants.ACTION_ID_DUO_UNIVERSAL_PROMPT_PREPARE_LOGIN)
        @Bean
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        public Action duoUniversalPromptPrepareLoginAction(
            @Qualifier("duoUniversalPromptSessionStore")
            final BrowserWebStorageSessionStore duoUniversalPromptSessionStore,
            final CasConfigurationProperties casProperties,
            final ConfigurableApplicationContext applicationContext) {
            return WebflowActionBeanSupplier.builder()
                .withApplicationContext(applicationContext)
                .withProperties(casProperties)
                .withAction(() -> BeanSupplier.of(Action.class)
                    .when(DuoSecurityAuthenticationService.CONDITION.given(applicationContext.getEnvironment()))
                    .supply(() -> new DuoSecurityUniversalPromptPrepareLoginAction(applicationContext, duoUniversalPromptSessionStore))
                    .otherwiseProxy()
                    .get())
                .withId(CasWebflowConstants.ACTION_ID_DUO_UNIVERSAL_PROMPT_PREPARE_LOGIN)
                .build()
                .get();
        }

        @ConditionalOnMissingBean(name = CasWebflowConstants.ACTION_ID_DUO_UNIVERSAL_PROMPT_VALIDATE_LOGIN)
        @Bean
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        public Action duoUniversalPromptValidateLoginAction(
            final CasConfigurationProperties casProperties,
            final ConfigurableApplicationContext applicationContext,
            @Qualifier("duoAuthenticationWebflowEventResolver")
            final CasWebflowEventResolver duoAuthenticationWebflowEventResolver,
            @Qualifier("duoUniversalPromptSessionStore")
            final BrowserWebStorageSessionStore duoUniversalPromptSessionStore,
            @Qualifier(AuthenticationSystemSupport.BEAN_NAME)
            final AuthenticationSystemSupport authenticationSystemSupport) {
            return WebflowActionBeanSupplier.builder()
                .withApplicationContext(applicationContext)
                .withProperties(casProperties)
                .withAction(() -> BeanSupplier.of(Action.class)
                    .when(DuoSecurityAuthenticationService.CONDITION.given(applicationContext.getEnvironment()))
                    .supply(() -> new DuoSecurityUniversalPromptValidateLoginAction(
                        duoAuthenticationWebflowEventResolver, duoUniversalPromptSessionStore,
                        applicationContext, authenticationSystemSupport))
                    .otherwiseProxy()
                    .get())
                .withId(CasWebflowConstants.ACTION_ID_DUO_UNIVERSAL_PROMPT_VALIDATE_LOGIN)
                .build()
                .get();
        }

        @ConditionalOnMissingBean(name = "duoAuthenticationWebflowEventResolver")
        @Bean
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        public CasWebflowEventResolver duoAuthenticationWebflowEventResolver(
            final ConfigurableApplicationContext applicationContext,
            @Qualifier("casWebflowConfigurationContext")
            final CasWebflowEventResolutionConfigurationContext casWebflowConfigurationContext) {
            return BeanSupplier.of(CasWebflowEventResolver.class)
                .when(DuoSecurityAuthenticationService.CONDITION.given(applicationContext.getEnvironment()))
                .supply(() -> new DuoSecurityAuthenticationWebflowEventResolver(casWebflowConfigurationContext))
                .otherwiseProxy()
                .get();
        }
    }

    @Configuration(value = "DuoSecurityAccountProfileWebflowConfiguration", proxyBeanMethods = false)
    @EnableConfigurationProperties(CasConfigurationProperties.class)
    @ConditionalOnFeatureEnabled(feature = CasFeatureModule.FeatureCatalog.AccountManagement, enabledByDefault = false)
    @AutoConfigureOrder(Ordered.LOWEST_PRECEDENCE)
    static class DuoSecurityAccountProfileWebflowConfiguration {
        @ConditionalOnMissingBean(name = "duoMultifactorAuthenticationDeviceProviderAction")
        @Bean
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        public MultifactorAuthenticationDeviceProviderAction duoMultifactorAuthenticationDeviceProviderAction(
            final ConfigurableApplicationContext applicationContext) {
            return BeanSupplier.of(MultifactorAuthenticationDeviceProviderAction.class)
                .when(DuoSecurityAuthenticationService.CONDITION.given(applicationContext.getEnvironment()))
                .supply(() -> new DuoSecurityMultifactorAuthenticationDeviceProviderAction(applicationContext))
                .otherwiseProxy()
                .get();
        }

        @ConditionalOnMissingBean(name = "duoMultifactorAccountProfileWebflowConfigurer")
        @Bean
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        public CasWebflowConfigurer duoMultifactorAccountProfileWebflowConfigurer(
            final CasConfigurationProperties casProperties,
            final ConfigurableApplicationContext applicationContext,
            @Qualifier(CasWebflowConstants.BEAN_NAME_ACCOUNT_PROFILE_FLOW_DEFINITION_REGISTRY)
            final FlowDefinitionRegistry accountProfileFlowRegistry,
            @Qualifier(CasWebflowConstants.BEAN_NAME_FLOW_BUILDER_SERVICES)
            final FlowBuilderServices flowBuilderServices) {
            return BeanSupplier.of(CasWebflowConfigurer.class)
                .when(DuoSecurityAuthenticationService.CONDITION.given(applicationContext.getEnvironment()))
                .supply(() -> new MultifactorAuthenticationAccountProfileWebflowConfigurer(flowBuilderServices,
                    accountProfileFlowRegistry, applicationContext, casProperties))
                .otherwiseProxy()
                .get();
        }

        @Bean
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        @ConditionalOnMissingBean(name = "duoSecurityAccountProfileWebflowExecutionPlanConfigurer")
        public CasWebflowExecutionPlanConfigurer duoSecurityAccountProfileWebflowExecutionPlanConfigurer(
            @Qualifier("duoMultifactorAccountProfileWebflowConfigurer")
            final CasWebflowConfigurer duoMultifactorAccountProfileWebflowConfigurer,
            final ConfigurableApplicationContext applicationContext) {
            return BeanSupplier.of(CasWebflowExecutionPlanConfigurer.class)
                .when(DuoSecurityAuthenticationService.CONDITION.given(applicationContext.getEnvironment()))
                .supply(() -> plan -> plan.registerWebflowConfigurer(duoMultifactorAccountProfileWebflowConfigurer))
                .otherwiseProxy()
                .get();
        }

    }
}
