package org.apereo.cas.config;

import org.apereo.cas.adaptors.yubikey.YubiKeyAccountRegistry;
import org.apereo.cas.adaptors.yubikey.web.flow.YubiKeyAccountCheckRegistrationAction;
import org.apereo.cas.adaptors.yubikey.web.flow.YubiKeyAccountSaveRegistrationAction;
import org.apereo.cas.adaptors.yubikey.web.flow.YubiKeyAuthenticationPrepareLoginAction;
import org.apereo.cas.adaptors.yubikey.web.flow.YubiKeyAuthenticationWebflowAction;
import org.apereo.cas.adaptors.yubikey.web.flow.YubiKeyAuthenticationWebflowEventResolver;
import org.apereo.cas.adaptors.yubikey.web.flow.YubiKeyMultifactorTrustedDeviceWebflowConfigurer;
import org.apereo.cas.adaptors.yubikey.web.flow.YubiKeyMultifactorWebflowConfigurer;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.trusted.config.ConditionalOnMultifactorTrustedDevicesEnabled;
import org.apereo.cas.trusted.config.MultifactorAuthnTrustConfiguration;
import org.apereo.cas.web.flow.CasWebflowConfigurer;
import org.apereo.cas.web.flow.CasWebflowExecutionPlanConfigurer;
import org.apereo.cas.web.flow.resolver.CasWebflowEventResolver;
import org.apereo.cas.web.flow.resolver.impl.CasWebflowEventResolutionConfigurationContext;
import org.apereo.cas.web.flow.util.MultifactorAuthenticationWebflowUtils;

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
@Configuration("yubiKeyAuthenticationWebflowConfiguration")
@EnableConfigurationProperties(CasConfigurationProperties.class)
public class YubiKeyAuthenticationWebflowConfiguration {
    private static final int WEBFLOW_CONFIGURER_ORDER = 100;

    @Autowired
    @Qualifier("yubiKeyAccountRegistry")
    private ObjectProvider<YubiKeyAccountRegistry> yubiKeyAccountRegistry;

    @Autowired
    @Qualifier("loginFlowRegistry")
    private ObjectProvider<FlowDefinitionRegistry> loginFlowDefinitionRegistry;

    @Autowired
    @Qualifier("flowBuilder")
    private ObjectProvider<FlowBuilder> flowBuilder;

    @Autowired
    private ObjectProvider<FlowBuilderServices> flowBuilderServices;

    @Autowired
    @Qualifier("casWebflowConfigurationContext")
    private ObjectProvider<CasWebflowEventResolutionConfigurationContext> casWebflowConfigurationContext;

    @Autowired
    private CasConfigurationProperties casProperties;

    @Autowired
    private ConfigurableApplicationContext applicationContext;

    @Bean
    @ConditionalOnMissingBean(name = "yubikeyCasWebflowExecutionPlanConfigurer")
    public CasWebflowExecutionPlanConfigurer yubikeyCasWebflowExecutionPlanConfigurer() {
        return plan -> plan.registerWebflowConfigurer(yubikeyMultifactorWebflowConfigurer());
    }


    @Bean
    @ConditionalOnMissingBean(name = "yubikeyFlowRegistry")
    public FlowDefinitionRegistry yubikeyFlowRegistry() {
        val builder = new FlowDefinitionRegistryBuilder(this.applicationContext, flowBuilderServices.getObject());
        builder.addFlowBuilder(flowBuilder.getObject(),
            YubiKeyMultifactorWebflowConfigurer.MFA_YUBIKEY_EVENT_ID);
        return builder.build();
    }

    @Bean
    @ConditionalOnMissingBean(name = "yubikeyAuthenticationWebflowEventResolver")
    public CasWebflowEventResolver yubikeyAuthenticationWebflowEventResolver() {
        return new YubiKeyAuthenticationWebflowEventResolver(casWebflowConfigurationContext.getObject());
    }

    @RefreshScope
    @Bean
    @ConditionalOnMissingBean(name = "yubikeyAuthenticationWebflowAction")
    public Action yubikeyAuthenticationWebflowAction() {
        return new YubiKeyAuthenticationWebflowAction(yubikeyAuthenticationWebflowEventResolver());
    }

    @ConditionalOnMissingBean(name = "yubikeyMultifactorWebflowConfigurer")
    @Bean
    @DependsOn("defaultWebflowConfigurer")
    public CasWebflowConfigurer yubikeyMultifactorWebflowConfigurer() {
        val cfg = new YubiKeyMultifactorWebflowConfigurer(flowBuilderServices.getObject(),
            loginFlowDefinitionRegistry.getObject(), yubikeyFlowRegistry(),
            applicationContext, casProperties,
            MultifactorAuthenticationWebflowUtils.getMultifactorAuthenticationWebflowCustomizers(applicationContext));
        cfg.setOrder(WEBFLOW_CONFIGURER_ORDER);
        return cfg;
    }

    @Bean
    @RefreshScope
    @ConditionalOnMissingBean(name = "prepareYubiKeyAuthenticationLoginAction")
    public Action prepareYubiKeyAuthenticationLoginAction() {
        return new YubiKeyAuthenticationPrepareLoginAction(casProperties);
    }

    @Bean
    @RefreshScope
    @ConditionalOnMissingBean(name = "yubiKeyAccountRegistrationAction")
    public Action yubiKeyAccountRegistrationAction() {
        return new YubiKeyAccountCheckRegistrationAction(yubiKeyAccountRegistry.getObject());
    }

    @Bean
    @RefreshScope
    @ConditionalOnMissingBean(name = "yubiKeySaveAccountRegistrationAction")
    public Action yubiKeySaveAccountRegistrationAction() {
        return new YubiKeyAccountSaveRegistrationAction(yubiKeyAccountRegistry.getObject());
    }

    /**
     * The Yubikey multifactor trust configuration.
     */
    @ConditionalOnClass(value = MultifactorAuthnTrustConfiguration.class)
    @ConditionalOnMultifactorTrustedDevicesEnabled(prefix = "cas.authn.mfa.yubikey")
    @Configuration("yubiMultifactorTrustConfiguration")
    public class YubiKeyMultifactorTrustConfiguration {

        @ConditionalOnMissingBean(name = "yubiMultifactorTrustWebflowConfigurer")
        @Bean
        @DependsOn("defaultWebflowConfigurer")
        public CasWebflowConfigurer yubiMultifactorTrustWebflowConfigurer() {
            val cfg = new YubiKeyMultifactorTrustedDeviceWebflowConfigurer(flowBuilderServices.getObject(),
                yubikeyFlowRegistry(),
                loginFlowDefinitionRegistry.getObject(),
                applicationContext,
                casProperties,
                MultifactorAuthenticationWebflowUtils.getMultifactorAuthenticationWebflowCustomizers(applicationContext));
            cfg.setOrder(WEBFLOW_CONFIGURER_ORDER + 1);
            return cfg;
        }

        @Bean
        @ConditionalOnMissingBean(name = "yubiMultifactorCasWebflowExecutionPlanConfigurer")
        public CasWebflowExecutionPlanConfigurer yubiMultifactorCasWebflowExecutionPlanConfigurer() {
            return plan -> plan.registerWebflowConfigurer(yubiMultifactorTrustWebflowConfigurer());
        }
    }
}
