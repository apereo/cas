package org.apereo.cas.web.flow.config;

import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.web.flow.CasWebflowConfigurer;
import org.apereo.cas.web.flow.CasWebflowExecutionPlanConfigurer;
import org.apereo.cas.web.flow.SpnegoCasMultifactorWebflowCustomizer;
import org.apereo.cas.web.flow.SpnegoWebflowConfigurer;
import org.apereo.cas.web.flow.configurer.CasMultifactorWebflowCustomizer;

import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.DependsOn;
import org.springframework.webflow.definition.registry.FlowDefinitionRegistry;
import org.springframework.webflow.engine.builder.support.FlowBuilderServices;

/**
 * This is {@link SpnegoWebflowConfiguration}.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 */
@Configuration("spnegoWebflowConfiguration")
@EnableConfigurationProperties(CasConfigurationProperties.class)
public class SpnegoWebflowConfiguration {
    @Autowired
    private ConfigurableApplicationContext applicationContext;

    @Autowired
    private CasConfigurationProperties casProperties;

    @Autowired
    @Qualifier("loginFlowRegistry")
    private ObjectProvider<FlowDefinitionRegistry> loginFlowDefinitionRegistry;

    @Autowired
    private ObjectProvider<FlowBuilderServices> flowBuilderServices;

    @ConditionalOnMissingBean(name = "spnegoWebflowConfigurer")
    @Bean
    @DependsOn("defaultWebflowConfigurer")
    public CasWebflowConfigurer spnegoWebflowConfigurer() {
        return new SpnegoWebflowConfigurer(flowBuilderServices.getObject(),
            loginFlowDefinitionRegistry.getObject(),
            applicationContext, casProperties);
    }

    @Bean
    @ConditionalOnMissingBean(name = "spnegoCasMultifactorWebflowCustomizer")
    @RefreshScope
    public CasMultifactorWebflowCustomizer spnegoCasMultifactorWebflowCustomizer() {
        return new SpnegoCasMultifactorWebflowCustomizer();
    }

    @Bean
    @ConditionalOnMissingBean(name = "spnegoCasWebflowExecutionPlanConfigurer")
    @RefreshScope
    public CasWebflowExecutionPlanConfigurer spnegoCasWebflowExecutionPlanConfigurer() {
        return plan -> plan.registerWebflowConfigurer(spnegoWebflowConfigurer());
    }
}
