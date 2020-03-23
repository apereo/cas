package org.apereo.cas.config;

import org.apereo.cas.authentication.AuthenticationServiceSelectionPlan;
import org.apereo.cas.authentication.attribute.AttributeDefinitionStore;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.consent.ConsentEngine;
import org.apereo.cas.services.ServicesManager;
import org.apereo.cas.web.flow.CasWebflowConfigurer;
import org.apereo.cas.web.flow.CasWebflowExecutionPlanConfigurer;
import org.apereo.cas.web.flow.CheckConsentRequiredAction;
import org.apereo.cas.web.flow.ConfirmConsentAction;
import org.apereo.cas.web.flow.ConsentWebflowConfigurer;

import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.DependsOn;
import org.springframework.webflow.definition.registry.FlowDefinitionRegistry;
import org.springframework.webflow.engine.builder.support.FlowBuilderServices;
import org.springframework.webflow.execution.Action;

/**
 * This is {@link CasConsentWebflowConfiguration}.
 *
 * @author Misagh Moayyed
 * @since 5.1.0
 */
@Configuration("casConsentWebflowConfiguration")
@EnableConfigurationProperties(CasConfigurationProperties.class)
@ConditionalOnBean(name = "consentRepository")
public class CasConsentWebflowConfiguration {
    @Autowired
    @Qualifier("attributeDefinitionStore")
    private ObjectProvider<AttributeDefinitionStore> attributeDefinitionStore;

    @Autowired
    @Qualifier("loginFlowRegistry")
    private ObjectProvider<FlowDefinitionRegistry> loginFlowDefinitionRegistry;

    @Autowired
    private ObjectProvider<FlowBuilderServices> flowBuilderServices;

    @Autowired
    @Qualifier("authenticationServiceSelectionPlan")
    private ObjectProvider<AuthenticationServiceSelectionPlan> authenticationRequestServiceSelectionStrategies;

    @Autowired
    @Qualifier("consentEngine")
    private ObjectProvider<ConsentEngine> consentEngine;

    @Autowired
    private ConfigurableApplicationContext applicationContext;

    @Autowired
    @Qualifier("servicesManager")
    private ObjectProvider<ServicesManager> servicesManager;

    @Autowired
    private CasConfigurationProperties casProperties;

    @ConditionalOnMissingBean(name = "checkConsentRequiredAction")
    @Bean
    public Action checkConsentRequiredAction() {
        return new CheckConsentRequiredAction(servicesManager.getObject(),
            authenticationRequestServiceSelectionStrategies.getObject(),
            consentEngine.getObject(), casProperties,
            attributeDefinitionStore.getObject(), applicationContext);
    }

    @ConditionalOnMissingBean(name = "confirmConsentAction")
    @Bean
    public Action confirmConsentAction() {
        return new ConfirmConsentAction(servicesManager.getObject(),
            authenticationRequestServiceSelectionStrategies.getObject(),
            consentEngine.getObject(), casProperties,
            attributeDefinitionStore.getObject(), applicationContext);
    }

    @ConditionalOnMissingBean(name = "consentWebflowConfigurer")
    @Bean
    @DependsOn("defaultWebflowConfigurer")
    public CasWebflowConfigurer consentWebflowConfigurer() {
        return new ConsentWebflowConfigurer(flowBuilderServices.getObject(),
            loginFlowDefinitionRegistry.getObject(),
            applicationContext, casProperties);
    }

    @Bean
    @ConditionalOnMissingBean(name = "consentCasWebflowExecutionPlanConfigurer")
    public CasWebflowExecutionPlanConfigurer consentCasWebflowExecutionPlanConfigurer() {
        return plan -> plan.registerWebflowConfigurer(consentWebflowConfigurer());
    }

}
