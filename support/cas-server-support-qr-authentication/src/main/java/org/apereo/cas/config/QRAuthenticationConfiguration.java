package org.apereo.cas.config;

import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.mfa.accepto.web.flow.QRAuthenticationWebflowConfigurer;
import org.apereo.cas.qr.web.QRAuthenticationChannelController;
import org.apereo.cas.web.flow.CasWebflowConfigurer;
import org.apereo.cas.web.flow.CasWebflowExecutionPlanConfigurer;

import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.DependsOn;
import org.springframework.webflow.definition.registry.FlowDefinitionRegistry;
import org.springframework.webflow.engine.builder.support.FlowBuilderServices;

/**
 * This is {@link QRAuthenticationConfiguration}.
 *
 * @author Misagh Moayyed
 * @since 6.3.0
 */
@Configuration("QRAuthenticationConfiguration")
@EnableConfigurationProperties(CasConfigurationProperties.class)
public class QRAuthenticationConfiguration {
    @Autowired
    @Qualifier("loginFlowRegistry")
    private ObjectProvider<FlowDefinitionRegistry> loginFlowDefinitionRegistry;

    @Autowired
    private ObjectProvider<FlowBuilderServices> flowBuilderServices;

    @Autowired
    private ConfigurableApplicationContext applicationContext;

    @Autowired
    private CasConfigurationProperties casProperties;

    @Bean
    public QRAuthenticationChannelController qrAuthenticationChannelController() {
        return new QRAuthenticationChannelController();
    }

    @ConditionalOnMissingBean(name = "qrAuthenticationWebflowConfigurer")
    @Bean
    @DependsOn("defaultWebflowConfigurer")
    public CasWebflowConfigurer qrAuthenticationWebflowConfigurer() {
        return new QRAuthenticationWebflowConfigurer(flowBuilderServices.getObject(),
            loginFlowDefinitionRegistry.getObject(),
            applicationContext, casProperties);
    }

    @Bean
    @ConditionalOnMissingBean(name = "qrAuthenticationCasWebflowExecutionPlanConfigurer")
    public CasWebflowExecutionPlanConfigurer qrAuthenticationCasWebflowExecutionPlanConfigurer() {
        return plan -> plan.registerWebflowConfigurer(qrAuthenticationWebflowConfigurer());
    }
}



