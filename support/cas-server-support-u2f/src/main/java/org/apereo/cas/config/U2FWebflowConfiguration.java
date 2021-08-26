package org.apereo.cas.config;

import org.apereo.cas.adaptors.u2f.storage.U2FDeviceRepository;
import org.apereo.cas.adaptors.u2f.web.U2FRegisteredDevicesEndpoint;
import org.apereo.cas.adaptors.u2f.web.flow.U2FAccountCheckRegistrationAction;
import org.apereo.cas.adaptors.u2f.web.flow.U2FAccountSaveRegistrationAction;
import org.apereo.cas.adaptors.u2f.web.flow.U2FAuthenticationWebflowAction;
import org.apereo.cas.adaptors.u2f.web.flow.U2FAuthenticationWebflowEventResolver;
import org.apereo.cas.adaptors.u2f.web.flow.U2FMultifactorTrustedDeviceWebflowConfigurer;
import org.apereo.cas.adaptors.u2f.web.flow.U2FMultifactorWebflowConfigurer;
import org.apereo.cas.adaptors.u2f.web.flow.U2FStartAuthenticationAction;
import org.apereo.cas.adaptors.u2f.web.flow.U2FStartRegistrationAction;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.trusted.config.ConditionalOnMultifactorTrustedDevicesEnabled;
import org.apereo.cas.trusted.config.MultifactorAuthnTrustConfiguration;
import org.apereo.cas.web.flow.CasWebflowConfigurer;
import org.apereo.cas.web.flow.CasWebflowExecutionPlanConfigurer;
import org.apereo.cas.web.flow.resolver.CasWebflowEventResolver;
import org.apereo.cas.web.flow.resolver.impl.CasWebflowEventResolutionConfigurationContext;
import org.apereo.cas.web.flow.util.MultifactorAuthenticationWebflowUtils;

import com.yubico.u2f.U2F;
import lombok.val;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.actuate.autoconfigure.endpoint.condition.ConditionalOnAvailableEndpoint;
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
 * This is {@link U2FWebflowConfiguration}.
 *
 * @author Misagh Moayyed
 * @since 5.1.0
 */
@Configuration("u2FWebflowConfiguration")
@EnableConfigurationProperties(CasConfigurationProperties.class)
public class U2FWebflowConfiguration {
    private static final int WEBFLOW_CONFIGURER_ORDER = 100;

    @Autowired
    private CasConfigurationProperties casProperties;

    @Autowired
    @Qualifier("loginFlowRegistry")
    private ObjectProvider<FlowDefinitionRegistry> loginFlowDefinitionRegistry;

    @Autowired
    private ObjectProvider<FlowBuilderServices> flowBuilderServices;

    @Autowired
    @Qualifier("u2fDeviceRepository")
    private ObjectProvider<U2FDeviceRepository> u2fDeviceRepository;

    @Autowired
    private ConfigurableApplicationContext applicationContext;

    @Autowired
    @Qualifier("u2fService")
    private ObjectProvider<U2F> u2fService;

    @Autowired
    @Qualifier("flowBuilder")
    private ObjectProvider<FlowBuilder> flowBuilder;

    @Autowired
    @Qualifier("casWebflowConfigurationContext")
    private ObjectProvider<CasWebflowEventResolutionConfigurationContext> casWebflowConfigurationContext;
    
    @Bean
    @ConditionalOnMissingBean(name = "u2fFlowRegistry")
    public FlowDefinitionRegistry u2fFlowRegistry() {
        val builder = new FlowDefinitionRegistryBuilder(this.applicationContext, flowBuilderServices.getObject());
        builder.addFlowBuilder(flowBuilder.getObject(), U2FMultifactorWebflowConfigurer.MFA_U2F_EVENT_ID);
        return builder.build();
    }

    @ConditionalOnMissingBean(name = "u2fAuthenticationWebflowAction")
    @Bean
    @RefreshScope
    public Action u2fAuthenticationWebflowAction() {
        return new U2FAuthenticationWebflowAction(u2fAuthenticationWebflowEventResolver());
    }

    @ConditionalOnMissingBean(name = "u2fMultifactorWebflowConfigurer")
    @Bean
    @DependsOn("defaultWebflowConfigurer")
    public CasWebflowConfigurer u2fMultifactorWebflowConfigurer() {
        val cfg = new U2FMultifactorWebflowConfigurer(flowBuilderServices.getObject(),
            loginFlowDefinitionRegistry.getObject(), u2fFlowRegistry(),
            applicationContext, casProperties,
            MultifactorAuthenticationWebflowUtils.getMultifactorAuthenticationWebflowCustomizers(applicationContext));
        cfg.setOrder(WEBFLOW_CONFIGURER_ORDER);
        return cfg;
    }

    @ConditionalOnMissingBean(name = "u2fStartAuthenticationAction")
    @Bean
    @RefreshScope
    public Action u2fStartAuthenticationAction() {
        return new U2FStartAuthenticationAction(u2fService.getObject(),
            casProperties.getServer().getName(),
            u2fDeviceRepository.getObject());
    }

    @ConditionalOnMissingBean(name = "u2fStartRegistrationAction")
    @Bean
    @RefreshScope
    public Action u2fStartRegistrationAction() {
        return new U2FStartRegistrationAction(u2fService.getObject(),
            casProperties.getServer().getName(), u2fDeviceRepository.getObject());
    }

    @ConditionalOnMissingBean(name = "u2fCheckAccountRegistrationAction")
    @Bean
    @RefreshScope
    public Action u2fCheckAccountRegistrationAction() {
        return new U2FAccountCheckRegistrationAction(u2fDeviceRepository.getObject());
    }

    @ConditionalOnMissingBean(name = "u2fSaveAccountRegistrationAction")
    @Bean
    @RefreshScope
    public Action u2fSaveAccountRegistrationAction() {
        return new U2FAccountSaveRegistrationAction(u2fService.getObject(), u2fDeviceRepository.getObject());
    }

    @ConditionalOnMissingBean(name = "u2fAuthenticationWebflowEventResolver")
    @Bean
    @RefreshScope
    public CasWebflowEventResolver u2fAuthenticationWebflowEventResolver() {
        return new U2FAuthenticationWebflowEventResolver(casWebflowConfigurationContext.getObject());
    }

    @Bean
    @ConditionalOnMissingBean(name = "u2fCasWebflowExecutionPlanConfigurer")
    public CasWebflowExecutionPlanConfigurer u2fCasWebflowExecutionPlanConfigurer() {
        return plan -> plan.registerWebflowConfigurer(u2fMultifactorWebflowConfigurer());
    }

    @Bean
    @ConditionalOnAvailableEndpoint
    public U2FRegisteredDevicesEndpoint u2fRegisteredDevicesEndpoint() {
        return new U2FRegisteredDevicesEndpoint(casProperties, u2fDeviceRepository.getObject());
    }

    /**
     * The u2f authenticator multifactor trust configuration.
     */
    @ConditionalOnClass(value = MultifactorAuthnTrustConfiguration.class)
    @ConditionalOnMultifactorTrustedDevicesEnabled(prefix = "cas.authn.mfa.u2f")
    @Configuration("u2fMultifactorTrustConfiguration")
    public class U2FMultifactorTrustConfiguration {

        @ConditionalOnMissingBean(name = "u2fMultifactorTrustWebflowConfigurer")
        @Bean
        @DependsOn({"defaultWebflowConfigurer", "u2fMultifactorWebflowConfigurer"})
        public CasWebflowConfigurer u2fMultifactorTrustWebflowConfigurer() {
            val cfg = new U2FMultifactorTrustedDeviceWebflowConfigurer(flowBuilderServices.getObject(),
                loginFlowDefinitionRegistry.getObject(),
                u2fFlowRegistry(),
                applicationContext,
                casProperties,
                MultifactorAuthenticationWebflowUtils.getMultifactorAuthenticationWebflowCustomizers(applicationContext));
            cfg.setOrder(WEBFLOW_CONFIGURER_ORDER + 1);
            return cfg;
        }

        @Bean
        public CasWebflowExecutionPlanConfigurer u2fMultifactorTrustCasWebflowExecutionPlanConfigurer() {
            return plan -> plan.registerWebflowConfigurer(u2fMultifactorTrustWebflowConfigurer());
        }
    }
}
