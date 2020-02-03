package org.apereo.cas.config;

import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.api.UserGraphicalAuthenticationRepository;
import org.apereo.cas.impl.LdapUserGraphicalAuthenticationRepository;
import org.apereo.cas.impl.StaticUserGraphicalAuthenticationRepository;
import org.apereo.cas.services.ServicesManager;
import org.apereo.cas.web.flow.AcceptUserGraphicsForAuthenticationAction;
import org.apereo.cas.web.flow.CasWebflowConfigurer;
import org.apereo.cas.web.flow.CasWebflowExecutionPlanConfigurer;
import org.apereo.cas.web.flow.DisplayUserGraphicsBeforeAuthenticationAction;
import org.apereo.cas.web.flow.GraphicalUserAuthenticationWebflowConfigurer;
import org.apereo.cas.web.flow.PrepareForGraphicalAuthenticationAction;

import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.BeanCreationException;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.DependsOn;
import org.springframework.webflow.definition.registry.FlowDefinitionRegistry;
import org.springframework.webflow.engine.builder.support.FlowBuilderServices;
import org.springframework.webflow.execution.Action;

/**
 * This is {@link GraphicalUserAuthenticationWebflowConfiguration}.
 *
 * @author Misagh Moayyed
 * @author Hayden Sartoris
 * @since 5.1.0
 */
@Configuration("graphicalUserAuthenticationWebflowConfiguration")
@EnableConfigurationProperties(CasConfigurationProperties.class)
@AutoConfigureAfter(MultiphaseAuthenticationWebflowConfiguration.class)
public class GraphicalUserAuthenticationWebflowConfiguration {

    @Autowired
    private CasConfigurationProperties casProperties;

    @Autowired
    @Qualifier("loginFlowRegistry")
    private ObjectProvider<FlowDefinitionRegistry> loginFlowDefinitionRegistry;

    @Autowired
    @Qualifier("servicesManager")
    private ObjectProvider<ServicesManager> servicesManager;

    @Autowired
    private ConfigurableApplicationContext applicationContext;

    @Autowired
    private ObjectProvider<FlowBuilderServices> flowBuilderServices;

    @ConditionalOnMissingBean(name = "graphicalUserAuthenticationWebflowConfigurer")
    @Bean
    @DependsOn("defaultWebflowConfigurer")
    public CasWebflowConfigurer graphicalUserAuthenticationWebflowConfigurer() {
        return new GraphicalUserAuthenticationWebflowConfigurer(flowBuilderServices.getObject(),
            loginFlowDefinitionRegistry.getObject(), applicationContext, casProperties);
    }

    @Autowired
    @Qualifier("userGraphicalAuthenticationRepository")
    private ObjectProvider<UserGraphicalAuthenticationRepository> userGraphicalAuthenticationRepository;

    /*
    @Bean
    @ConditionalOnMissingBean(name = "acceptUserGraphicsForAuthenticationAction")
    @RefreshScope
    public Action acceptUserGraphicsForAuthenticationAction() {
        return new AcceptUserGraphicsForAuthenticationAction();
    }
    */

    @Bean
    @RefreshScope
    @ConditionalOnMissingBean(name = "displayUserGraphicsBeforeAuthenticationAction")
    public Action displayUserGraphicsBeforeAuthenticationAction() {
        return new DisplayUserGraphicsBeforeAuthenticationAction(userGraphicalAuthenticationRepository.getObject());
    }

    @Bean
    @RefreshScope
    public Action initializeLoginAction() {
        return new PrepareForGraphicalAuthenticationAction(servicesManager.getObject());
    }

    @Bean
    @ConditionalOnMissingBean(name = "graphicalUserAuthenticationCasWebflowExecutionPlanConfigurer")
    public CasWebflowExecutionPlanConfigurer graphicalUserAuthenticationCasWebflowExecutionPlanConfigurer() {
        return plan -> plan.registerWebflowConfigurer(graphicalUserAuthenticationWebflowConfigurer());
    }
}
