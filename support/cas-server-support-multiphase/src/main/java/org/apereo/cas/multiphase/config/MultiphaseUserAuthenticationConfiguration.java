package org.apereo.cas.multiphase.config;

import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.services.ServicesManager;
import org.apereo.cas.web.flow.CasWebflowConfigurer;
import org.apereo.cas.web.flow.CasWebflowExecutionPlanConfigurer;
import org.apereo.cas.web.flow.MultiphaseUserAuthenticationWebflowConfigurer;
import org.apereo.cas.web.flow.StoreUserIdForAuthenticationAction;

import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.DependsOn;
import org.springframework.webflow.definition.registry.FlowDefinitionRegistry;
import org.springframework.webflow.engine.builder.support.FlowBuilderServices;
import org.springframework.webflow.execution.Action;

/**
 * This is {@link MultiphaseUserAuthenticationConfiguration}.
 *
 * @author Hayden Sartoris
 * @since 6.2.0
 */

@Configuration("multiphaseUserAuthenticationConfiguration")
@EnableConfigurationProperties(CasConfigurationProperties.class)
public class MultiphaseUserAuthenticationConfiguration {

    @Autowired
    private CasConfigurationProperties casProperties;

    @Autowired
    @Qualifier("loginFlowRegistry")
    private ObjectProvider<FlowDefinitionRegistry> loginFlowDefinitionRegistry;

    @Autowired
    @Qualifier("servicesManager")
    private ObjectProvider<ServicesManager> servicesManager;

    @Autowired
    private ApplicationContext applicationContext;

    @Autowired
    private ObjectProvider<FlowBuilderServices> flowBuilderServices;

    @ConditionalOnMissingBean(name = "multiphaseUserAuthenticationWebflowConfigurer")
    @Bean
    @DependsOn("defaultWebflowConfigurer")
    public CasWebflowConfigurer multiphaseUserAuthenticationWebflowConfigurer() {
        return new MultiphaseUserAuthenticationWebflowConfigurer(flowBuilderServices.getObject(),
                loginFlowDefinitionRegistry.getObject(), applicationContext, casProperties);
    }

    @Bean
    @ConditionalOnMissingBean(name = "storeUserIdForAuthenticationAction")
    @RefreshScope
    public Action storeUserIdForAuthenticationAction() {
        return new StoreUserIdForAuthenticationAction();
    }

    @Bean
    @ConditionalOnMissingBean(name = "multiphaseUserAuthenticationCasWebflowExecutionPlanConfigurer")
    public CasWebflowExecutionPlanConfigurer multiphaseUserAuthenticationCasWebflowExecutionPlanConfigurer() {
        return plan -> plan.registerWebflowConfigurer(multiphaseUserAuthenticationWebflowConfigurer());
    }

}
