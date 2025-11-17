package org.apereo.cas.config;

import org.apereo.cas.authentication.AuthenticationServiceSelectionPlan;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.configuration.features.CasFeatureModule;
import org.apereo.cas.interrupt.InterruptInquirer;
import org.apereo.cas.interrupt.InterruptInquiryExecutionPlan;
import org.apereo.cas.interrupt.InterruptTrackingEngine;
import org.apereo.cas.interrupt.webflow.InterruptSingleSignOnParticipationStrategy;
import org.apereo.cas.interrupt.webflow.InterruptWebflowConfigurer;
import org.apereo.cas.interrupt.webflow.actions.FinalizeInterruptFlowAction;
import org.apereo.cas.interrupt.webflow.actions.InquireInterruptAction;
import org.apereo.cas.interrupt.webflow.actions.InterruptLogoutAction;
import org.apereo.cas.services.ServicesManager;
import org.apereo.cas.ticket.registry.TicketRegistrySupport;
import org.apereo.cas.util.scripting.ScriptResourceCacheManager;
import org.apereo.cas.util.spring.boot.ConditionalOnFeatureEnabled;
import org.apereo.cas.web.flow.CasWebflowConfigurer;
import org.apereo.cas.web.flow.CasWebflowConstants;
import org.apereo.cas.web.flow.CasWebflowExecutionPlanConfigurer;
import org.apereo.cas.web.flow.SingleSignOnParticipationStrategy;
import org.apereo.cas.web.flow.SingleSignOnParticipationStrategyConfigurer;
import org.apereo.cas.web.flow.actions.ConsumerExecutionAction;
import org.apereo.cas.web.flow.actions.WebflowActionBeanSupplier;
import org.jspecify.annotations.NonNull;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ScopedProxyMode;
import org.springframework.webflow.definition.registry.FlowDefinitionRegistry;
import org.springframework.webflow.engine.builder.support.FlowBuilderServices;
import org.springframework.webflow.execution.Action;

/**
 * This is {@link CasInterruptWebflowAutoConfiguration}.
 *
 * @author Misagh Moayyed
 * @since 5.2.0
 */
@EnableConfigurationProperties(CasConfigurationProperties.class)
@ConditionalOnFeatureEnabled(feature = CasFeatureModule.FeatureCatalog.InterruptNotifications)
@AutoConfiguration
public class CasInterruptWebflowAutoConfiguration {

    @ConditionalOnMissingBean(name = "interruptWebflowConfigurer")
    @Bean
    @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
    public CasWebflowConfigurer interruptWebflowConfigurer(
        final CasConfigurationProperties casProperties,
        final ConfigurableApplicationContext applicationContext,
        @Qualifier(CasWebflowConstants.BEAN_NAME_FLOW_DEFINITION_REGISTRY)
        final FlowDefinitionRegistry flowDefinitionRegistry,
        @Qualifier(CasWebflowConstants.BEAN_NAME_FLOW_BUILDER_SERVICES)
        final FlowBuilderServices flowBuilderServices) {
        return new InterruptWebflowConfigurer(flowBuilderServices, flowDefinitionRegistry,
            applicationContext, casProperties);
    }

    @ConditionalOnMissingBean(name = CasWebflowConstants.ACTION_ID_INQUIRE_INTERRUPT)
    @Bean
    @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
    public Action inquireInterruptAction(
        final ConfigurableApplicationContext applicationContext,
        final CasConfigurationProperties casProperties,
        @Qualifier(ScriptResourceCacheManager.BEAN_NAME)
        final ObjectProvider<@NonNull ScriptResourceCacheManager> scriptResourceCacheManager,
        @Qualifier(InterruptTrackingEngine.BEAN_NAME)
        final InterruptTrackingEngine interruptTrackingEngine,
        @Qualifier(InterruptInquirer.BEAN_NAME)
        final InterruptInquiryExecutionPlan interruptInquirer) {
        return WebflowActionBeanSupplier.builder()
            .withApplicationContext(applicationContext)
            .withProperties(casProperties)
            .withAction(() -> new InquireInterruptAction(interruptInquirer.getInterruptInquirers(),
                casProperties, interruptTrackingEngine, scriptResourceCacheManager))
            .withId(CasWebflowConstants.ACTION_ID_INQUIRE_INTERRUPT)
            .build()
            .get();
    }

    @ConditionalOnMissingBean(name = CasWebflowConstants.ACTION_ID_PREPARE_INTERRUPT_VIEW)
    @Bean
    @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
    public Action prepareInterruptViewAction(final ConfigurableApplicationContext applicationContext,
                                             final CasConfigurationProperties casProperties) {
        return WebflowActionBeanSupplier.builder()
            .withApplicationContext(applicationContext)
            .withProperties(casProperties)
            .withAction(() -> ConsumerExecutionAction.NONE)
            .withId(CasWebflowConstants.ACTION_ID_PREPARE_INTERRUPT_VIEW)
            .build()
            .get();
    }


    @ConditionalOnMissingBean(name = CasWebflowConstants.ACTION_ID_INTERRUPT_LOGOUT)
    @Bean
    @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
    public Action interruptLogoutAction(final ConfigurableApplicationContext applicationContext,
                                        final CasConfigurationProperties casProperties,
                                        @Qualifier(InterruptTrackingEngine.BEAN_NAME)
                                        final InterruptTrackingEngine interruptTrackingEngine) {
        return WebflowActionBeanSupplier.builder()
            .withApplicationContext(applicationContext)
            .withProperties(casProperties)
            .withAction(() -> new InterruptLogoutAction(interruptTrackingEngine))
            .withId(CasWebflowConstants.ACTION_ID_INTERRUPT_LOGOUT)
            .build()
            .get();
    }

    @ConditionalOnMissingBean(name = CasWebflowConstants.ACTION_ID_FINALIZE_INTERRUPT)
    @Bean
    @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
    public Action finalizeInterruptFlowAction(
        final ConfigurableApplicationContext applicationContext,
        final CasConfigurationProperties casProperties,
        @Qualifier(InterruptTrackingEngine.BEAN_NAME)
        final InterruptTrackingEngine interruptTrackingEngine) {
        return WebflowActionBeanSupplier.builder()
            .withApplicationContext(applicationContext)
            .withProperties(casProperties)
            .withAction(() -> new FinalizeInterruptFlowAction(interruptTrackingEngine))
            .withId(CasWebflowConstants.ACTION_ID_FINALIZE_INTERRUPT)
            .build()
            .get();
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
        return new InterruptSingleSignOnParticipationStrategy(servicesManager,
            ticketRegistrySupport, authenticationServiceSelectionPlan);
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
    @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
    @ConditionalOnMissingBean(name = "interruptCasWebflowExecutionPlanConfigurer")
    public CasWebflowExecutionPlanConfigurer interruptCasWebflowExecutionPlanConfigurer(
        @Qualifier("interruptWebflowConfigurer")
        final CasWebflowConfigurer interruptWebflowConfigurer) {
        return plan -> plan.registerWebflowConfigurer(interruptWebflowConfigurer);
    }
}
