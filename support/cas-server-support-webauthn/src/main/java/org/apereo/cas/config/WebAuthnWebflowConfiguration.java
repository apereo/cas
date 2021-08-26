package org.apereo.cas.config;

import org.apereo.cas.authentication.principal.PrincipalFactory;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.trusted.config.ConditionalOnMultifactorTrustedDevicesEnabled;
import org.apereo.cas.trusted.config.MultifactorAuthnTrustConfiguration;
import org.apereo.cas.web.flow.CasWebflowConfigurer;
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
import org.springframework.beans.factory.ObjectProvider;
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
@Configuration(value = "webAuthnWebflowConfiguration", proxyBeanMethods = true)
@EnableConfigurationProperties(CasConfigurationProperties.class)
@ConditionalOnWebAuthnEnabled
public class WebAuthnWebflowConfiguration {
    private static final int WEBFLOW_CONFIGURER_ORDER = 100;

    @Autowired
    private CasConfigurationProperties casProperties;

    @Autowired
    @Qualifier("webAuthnCsrfTokenRepository")
    private ObjectProvider<CsrfTokenRepository> webAuthnCsrfTokenRepository;

    @Autowired
    @Qualifier("webAuthnPrincipalFactory")
    private ObjectProvider<PrincipalFactory> webAuthnPrincipalFactory;

    @Autowired
    @Qualifier("webAuthnSessionManager")
    private ObjectProvider<SessionManager> webAuthnSessionManager;

    @Autowired
    @Qualifier("loginFlowRegistry")
    private ObjectProvider<FlowDefinitionRegistry> loginFlowDefinitionRegistry;

    @Autowired
    private ObjectProvider<FlowBuilderServices> flowBuilderServices;

    @Autowired
    @Qualifier("webAuthnCredentialRepository")
    private ObjectProvider<RegistrationStorage> webAuthnCredentialRepository;

    @Autowired
    private ConfigurableApplicationContext applicationContext;

    @Autowired
    @Qualifier("casWebflowConfigurationContext")
    private ObjectProvider<CasWebflowEventResolutionConfigurationContext> casWebflowConfigurationContext;

    @Autowired
    @Qualifier("flowBuilder")
    private ObjectProvider<FlowBuilder> flowBuilder;

    @Bean
    @ConditionalOnMissingBean(name = "webAuthnFlowRegistry")
    public FlowDefinitionRegistry webAuthnFlowRegistry() {
        val builder = new FlowDefinitionRegistryBuilder(this.applicationContext, flowBuilderServices.getObject());
        builder.addFlowBuilder(flowBuilder.getObject(), WebAuthnMultifactorWebflowConfigurer.MFA_WEB_AUTHN_EVENT_ID);
        return builder.build();
    }

    @ConditionalOnMissingBean(name = "webAuthnAuthenticationWebflowAction")
    @Bean
    @RefreshScope
    @Autowired
    public Action webAuthnAuthenticationWebflowAction(
        @Qualifier("webAuthnAuthenticationWebflowEventResolver") final CasWebflowEventResolver webAuthnAuthenticationWebflowEventResolver) {
        return new WebAuthnAuthenticationWebflowAction(webAuthnAuthenticationWebflowEventResolver);
    }

    @ConditionalOnMissingBean(name = "webAuthnMultifactorWebflowConfigurer")
    @Bean
    @DependsOn("defaultWebflowConfigurer")
    public CasWebflowConfigurer webAuthnMultifactorWebflowConfigurer() {
        val cfg = new WebAuthnMultifactorWebflowConfigurer(flowBuilderServices.getObject(),
            loginFlowDefinitionRegistry.getObject(), webAuthnFlowRegistry(),
            applicationContext, casProperties,
            MultifactorAuthenticationWebflowUtils.getMultifactorAuthenticationWebflowCustomizers(applicationContext));
        cfg.setOrder(WEBFLOW_CONFIGURER_ORDER);
        return cfg;
    }

    @ConditionalOnMissingBean(name = "webAuthnStartAuthenticationAction")
    @Bean
    @RefreshScope
    public Action webAuthnStartAuthenticationAction() {
        return new WebAuthnStartAuthenticationAction(webAuthnCredentialRepository.getObject());
    }

    @ConditionalOnMissingBean(name = "webAuthnStartRegistrationAction")
    @Bean
    @RefreshScope
    public Action webAuthnStartRegistrationAction() {
        return new WebAuthnStartRegistrationAction(webAuthnCredentialRepository.getObject(),
            casProperties, webAuthnCsrfTokenRepository.getObject());
    }

    @ConditionalOnMissingBean(name = "webAuthnCheckAccountRegistrationAction")
    @Bean
    @RefreshScope
    public Action webAuthnCheckAccountRegistrationAction() {
        return new WebAuthnAccountCheckRegistrationAction(webAuthnCredentialRepository.getObject());
    }

    @ConditionalOnMissingBean(name = "webAuthnSaveAccountRegistrationAction")
    @Bean
    @RefreshScope
    public Action webAuthnSaveAccountRegistrationAction() {
        return new WebAuthnAccountSaveRegistrationAction(webAuthnCredentialRepository.getObject(),
            webAuthnSessionManager.getObject());
    }

    @ConditionalOnMissingBean(name = "webAuthnValidateSessionCredentialTokenAction")
    @Bean
    @RefreshScope
    public Action webAuthnValidateSessionCredentialTokenAction() {
        return new WebAuthnValidateSessionCredentialTokenAction(webAuthnCredentialRepository.getObject(),
            webAuthnSessionManager.getObject(), webAuthnPrincipalFactory.getObject());
    }

    @ConditionalOnMissingBean(name = "webAuthnAuthenticationWebflowEventResolver")
    @Bean
    @RefreshScope
    public CasWebflowEventResolver webAuthnAuthenticationWebflowEventResolver() {
        return new WebAuthnAuthenticationWebflowEventResolver(casWebflowConfigurationContext.getObject());
    }

    @ConditionalOnMissingBean(name = "webAuthnCasWebflowExecutionPlanConfigurer")
    @Bean
    @Autowired
    public CasWebflowExecutionPlanConfigurer webAuthnCasWebflowExecutionPlanConfigurer(
        @Qualifier("webAuthnMultifactorWebflowConfigurer") final CasWebflowConfigurer webAuthnMultifactorWebflowConfigurer) {
        return plan -> plan.registerWebflowConfigurer(webAuthnMultifactorWebflowConfigurer);
    }

    /**
     * The WebAuthN multifactor trust configuration.
     */
    @ConditionalOnClass(value = MultifactorAuthnTrustConfiguration.class)
    @ConditionalOnWebAuthnEnabled
    @ConditionalOnMultifactorTrustedDevicesEnabled(prefix = "cas.authn.mfa.web-authn")
    @Configuration("webAuthnMultifactorTrustConfiguration")
    public class WebAuthnMultifactorTrustConfiguration {

        @ConditionalOnMissingBean(name = "webAuthnMultifactorTrustWebflowConfigurer")
        @Bean
        @DependsOn("defaultWebflowConfigurer")
        public CasWebflowConfigurer webAuthnMultifactorTrustWebflowConfigurer() {
            val cfg = new WebAuthnMultifactorTrustWebflowConfigurer(
                flowBuilderServices.getObject(),
                loginFlowDefinitionRegistry.getObject(),
                webAuthnFlowRegistry(),
                applicationContext,
                casProperties,
                MultifactorAuthenticationWebflowUtils.getMultifactorAuthenticationWebflowCustomizers(applicationContext));
            cfg.setOrder(WEBFLOW_CONFIGURER_ORDER + 1);
            return cfg;
        }

        @Bean
        public CasWebflowExecutionPlanConfigurer webAuthnMultifactorTrustCasWebflowExecutionPlanConfigurer() {
            return plan -> plan.registerWebflowConfigurer(webAuthnMultifactorTrustWebflowConfigurer());
        }
    }
}
