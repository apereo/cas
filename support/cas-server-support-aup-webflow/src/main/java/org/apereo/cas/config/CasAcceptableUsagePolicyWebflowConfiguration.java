package org.apereo.cas.config;

import org.apereo.cas.audit.AuditActionResolvers;
import org.apereo.cas.audit.AuditResourceResolvers;
import org.apereo.cas.audit.AuditTrailConstants;
import org.apereo.cas.audit.AuditTrailRecordResolutionPlanConfigurer;
import org.apereo.cas.audit.AuditableExecution;
import org.apereo.cas.aup.AcceptableUsagePolicyRepository;
import org.apereo.cas.aup.DefaultAcceptableUsagePolicyRepository;
import org.apereo.cas.aup.GroovyAcceptableUsagePolicyRepository;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.ticket.registry.TicketRegistrySupport;
import org.apereo.cas.util.scripting.WatchableGroovyScriptResource;
import org.apereo.cas.web.flow.AcceptableUsagePolicySubmitAction;
import org.apereo.cas.web.flow.AcceptableUsagePolicyVerifyAction;
import org.apereo.cas.web.flow.AcceptableUsagePolicyVerifyServiceAction;
import org.apereo.cas.web.flow.AcceptableUsagePolicyWebflowConfigurer;
import org.apereo.cas.web.flow.CasWebflowConfigurer;
import org.apereo.cas.web.flow.CasWebflowExecutionPlanConfigurer;
import org.apereo.cas.web.flow.actions.ConsumerExecutionAction;
import org.apereo.cas.web.support.WebUtils;

import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.apereo.inspektr.audit.spi.AuditResourceResolver;
import org.apereo.inspektr.audit.spi.support.DefaultAuditActionResolver;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.webflow.definition.registry.FlowDefinitionRegistry;
import org.springframework.webflow.engine.builder.support.FlowBuilderServices;
import org.springframework.webflow.execution.Action;

/**
 * This is {@link CasAcceptableUsagePolicyWebflowConfiguration}.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 */
@EnableConfigurationProperties(CasConfigurationProperties.class)
@ConditionalOnProperty(prefix = "cas.acceptable-usage-policy.core", name = "enabled", havingValue = "true", matchIfMissing = true)
@Configuration(value = "casAcceptableUsagePolicyWebflowConfiguration", proxyBeanMethods = false)
public class CasAcceptableUsagePolicyWebflowConfiguration {

    @Bean
    @RefreshScope
    @ConditionalOnMissingBean(name = "acceptableUsagePolicySubmitAction")
    public Action acceptableUsagePolicySubmitAction(
        @Qualifier("acceptableUsagePolicyRepository")
        final AcceptableUsagePolicyRepository acceptableUsagePolicyRepository) {
        return new AcceptableUsagePolicySubmitAction(acceptableUsagePolicyRepository);
    }

    @Bean
    @RefreshScope
    @ConditionalOnMissingBean(name = "acceptableUsagePolicyVerifyAction")
    public Action acceptableUsagePolicyVerifyAction(
        @Qualifier("acceptableUsagePolicyRepository")
        final AcceptableUsagePolicyRepository acceptableUsagePolicyRepository,
        @Qualifier("registeredServiceAccessStrategyEnforcer")
        final AuditableExecution registeredServiceAccessStrategyEnforcer) {
        return new AcceptableUsagePolicyVerifyAction(acceptableUsagePolicyRepository, registeredServiceAccessStrategyEnforcer);
    }

    @Bean
    @RefreshScope
    @ConditionalOnMissingBean(name = "acceptableUsagePolicyRenderAction")
    public Action acceptableUsagePolicyRenderAction(
        @Qualifier("acceptableUsagePolicyRepository")
        final AcceptableUsagePolicyRepository acceptableUsagePolicyRepository) {
        return new ConsumerExecutionAction(requestContext -> {
            var repository = acceptableUsagePolicyRepository;
            repository.fetchPolicy(requestContext).ifPresent(policy -> WebUtils.putAcceptableUsagePolicyTermsIntoFlowScope(requestContext, policy));
        });
    }

    @Bean
    @RefreshScope
    @ConditionalOnMissingBean(name = "acceptableUsagePolicyVerifyServiceAction")
    public Action acceptableUsagePolicyVerifyServiceAction(
        @Qualifier("acceptableUsagePolicyRepository")
        final AcceptableUsagePolicyRepository acceptableUsagePolicyRepository,
        @Qualifier("registeredServiceAccessStrategyEnforcer")
        final AuditableExecution registeredServiceAccessStrategyEnforcer) {
        return new AcceptableUsagePolicyVerifyServiceAction(acceptableUsagePolicyRepository, registeredServiceAccessStrategyEnforcer);
    }

    @ConditionalOnMissingBean(name = "acceptableUsagePolicyWebflowConfigurer")
    @Bean
    @Autowired
    public CasWebflowConfigurer acceptableUsagePolicyWebflowConfigurer(final CasConfigurationProperties casProperties, final ConfigurableApplicationContext applicationContext,
                                                                       @Qualifier("loginFlowDefinitionRegistry")
                                                                       final FlowDefinitionRegistry loginFlowDefinitionRegistry,
                                                                       @Qualifier("flowBuilderServices")
                                                                       final FlowBuilderServices flowBuilderServices) {
        return new AcceptableUsagePolicyWebflowConfigurer(flowBuilderServices, loginFlowDefinitionRegistry, applicationContext, casProperties);
    }

    @ConditionalOnMissingBean(name = "acceptableUsagePolicyRepository")
    @Bean
    @RefreshScope
    @Autowired
    public AcceptableUsagePolicyRepository acceptableUsagePolicyRepository(final CasConfigurationProperties casProperties, final ConfigurableApplicationContext applicationContext,
                                                                           @Qualifier("ticketRegistrySupport")
                                                                           final TicketRegistrySupport ticketRegistrySupport) {
        val groovy = casProperties.getAcceptableUsagePolicy().getGroovy();
        if (groovy.getLocation() != null) {
            return new GroovyAcceptableUsagePolicyRepository(ticketRegistrySupport, casProperties.getAcceptableUsagePolicy(), new WatchableGroovyScriptResource(groovy.getLocation()),
                applicationContext);
        }
        return new DefaultAcceptableUsagePolicyRepository(ticketRegistrySupport, casProperties.getAcceptableUsagePolicy());
    }

    @ConditionalOnMissingBean(name = "casAcceptableUsagePolicyAuditTrailRecordResolutionPlanConfigurer")
    @Bean
    public AuditTrailRecordResolutionPlanConfigurer casAcceptableUsagePolicyAuditTrailRecordResolutionPlanConfigurer(
        @Qualifier("nullableReturnValueResourceResolver")
        final AuditResourceResolver nullableReturnValueResourceResolver) {
        return plan -> {
            plan.registerAuditResourceResolver(AuditResourceResolvers.AUP_VERIFY_RESOURCE_RESOLVER, nullableReturnValueResourceResolver);
            plan.registerAuditActionResolver(AuditActionResolvers.AUP_VERIFY_ACTION_RESOLVER,
                new DefaultAuditActionResolver(AuditTrailConstants.AUDIT_ACTION_POSTFIX_TRIGGERED, StringUtils.EMPTY));
            plan.registerAuditResourceResolver(AuditResourceResolvers.AUP_SUBMIT_RESOURCE_RESOLVER, nullableReturnValueResourceResolver);
            plan.registerAuditActionResolver(AuditActionResolvers.AUP_SUBMIT_ACTION_RESOLVER,
                new DefaultAuditActionResolver(AuditTrailConstants.AUDIT_ACTION_POSTFIX_TRIGGERED, StringUtils.EMPTY));
        };
    }

    @ConditionalOnMissingBean(name = "casAcceptableUsagePolicyWebflowExecutionPlanConfigurer")
    @Bean
    public CasWebflowExecutionPlanConfigurer casAcceptableUsagePolicyWebflowExecutionPlanConfigurer(
        @Qualifier("acceptableUsagePolicyWebflowConfigurer")
        final CasWebflowConfigurer acceptableUsagePolicyWebflowConfigurer) {
        return plan -> plan.registerWebflowConfigurer(acceptableUsagePolicyWebflowConfigurer);
    }
}
