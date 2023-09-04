package org.apereo.cas.adaptors.authy.config;

import lombok.val;
import org.apereo.cas.adaptors.authy.core.AuthyClientInstance;
import org.apereo.cas.adaptors.authy.core.okta.OktaService;
import org.apereo.cas.adaptors.authy.web.flow.*;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.configuration.features.CasFeatureModule;
import org.apereo.cas.trusted.config.MultifactorAuthnTrustConfiguration;
import org.apereo.cas.util.spring.beans.BeanCondition;
import org.apereo.cas.util.spring.beans.BeanSupplier;
import org.apereo.cas.util.spring.boot.ConditionalOnFeatureEnabled;
import org.apereo.cas.web.flow.CasWebflowConfigurer;
import org.apereo.cas.web.flow.CasWebflowConstants;
import org.apereo.cas.web.flow.CasWebflowExecutionPlanConfigurer;
import org.apereo.cas.web.flow.actions.WebflowActionBeanSupplier;
import org.apereo.cas.web.flow.resolver.CasWebflowEventResolver;
import org.apereo.cas.web.flow.resolver.impl.CasWebflowEventResolutionConfigurationContext;
import org.apereo.cas.web.flow.util.MultifactorAuthenticationWebflowUtils;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.*;
import org.springframework.webflow.config.FlowDefinitionRegistryBuilder;
import org.springframework.webflow.definition.registry.FlowDefinitionRegistry;
import org.springframework.webflow.engine.builder.FlowBuilder;
import org.springframework.webflow.engine.builder.support.FlowBuilderServices;
import org.springframework.webflow.execution.Action;

/**
 * This is {@link AuthyConfiguration}.
 *
 * @author Jérémie POISSON
 */
@ConfigurationPropertiesScan
@EnableConfigurationProperties({CasConfigurationProperties.class, OktaMfaProperties.class})
@AutoConfiguration
public class AuthyConfiguration {
    private static final int WEBFLOW_CONFIGURER_ORDER = 100;

    private static final BeanCondition CONDITION = BeanCondition.on("okta.mfa.cas.token");

    @Bean
    @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
    @ConditionalOnMissingBean(name = "oktaProperties")
    public OktaMfaProperties oktaMfaProperties() {
        return new OktaMfaProperties();
    }

    @Bean
    @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
    @DependsOn("oktaMfaProperties")
    @ConditionalOnMissingBean(name = "oktaService")
    public OktaService oktaService(OktaMfaProperties oktaMfaProperties) {
        return new OktaService(oktaMfaProperties);
    }

    @Configuration(value = "AuthyWebflowRegistryConfiguration", proxyBeanMethods = false)
    @EnableConfigurationProperties({CasConfigurationProperties.class, OktaMfaProperties.class})
    public static class AuthyWebflowRegistryConfiguration {
        @Bean
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        @ConditionalOnMissingBean(name = "authyAuthenticatorFlowRegistry")
        public FlowDefinitionRegistry authyAuthenticatorFlowRegistry(
                final ConfigurableApplicationContext applicationContext,
                @Qualifier(CasWebflowConstants.BEAN_NAME_FLOW_BUILDER_SERVICES) final FlowBuilderServices flowBuilderServices,
                @Qualifier(CasWebflowConstants.BEAN_NAME_FLOW_BUILDER) final FlowBuilder flowBuilder) throws Exception {
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
    @EnableConfigurationProperties({CasConfigurationProperties.class, OktaMfaProperties.class})
    public static class AuthyWebflowCoreConfiguration {
        @ConditionalOnMissingBean(name = "authyMultifactorWebflowConfigurer")
        @Bean
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        public CasWebflowConfigurer authyMultifactorWebflowConfigurer(
                @Qualifier("authyAuthenticatorFlowRegistry") final FlowDefinitionRegistry authyAuthenticatorFlowRegistry,
                final ConfigurableApplicationContext applicationContext,
                final CasConfigurationProperties casProperties,
                @Qualifier(CasWebflowConstants.BEAN_NAME_LOGIN_FLOW_DEFINITION_REGISTRY) final FlowDefinitionRegistry loginFlowDefinitionRegistry,
                @Qualifier(CasWebflowConstants.BEAN_NAME_FLOW_BUILDER_SERVICES) final FlowBuilderServices flowBuilderServices) throws Exception {
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
    @EnableConfigurationProperties({CasConfigurationProperties.class, OktaMfaProperties.class})
    public static class AuthyWebflowEventConfiguration {
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        @Bean
        public CasWebflowEventResolver authyAuthenticationWebflowEventResolver(
                final ConfigurableApplicationContext applicationContext,
                @Qualifier("casWebflowConfigurationContext") final CasWebflowEventResolutionConfigurationContext casWebflowConfigurationContext) throws Exception {
            return BeanSupplier.of(CasWebflowEventResolver.class)
                    .when(CONDITION.given(applicationContext.getEnvironment()))
                    .supply(() -> new AuthyAuthenticationWebflowEventResolver(casWebflowConfigurationContext))
                    .otherwiseProxy()
                    .get();
        }

    }

    @Configuration(value = "AuthyWebflowActionConfiguration", proxyBeanMethods = false)
    @EnableConfigurationProperties({CasConfigurationProperties.class, OktaMfaProperties.class})
    public static class AuthyWebflowActionConfiguration {
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        @ConditionalOnMissingBean(name = CasWebflowConstants.ACTION_ID_AUTHY_REGISTRATION)
        @Bean
        public Action authyAuthenticationRegistrationWebflowAction(
                final CasConfigurationProperties casProperties,
                final ConfigurableApplicationContext applicationContext,
                @Qualifier("authyClientInstance") final AuthyClientInstance authyClientInstance) throws Exception {
            return WebflowActionBeanSupplier.builder()
                    .withApplicationContext(applicationContext)
                    .withProperties(casProperties)
                    .withAction(() -> BeanSupplier.of(Action.class)
                            .when(CONDITION.given(applicationContext.getEnvironment()))
                            .supply(() ->
                                    new AuthyAuthenticationRegistrationWebflowAction(authyClientInstance))
                            .otherwiseProxy()
                            .get())
                    .withId(CasWebflowConstants.ACTION_ID_AUTHY_REGISTRATION)
                    .build()
                    .get();
        }

        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        @Bean
        @ConditionalOnMissingBean(name = CasWebflowConstants.ACTION_ID_AUTHY_AUTHENTICATION)
        public Action authyAuthenticationWebflowAction(
                final CasConfigurationProperties casProperties,
                final ConfigurableApplicationContext applicationContext,
                @Qualifier("authyAuthenticationWebflowEventResolver") final CasWebflowEventResolver authyAuthenticationWebflowEventResolver) throws Exception {
            return WebflowActionBeanSupplier.builder()
                    .withApplicationContext(applicationContext)
                    .withProperties(casProperties)
                    .withAction(() -> BeanSupplier.of(Action.class)
                            .when(CONDITION.given(applicationContext.getEnvironment()))
                            .supply(() -> new AuthyAuthenticationWebflowAction(authyAuthenticationWebflowEventResolver))
                            .otherwiseProxy()
                            .get())
                    .withId(CasWebflowConstants.ACTION_ID_AUTHY_AUTHENTICATION)
                    .build()
                    .get();
        }

        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        @ConditionalOnMissingBean(name = "userCheckAction")
        @Bean
        public Action userCheckAction(OktaService oktaService) {
            return new UserCheckAction(oktaService);
        }

        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        @ConditionalOnMissingBean(name = "phoneEnrollmentAction")
        @Bean
        public Action phoneEnrollmentAction(OktaService oktaService) {
            return new PhoneEnrollmentAction(oktaService);
        }

        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        @ConditionalOnMissingBean(name = "emailEnrollmentAction")
        @Bean
        public Action emailEnrollmentAction(OktaService oktaService) {
            return new EmailEnrollmentAction(oktaService);
        }

        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        @ConditionalOnMissingBean(name = "totpEnrollViewAction")
        @Bean
        public Action totpEnrollViewAction(OktaService oktaService) {
            return new TotpEnrollViewAction(oktaService);
        }

        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        @ConditionalOnMissingBean(name = "totpEnrollAction")
        @Bean
        public Action totpEnrollAction(OktaService oktaService) {
            return new TotpEnrollAction(oktaService);
        }

        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        @ConditionalOnMissingBean(name = "selectMfaAction")
        @Bean
        public Action selectMfaAction(OktaService oktaService) {
            return new SelectMfaAction(oktaService);
        }

        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        @ConditionalOnMissingBean(name = "resendCodeAction")
        @Bean
        public Action resendCodeAction(OktaService oktaService) {
            return new ResendCodeAction(oktaService);
        }

        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        @ConditionalOnMissingBean(name = "verifyFactorAction")
        @Bean
        public Action verifyFactorAction(OktaService oktaService) {
            return new VerifyFactorAction(oktaService);
        }

        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        @ConditionalOnMissingBean(name = "pushValidationAction")
        @Bean
        public Action pushValidationAction(OktaService oktaService) {
            return new PushValidationAction(oktaService);
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
                @Qualifier("authyMultifactorWebflowConfigurer") final CasWebflowConfigurer authyMultifactorWebflowConfigurer) throws Exception {
            return BeanSupplier.of(CasWebflowExecutionPlanConfigurer.class)
                    .when(CONDITION.given(applicationContext.getEnvironment()))
                    .supply(() -> plan -> plan.registerWebflowConfigurer(authyMultifactorWebflowConfigurer))
                    .otherwiseProxy()
                    .get();
        }
    }

    @ConditionalOnClass(MultifactorAuthnTrustConfiguration.class)
    @Configuration(value = "AuthyMultifactorTrustConfiguration", proxyBeanMethods = false)
    @DependsOn("authyMultifactorWebflowConfigurer")
    @ConditionalOnFeatureEnabled(feature = CasFeatureModule.FeatureCatalog.MultifactorAuthenticationTrustedDevices, module = "authy")
    public static class AuthyMultifactorTrustConfiguration {
        private static final BeanCondition CONDITION = BeanCondition.on("cas.authn.mfa.authy.trusted-device-enabled")
                .isTrue().evenIfMissing();

        @ConditionalOnMissingBean(name = "authyMultifactorTrustWebflowConfigurer")
        @Bean
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        public CasWebflowConfigurer authyMultifactorTrustWebflowConfigurer(
                @Qualifier("authyAuthenticatorFlowRegistry") final FlowDefinitionRegistry authyAuthenticatorFlowRegistry,
                final ConfigurableApplicationContext applicationContext,
                final CasConfigurationProperties casProperties,
                @Qualifier(CasWebflowConstants.BEAN_NAME_LOGIN_FLOW_DEFINITION_REGISTRY) final FlowDefinitionRegistry loginFlowDefinitionRegistry,
                @Qualifier(CasWebflowConstants.BEAN_NAME_FLOW_BUILDER_SERVICES) final FlowBuilderServices flowBuilderServices) {
            return BeanSupplier.of(CasWebflowConfigurer.class)
                    .when(AuthyConfiguration.CONDITION.given(applicationContext.getEnvironment()))
                    .and(AuthyMultifactorTrustConfiguration.CONDITION.given(applicationContext.getEnvironment()))
                    .supply(() -> {
                        val cfg = new AuthyMultifactorTrustedDeviceWebflowConfigurer(flowBuilderServices,
                                loginFlowDefinitionRegistry,
                                authyAuthenticatorFlowRegistry,
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
        @ConditionalOnMissingBean(name = "authyMultifactorTrustCasWebflowExecutionPlanConfigurer")
        public CasWebflowExecutionPlanConfigurer authyMultifactorTrustCasWebflowExecutionPlanConfigurer(
                final ConfigurableApplicationContext applicationContext,
                @Qualifier("authyMultifactorTrustWebflowConfigurer") final CasWebflowConfigurer authyMultifactorTrustWebflowConfigurer) {
            return BeanSupplier.of(CasWebflowExecutionPlanConfigurer.class)
                    .when(AuthyConfiguration.CONDITION.given(applicationContext.getEnvironment()))
                    .and(AuthyMultifactorTrustConfiguration.CONDITION.given(applicationContext.getEnvironment()))
                    .supply(() -> plan -> plan.registerWebflowConfigurer(authyMultifactorTrustWebflowConfigurer))
                    .otherwiseProxy()
                    .get();
        }
    }
}
