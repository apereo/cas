package org.apereo.cas.config;

import org.apereo.cas.authentication.principal.PrincipalFactory;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.trusted.config.ConditionalOnMultifactorTrustedDevicesEnabled;
import org.apereo.cas.trusted.config.MultifactorAuthnTrustConfiguration;
import org.apereo.cas.web.flow.CasWebflowConfigurer;
import org.apereo.cas.web.flow.CasWebflowConstants;
import org.apereo.cas.web.flow.CasWebflowExecutionPlanConfigurer;
import org.apereo.cas.web.flow.resolver.CasWebflowEventResolver;
import org.apereo.cas.web.flow.resolver.impl.CasWebflowEventResolutionConfigurationContext;
import org.apereo.cas.web.flow.util.MultifactorAuthenticationWebflowUtils;
import org.apereo.cas.webauthn.web.flow.WebAuthnAccountCheckRegistrationAction;
import org.apereo.cas.webauthn.web.flow.WebAuthnAccountSaveRegistrationAction;
import org.apereo.cas.webauthn.web.flow.WebAuthnAuthenticationWebflowAction;
import org.apereo.cas.webauthn.web.flow.WebAuthnAuthenticationWebflowEventResolver;
import org.apereo.cas.webauthn.web.flow.WebAuthnMultifactorTrustWebflowConfigurer;
import org.apereo.cas.webauthn.web.flow.WebAuthnMultifactorWebflowConfigurer;
import org.apereo.cas.webauthn.web.flow.WebAuthnStartAuthenticationAction;
import org.apereo.cas.webauthn.web.flow.WebAuthnStartRegistrationAction;
import org.apereo.cas.webauthn.web.flow.WebAuthnValidateSessionCredentialTokenAction;

import com.yubico.core.RegistrationStorage;
import com.yubico.core.SessionManager;
import lombok.val;
import org.springframework.beans.factory.annotation.Autowired;
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
@Configuration(value = "webAuthnWebflowConfiguration", proxyBeanMethods = false)
@EnableConfigurationProperties(CasConfigurationProperties.class)
@ConditionalOnWebAuthnEnabled
public class WebAuthnWebflowConfiguration {
    private static final int WEBFLOW_CONFIGURER_ORDER = 100;

    @Configuration(value = "WebAuthnWebflowRegistryConfiguration", proxyBeanMethods = false)
    @EnableConfigurationProperties(CasConfigurationProperties.class)
    public static class WebAuthnWebflowRegistryConfiguration {
        @Bean
        @ConditionalOnMissingBean(name = "webAuthnFlowRegistry")
        @Autowired
        public FlowDefinitionRegistry webAuthnFlowRegistry(
            @Qualifier(CasWebflowConstants.BEAN_NAME_FLOW_BUILDER_SERVICES)
            final FlowBuilderServices flowBuilderServices,
            @Qualifier(CasWebflowConstants.BEAN_NAME_FLOW_BUILDER)
            final FlowBuilder flowBuilder,
            final ConfigurableApplicationContext applicationContext) {
            val builder = new FlowDefinitionRegistryBuilder(applicationContext, flowBuilderServices);
            builder.addFlowBuilder(flowBuilder, WebAuthnMultifactorWebflowConfigurer.MFA_WEB_AUTHN_EVENT_ID);
            return builder.build();
        }
    }

    @Configuration(value = "WebAuthnWebflowBaseConfiguration", proxyBeanMethods = false)
    @EnableConfigurationProperties(CasConfigurationProperties.class)
    public static class WebAuthnWebflowBaseConfiguration {
        @ConditionalOnMissingBean(name = "webAuthnMultifactorWebflowConfigurer")
        @Bean
        @Autowired
        public CasWebflowConfigurer webAuthnMultifactorWebflowConfigurer(
            @Qualifier(CasWebflowConstants.BEAN_NAME_LOGIN_FLOW_DEFINITION_REGISTRY)
            final FlowDefinitionRegistry loginFlowDefinitionRegistry,
            @Qualifier(CasWebflowConstants.BEAN_NAME_FLOW_BUILDER_SERVICES)
            final FlowBuilderServices flowBuilderServices,
            @Qualifier("webAuthnFlowRegistry")
            final FlowDefinitionRegistry webAuthnFlowRegistry,
            final ConfigurableApplicationContext applicationContext,
            final CasConfigurationProperties casProperties) {
            val cfg = new WebAuthnMultifactorWebflowConfigurer(flowBuilderServices,
                loginFlowDefinitionRegistry, webAuthnFlowRegistry,
                applicationContext, casProperties,
                MultifactorAuthenticationWebflowUtils.getMultifactorAuthenticationWebflowCustomizers(applicationContext));
            cfg.setOrder(WEBFLOW_CONFIGURER_ORDER);
            return cfg;
        }
    }

    @Configuration(value = "WebAuthnWebflowEventResolutionConfiguration", proxyBeanMethods = false)
    @EnableConfigurationProperties(CasConfigurationProperties.class)
    public static class WebAuthnWebflowEventResolutionConfiguration {
        @ConditionalOnMissingBean(name = "webAuthnAuthenticationWebflowEventResolver")
        @Bean
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        @Autowired
        public CasWebflowEventResolver webAuthnAuthenticationWebflowEventResolver(
            @Qualifier("casWebflowConfigurationContext")
            final CasWebflowEventResolutionConfigurationContext casWebflowConfigurationContext) {
            return new WebAuthnAuthenticationWebflowEventResolver(casWebflowConfigurationContext);
        }

    }

    @Configuration(value = "WebAuthnWebflowExecutionPlanConfiguration", proxyBeanMethods = false)
    @EnableConfigurationProperties(CasConfigurationProperties.class)
    public static class WebAuthnWebflowExecutionPlanConfiguration {

        @ConditionalOnMissingBean(name = "webAuthnCasWebflowExecutionPlanConfigurer")
        @Bean
        @Autowired
        public CasWebflowExecutionPlanConfigurer webAuthnCasWebflowExecutionPlanConfigurer(
            @Qualifier("webAuthnMultifactorWebflowConfigurer")
            final CasWebflowConfigurer webAuthnMultifactorWebflowConfigurer) {
            return plan -> plan.registerWebflowConfigurer(webAuthnMultifactorWebflowConfigurer);
        }

    }

    @ConditionalOnClass(value = MultifactorAuthnTrustConfiguration.class)
    @ConditionalOnWebAuthnEnabled
    @ConditionalOnMultifactorTrustedDevicesEnabled(prefix = "cas.authn.mfa.web-authn")
    @Configuration(value = "webAuthnMultifactorTrustConfiguration", proxyBeanMethods = false)
    @DependsOn("webAuthnMultifactorWebflowConfigurer")
    public static class WebAuthnMultifactorTrustConfiguration {

        @ConditionalOnMissingBean(name = "webAuthnMultifactorTrustWebflowConfigurer")
        @Bean
        @Autowired
        public CasWebflowConfigurer webAuthnMultifactorTrustWebflowConfigurer(
            @Qualifier("webAuthnFlowRegistry")
            final FlowDefinitionRegistry webAuthnFlowRegistry,
            @Qualifier(CasWebflowConstants.BEAN_NAME_LOGIN_FLOW_DEFINITION_REGISTRY)
            final FlowDefinitionRegistry loginFlowDefinitionRegistry,
            @Qualifier(CasWebflowConstants.BEAN_NAME_FLOW_BUILDER_SERVICES)
            final FlowBuilderServices flowBuilderServices,
            final ConfigurableApplicationContext applicationContext,
            final CasConfigurationProperties casProperties) {
            val cfg = new WebAuthnMultifactorTrustWebflowConfigurer(
                flowBuilderServices,
                loginFlowDefinitionRegistry,
                webAuthnFlowRegistry,
                applicationContext,
                casProperties,
                MultifactorAuthenticationWebflowUtils.getMultifactorAuthenticationWebflowCustomizers(applicationContext));
            cfg.setOrder(WEBFLOW_CONFIGURER_ORDER + 1);
            return cfg;
        }

        @Bean
        public CasWebflowExecutionPlanConfigurer webAuthnMultifactorTrustCasWebflowExecutionPlanConfigurer(
            @Qualifier("webAuthnMultifactorTrustWebflowConfigurer")
            final CasWebflowConfigurer webAuthnMultifactorTrustWebflowConfigurer) {
            return plan -> plan.registerWebflowConfigurer(webAuthnMultifactorTrustWebflowConfigurer);
        }
    }

    @Configuration(value = "WebAuthnWebflowActionConfiguration", proxyBeanMethods = false)
    @EnableConfigurationProperties(CasConfigurationProperties.class)
    public static class WebAuthnWebflowActionConfiguration {

        @ConditionalOnMissingBean(name = "webAuthnStartAuthenticationAction")
        @Bean
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        @Autowired
        public Action webAuthnStartAuthenticationAction(
            @Qualifier("webAuthnCredentialRepository")
            final RegistrationStorage webAuthnCredentialRepository) {
            return new WebAuthnStartAuthenticationAction(webAuthnCredentialRepository);
        }

        @ConditionalOnMissingBean(name = "webAuthnStartRegistrationAction")
        @Bean
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        @Autowired
        public Action webAuthnStartRegistrationAction(
            @Qualifier("webAuthnCsrfTokenRepository")
            final CsrfTokenRepository webAuthnCsrfTokenRepository,
            @Qualifier("webAuthnCredentialRepository")
            final RegistrationStorage webAuthnCredentialRepository,
            final CasConfigurationProperties casProperties) {
            return new WebAuthnStartRegistrationAction(webAuthnCredentialRepository,
                casProperties, webAuthnCsrfTokenRepository);
        }

        @ConditionalOnMissingBean(name = "webAuthnCheckAccountRegistrationAction")
        @Bean
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        @Autowired
        public Action webAuthnCheckAccountRegistrationAction(
            @Qualifier("webAuthnCredentialRepository")
            final RegistrationStorage webAuthnCredentialRepository) {
            return new WebAuthnAccountCheckRegistrationAction(webAuthnCredentialRepository);
        }

        @ConditionalOnMissingBean(name = "webAuthnSaveAccountRegistrationAction")
        @Bean
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        @Autowired
        public Action webAuthnSaveAccountRegistrationAction(
            @Qualifier("webAuthnSessionManager")
            final SessionManager webAuthnSessionManager,
            @Qualifier("webAuthnCredentialRepository")
            final RegistrationStorage webAuthnCredentialRepository) {
            return new WebAuthnAccountSaveRegistrationAction(webAuthnCredentialRepository, webAuthnSessionManager);
        }

        @ConditionalOnMissingBean(name = "webAuthnAuthenticationWebflowAction")
        @Bean
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        @Autowired
        public Action webAuthnAuthenticationWebflowAction(
            @Qualifier("webAuthnAuthenticationWebflowEventResolver")
            final CasWebflowEventResolver webAuthnAuthenticationWebflowEventResolver) {
            return new WebAuthnAuthenticationWebflowAction(webAuthnAuthenticationWebflowEventResolver);
        }


        @ConditionalOnMissingBean(name = "webAuthnValidateSessionCredentialTokenAction")
        @Bean
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        @Autowired
        public Action webAuthnValidateSessionCredentialTokenAction(
            @Qualifier("webAuthnSessionManager")
            final SessionManager webAuthnSessionManager,
            @Qualifier("webAuthnPrincipalFactory")
            final PrincipalFactory webAuthnPrincipalFactory,
            @Qualifier("webAuthnCredentialRepository")
            final RegistrationStorage webAuthnCredentialRepository) {
            return new WebAuthnValidateSessionCredentialTokenAction(webAuthnCredentialRepository,
                webAuthnSessionManager, webAuthnPrincipalFactory);
        }
    }
}
