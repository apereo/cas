package org.apereo.cas.config;

import org.apereo.cas.authentication.AuthenticationServiceSelectionPlan;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.interrupt.InterruptInquiryExecutionPlan;
import org.apereo.cas.interrupt.webflow.InterruptSingleSignOnParticipationStrategy;
import org.apereo.cas.interrupt.webflow.InterruptWebflowConfigurer;
import org.apereo.cas.interrupt.webflow.actions.FinalizeInterruptFlowAction;
import org.apereo.cas.interrupt.webflow.actions.InquireInterruptAction;
import org.apereo.cas.interrupt.webflow.actions.PrepareInterruptViewAction;
import org.apereo.cas.services.ServicesManager;
import org.apereo.cas.ticket.registry.TicketRegistrySupport;
import org.apereo.cas.web.cookie.CasCookieBuilder;
import org.apereo.cas.web.flow.CasWebflowConfigurer;
import org.apereo.cas.web.flow.CasWebflowConstants;
import org.apereo.cas.web.flow.CasWebflowExecutionPlanConfigurer;
import org.apereo.cas.web.flow.SingleSignOnParticipationStrategy;
import org.apereo.cas.web.flow.SingleSignOnParticipationStrategyConfigurer;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.ScopedProxyMode;
import org.springframework.webflow.definition.registry.FlowDefinitionRegistry;
import org.springframework.webflow.engine.builder.support.FlowBuilderServices;
import org.springframework.webflow.execution.Action;

/**
 * This is {@link CasInterruptWebflowConfiguration}.
 *
 * @author Misagh Moayyed
 * @since 5.2.0
 */
@EnableConfigurationProperties(CasConfigurationProperties.class)
@Configuration(value = "casInterruptWebflowConfiguration", proxyBeanMethods = false)
public class CasInterruptWebflowConfiguration {

    @ConditionalOnMissingBean(name = "interruptWebflowConfigurer")
    @Bean
    @Autowired
    public CasWebflowConfigurer interruptWebflowConfigurer(final CasConfigurationProperties casProperties, final ConfigurableApplicationContext applicationContext,
                                                           @Qualifier(CasWebflowConstants.BEAN_NAME_LOGIN_FLOW_DEFINITION_REGISTRY)
                                                           final FlowDefinitionRegistry loginFlowDefinitionRegistry,
                                                           @Qualifier(CasWebflowConstants.BEAN_NAME_FLOW_BUILDER_SERVICES)
                                                           final FlowBuilderServices flowBuilderServices) {
        return new InterruptWebflowConfigurer(flowBuilderServices, loginFlowDefinitionRegistry, applicationContext, casProperties);
    }

    @ConditionalOnMissingBean(name = "inquireInterruptAction")
    @Bean
    @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
    @Autowired
    public Action inquireInterruptAction(final CasConfigurationProperties casProperties,
                                         @Qualifier("interruptCookieGenerator")
                                         final CasCookieBuilder interruptCookieGenerator,
                                         @Qualifier("interruptInquirer")
                                         final InterruptInquiryExecutionPlan interruptInquirer) {
        return new InquireInterruptAction(interruptInquirer.getInterruptInquirers(), casProperties, interruptCookieGenerator);
    }

    @ConditionalOnMissingBean(name = "prepareInterruptViewAction")
    @Bean
    @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
    public Action prepareInterruptViewAction() {
        return new PrepareInterruptViewAction();
    }

    @ConditionalOnMissingBean(name = "finalizeInterruptFlowAction")
    @Bean
    @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
    public Action finalizeInterruptFlowAction(
        @Qualifier("interruptCookieGenerator")
        final CasCookieBuilder interruptCookieGenerator) {
        return new FinalizeInterruptFlowAction(interruptCookieGenerator);
    }

    @Bean
    @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
    @ConditionalOnMissingBean(name = "interruptSingleSignOnParticipationStrategy")
    public SingleSignOnParticipationStrategy interruptSingleSignOnParticipationStrategy(
        @Qualifier(TicketRegistrySupport.BEAN_NAME)
        final TicketRegistrySupport ticketRegistrySupport,
        @Qualifier(ServicesManager.BEAN_NAME)
        final ServicesManager servicesManager,
        @Qualifier(AuthenticationServiceSelectionPlan.BEAN_NAME)
        final AuthenticationServiceSelectionPlan authenticationServiceSelectionPlan) {
        return new InterruptSingleSignOnParticipationStrategy(servicesManager, ticketRegistrySupport, authenticationServiceSelectionPlan);
    }

    @Bean
    @ConditionalOnMissingBean(name = "interruptSingleSignOnParticipationStrategyConfigurer")
    @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
    public SingleSignOnParticipationStrategyConfigurer interruptSingleSignOnParticipationStrategyConfigurer(
        @Qualifier("interruptSingleSignOnParticipationStrategy")
        final SingleSignOnParticipationStrategy interruptSingleSignOnParticipationStrategy) {
        return chain -> chain.addStrategy(interruptSingleSignOnParticipationStrategy);
    }

    @Bean
    @ConditionalOnMissingBean(name = "interruptCasWebflowExecutionPlanConfigurer")
    public CasWebflowExecutionPlanConfigurer interruptCasWebflowExecutionPlanConfigurer(
        @Qualifier("interruptWebflowConfigurer")
        final CasWebflowConfigurer interruptWebflowConfigurer) {
        return plan -> plan.registerWebflowConfigurer(interruptWebflowConfigurer);
    }
}
