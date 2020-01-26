package org.apereo.cas.config;

import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.interrupt.InterruptInquiryExecutionPlan;
import org.apereo.cas.interrupt.webflow.InterruptSingleSignOnParticipationStrategy;
import org.apereo.cas.interrupt.webflow.InterruptWebflowConfigurer;
import org.apereo.cas.interrupt.webflow.actions.FinalizeInterruptFlowAction;
import org.apereo.cas.interrupt.webflow.actions.InquireInterruptAction;
import org.apereo.cas.interrupt.webflow.actions.PrepareInterruptViewAction;
import org.apereo.cas.web.flow.CasWebflowConfigurer;
import org.apereo.cas.web.flow.CasWebflowExecutionPlanConfigurer;
import org.apereo.cas.web.flow.SingleSignOnParticipationStrategy;
import org.apereo.cas.web.flow.SingleSignOnParticipationStrategyConfigurer;

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
import org.springframework.webflow.execution.Action;

/**
 * This is {@link CasInterruptWebflowConfiguration}.
 *
 * @author Misagh Moayyed
 * @since 5.2.0
 */
@Configuration("casInterruptWebflowConfiguration")
@EnableConfigurationProperties(CasConfigurationProperties.class)
public class CasInterruptWebflowConfiguration {
    @Autowired
    private CasConfigurationProperties casProperties;

    @Autowired
    @Qualifier("interruptInquirer")
    private ObjectProvider<InterruptInquiryExecutionPlan> interruptInquirer;

    @Autowired
    @Qualifier("loginFlowRegistry")
    private ObjectProvider<FlowDefinitionRegistry> loginFlowDefinitionRegistry;

    @Autowired
    private ObjectProvider<FlowBuilderServices> flowBuilderServices;

    @Autowired
    private ConfigurableApplicationContext applicationContext;

    @ConditionalOnMissingBean(name = "interruptWebflowConfigurer")
    @Bean
    @DependsOn("defaultWebflowConfigurer")
    public CasWebflowConfigurer interruptWebflowConfigurer() {
        return new InterruptWebflowConfigurer(flowBuilderServices.getObject(),
            loginFlowDefinitionRegistry.getObject(), applicationContext, casProperties);
    }

    @ConditionalOnMissingBean(name = "inquireInterruptAction")
    @Bean
    @RefreshScope
    public Action inquireInterruptAction() {
        return new InquireInterruptAction(interruptInquirer.getObject().getInterruptInquirers());
    }

    @ConditionalOnMissingBean(name = "prepareInterruptViewAction")
    @Bean
    @RefreshScope
    public Action prepareInterruptViewAction() {
        return new PrepareInterruptViewAction();
    }

    @ConditionalOnMissingBean(name = "finalizeInterruptFlowAction")
    @Bean
    @RefreshScope
    public Action finalizeInterruptFlowAction() {
        return new FinalizeInterruptFlowAction();
    }

    @Bean
    @RefreshScope
    @ConditionalOnMissingBean(name = "interruptSingleSignOnParticipationStrategy")
    public SingleSignOnParticipationStrategy interruptSingleSignOnParticipationStrategy() {
        return new InterruptSingleSignOnParticipationStrategy();
    }

    @Bean
    @ConditionalOnMissingBean(name = "interruptSingleSignOnParticipationStrategyConfigurer")
    @RefreshScope
    public SingleSignOnParticipationStrategyConfigurer interruptSingleSignOnParticipationStrategyConfigurer() {
        return chain -> chain.addStrategy(interruptSingleSignOnParticipationStrategy());
    }

    @Bean
    @ConditionalOnMissingBean(name = "interruptCasWebflowExecutionPlanConfigurer")
    public CasWebflowExecutionPlanConfigurer interruptCasWebflowExecutionPlanConfigurer() {
        return plan -> plan.registerWebflowConfigurer(interruptWebflowConfigurer());
    }
}
