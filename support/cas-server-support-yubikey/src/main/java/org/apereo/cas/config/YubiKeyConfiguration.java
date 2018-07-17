package org.apereo.cas.config;

import org.apereo.cas.CentralAuthenticationService;
import org.apereo.cas.CipherExecutor;
import org.apereo.cas.adaptors.yubikey.YubikeyAccountCipherExecutor;
import org.apereo.cas.adaptors.yubikey.web.flow.YubiKeyAuthenticationWebflowAction;
import org.apereo.cas.adaptors.yubikey.web.flow.YubiKeyAuthenticationWebflowEventResolver;
import org.apereo.cas.adaptors.yubikey.web.flow.YubiKeyMultifactorTrustWebflowConfigurer;
import org.apereo.cas.adaptors.yubikey.web.flow.YubiKeyMultifactorWebflowConfigurer;
import org.apereo.cas.authentication.AuthenticationServiceSelectionPlan;
import org.apereo.cas.authentication.AuthenticationSystemSupport;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.services.MultifactorAuthenticationProviderSelector;
import org.apereo.cas.services.ServicesManager;
import org.apereo.cas.ticket.registry.TicketRegistrySupport;
import org.apereo.cas.trusted.authentication.api.MultifactorAuthenticationTrustStorage;
import org.apereo.cas.web.flow.CasWebflowConfigurer;
import org.apereo.cas.web.flow.CasWebflowExecutionPlan;
import org.apereo.cas.web.flow.CasWebflowExecutionPlanConfigurer;
import org.apereo.cas.web.flow.authentication.RankedMultifactorAuthenticationProviderSelector;
import org.apereo.cas.web.flow.resolver.CasWebflowEventResolver;

import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.DependsOn;
import org.springframework.web.util.CookieGenerator;
import org.springframework.webflow.config.FlowDefinitionRegistryBuilder;
import org.springframework.webflow.definition.registry.FlowDefinitionRegistry;
import org.springframework.webflow.engine.builder.support.FlowBuilderServices;
import org.springframework.webflow.execution.Action;

/**
 * This is {@link YubiKeyConfiguration}.
 *
 * @author Misagh Moayyed
 * @author Dmitriy Kopylenko
 * @since 5.0.0
 */
@Configuration("yubikeyConfiguration")
@EnableConfigurationProperties(CasConfigurationProperties.class)
@Slf4j
public class YubiKeyConfiguration implements CasWebflowExecutionPlanConfigurer {

    @Autowired
    @Qualifier("loginFlowRegistry")
    private FlowDefinitionRegistry loginFlowDefinitionRegistry;

    @Autowired
    private FlowBuilderServices flowBuilderServices;

    @Autowired
    private CasConfigurationProperties casProperties;

    @Autowired
    private ApplicationContext applicationContext;

    @Autowired
    @Qualifier("authenticationServiceSelectionPlan")
    private AuthenticationServiceSelectionPlan authenticationRequestServiceSelectionStrategies;

    @Autowired
    @Qualifier("centralAuthenticationService")
    private CentralAuthenticationService centralAuthenticationService;

    @Autowired
    @Qualifier("defaultAuthenticationSystemSupport")
    private AuthenticationSystemSupport authenticationSystemSupport;

    @Autowired
    @Qualifier("defaultTicketRegistrySupport")
    private TicketRegistrySupport ticketRegistrySupport;

    @Autowired
    @Qualifier("servicesManager")
    private ServicesManager servicesManager;

    @Autowired
    @Qualifier("multifactorAuthenticationProviderSelector")
    private ObjectProvider<MultifactorAuthenticationProviderSelector> multifactorAuthenticationProviderSelector;

    @Autowired
    @Qualifier("warnCookieGenerator")
    private CookieGenerator warnCookieGenerator;

    @Bean
    public FlowDefinitionRegistry yubikeyFlowRegistry() {
        val builder = new FlowDefinitionRegistryBuilder(this.applicationContext, this.flowBuilderServices);
        builder.setBasePath("classpath*:/webflow");
        builder.addFlowLocationPattern("/mfa-yubikey/*-webflow.xml");
        return builder.build();
    }

    @RefreshScope
    @Bean
    public Action yubikeyAuthenticationWebflowAction() {
        return new YubiKeyAuthenticationWebflowAction(yubikeyAuthenticationWebflowEventResolver());
    }

    @ConditionalOnMissingBean(name = "yubikeyMultifactorWebflowConfigurer")
    @Bean
    @DependsOn("defaultWebflowConfigurer")
    public CasWebflowConfigurer yubikeyMultifactorWebflowConfigurer() {
        return new YubiKeyMultifactorWebflowConfigurer(flowBuilderServices,
            loginFlowDefinitionRegistry, yubikeyFlowRegistry(), applicationContext, casProperties);
    }

    @Bean
    public CasWebflowEventResolver yubikeyAuthenticationWebflowEventResolver() {
        return new YubiKeyAuthenticationWebflowEventResolver(authenticationSystemSupport,
            centralAuthenticationService, servicesManager, ticketRegistrySupport,
            warnCookieGenerator, authenticationRequestServiceSelectionStrategies,
            multifactorAuthenticationProviderSelector.getIfAvailable(RankedMultifactorAuthenticationProviderSelector::new));
    }

    @Override
    public void configureWebflowExecutionPlan(final CasWebflowExecutionPlan plan) {
        plan.registerWebflowConfigurer(yubikeyMultifactorWebflowConfigurer());
    }

    @Bean
    @RefreshScope
    public CipherExecutor yubikeyAccountCipherExecutor() {
        val crypto = casProperties.getAuthn().getMfa().getYubikey().getCrypto();
        if (crypto.isEnabled()) {
            return new YubikeyAccountCipherExecutor(
                crypto.getEncryption().getKey(),
                crypto.getSigning().getKey(),
                crypto.getAlg());
        }
        LOGGER.info("YubiKey account encryption/signing is turned off and "
            + "MAY NOT be safe in a production environment. "
            + "Consider using other choices to handle encryption, signing and verification of "
            + "YubiKey accounts for MFA");
        return CipherExecutor.noOp();
    }


    /**
     * The Yubikey multifactor trust configuration.
     */
    @ConditionalOnClass(value = MultifactorAuthenticationTrustStorage.class)
    @ConditionalOnProperty(prefix = "cas.authn.mfa.yubikey", name = "trustedDeviceEnabled", havingValue = "true", matchIfMissing = true)
    @Configuration("yubiMultifactorTrustConfiguration")
    public class YubiKeyMultifactorTrustConfiguration implements CasWebflowExecutionPlanConfigurer {

        @ConditionalOnMissingBean(name = "yubiMultifactorTrustWebflowConfigurer")
        @Bean
        @DependsOn("defaultWebflowConfigurer")
        public CasWebflowConfigurer yubiMultifactorTrustWebflowConfigurer() {
            val deviceRegistrationEnabled = casProperties.getAuthn().getMfa().getTrusted().isDeviceRegistrationEnabled();
            return new YubiKeyMultifactorTrustWebflowConfigurer(flowBuilderServices,
                deviceRegistrationEnabled, loginFlowDefinitionRegistry, applicationContext, casProperties);
        }

        @Override
        public void configureWebflowExecutionPlan(final CasWebflowExecutionPlan plan) {
            plan.registerWebflowConfigurer(yubiMultifactorTrustWebflowConfigurer());
        }
    }
}
