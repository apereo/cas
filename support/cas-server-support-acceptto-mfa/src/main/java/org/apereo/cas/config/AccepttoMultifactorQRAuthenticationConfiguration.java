package org.apereo.cas.config;

import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.mfa.accepto.web.flow.qr.AccepttoQRCodeAuthenticationWebflowConfigurer;
import org.apereo.cas.mfa.accepto.web.flow.qr.AccepttoQRCodePrepareAuthenticationAction;
import org.apereo.cas.services.ServicesManager;
import org.apereo.cas.web.flow.CasWebflowConfigurer;
import org.apereo.cas.web.flow.CasWebflowExecutionPlan;
import org.apereo.cas.web.flow.CasWebflowExecutionPlanConfigurer;

import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.DependsOn;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.webflow.definition.registry.FlowDefinitionRegistry;
import org.springframework.webflow.engine.builder.support.FlowBuilderServices;
import org.springframework.webflow.execution.Action;

/**
 * This is {@link AccepttoMultifactorQRAuthenticationConfiguration}.
 *
 * @author Misagh Moayyed
 * @since 6.1.0
 */
@Configuration("accepttoMultifactorQRAuthenticationConfiguration")
@EnableConfigurationProperties(CasConfigurationProperties.class)
@EnableScheduling
@ConditionalOnProperty(value = "cas.authn.mfa.acceptto.qrLoginEnabled", havingValue = "true")
public class AccepttoMultifactorQRAuthenticationConfiguration {
    @Autowired
    private CasConfigurationProperties casProperties;

    @Autowired
    private ApplicationContext applicationContext;

    @Autowired
    @Qualifier("loginFlowRegistry")
    private ObjectProvider<FlowDefinitionRegistry> loginFlowDefinitionRegistry;

    @Autowired
    private FlowBuilderServices flowBuilderServices;

    @Autowired
    @Qualifier("servicesManager")
    private ObjectProvider<ServicesManager> servicesManager;

    @ConditionalOnMissingBean(name = "mfaAccepttoQRCodeCasWebflowExecutionPlanConfigurer")
    @Bean
    public CasWebflowExecutionPlanConfigurer mfaAccepttoQRCodeCasWebflowExecutionPlanConfigurer() {
        return new CasWebflowExecutionPlanConfigurer() {
            @Override
            public void configureWebflowExecutionPlan(final CasWebflowExecutionPlan plan) {
                plan.registerWebflowConfigurer(mfaAccepttoQRCodeAuthenticationWebflowConfigurer());
            }
        };
    }

    @ConditionalOnMissingBean(name = "mfaAccepttoQRCodeAuthenticationWebflowConfigurer")
    @Bean
    @DependsOn("defaultWebflowConfigurer")
    public CasWebflowConfigurer mfaAccepttoQRCodeAuthenticationWebflowConfigurer() {
        return new AccepttoQRCodeAuthenticationWebflowConfigurer(flowBuilderServices,
            loginFlowDefinitionRegistry.getIfAvailable(),
            applicationContext,
            casProperties);
    }

    @Bean
    public Action initializeLoginAction() {
        return new AccepttoQRCodePrepareAuthenticationAction(servicesManager.getIfAvailable());
    }
}
