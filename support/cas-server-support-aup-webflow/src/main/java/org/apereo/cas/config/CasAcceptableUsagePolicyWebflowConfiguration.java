package org.apereo.cas.config;

import org.apereo.cas.audit.AuditTrailConstants;
import org.apereo.cas.audit.AuditTrailRecordResolutionPlanConfigurer;
import org.apereo.cas.audit.AuditableExecution;
import org.apereo.cas.aup.AcceptableUsagePolicyRepository;
import org.apereo.cas.aup.DefaultAcceptableUsagePolicyRepository;
import org.apereo.cas.aup.GroovyAcceptableUsagePolicyRepository;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.ticket.registry.TicketRegistrySupport;
import org.apereo.cas.util.scripting.WatchableGroovyScriptResource;
import org.apereo.cas.web.flow.AcceptableUsagePolicyRenderAction;
import org.apereo.cas.web.flow.AcceptableUsagePolicySubmitAction;
import org.apereo.cas.web.flow.AcceptableUsagePolicyVerifyAction;
import org.apereo.cas.web.flow.AcceptableUsagePolicyVerifyServiceAction;
import org.apereo.cas.web.flow.AcceptableUsagePolicyWebflowConfigurer;
import org.apereo.cas.web.flow.CasWebflowConfigurer;
import org.apereo.cas.web.flow.CasWebflowExecutionPlanConfigurer;

import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.apereo.inspektr.audit.spi.AuditResourceResolver;
import org.apereo.inspektr.audit.spi.support.DefaultAuditActionResolver;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
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
 * This is {@link CasAcceptableUsagePolicyWebflowConfiguration}.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 */
@Configuration("casAcceptableUsagePolicyWebflowConfiguration")
@EnableConfigurationProperties(CasConfigurationProperties.class)
@ConditionalOnProperty(prefix = "cas.acceptable-usage-policy", name = "enabled", havingValue = "true", matchIfMissing = true)
public class CasAcceptableUsagePolicyWebflowConfiguration {

    @Autowired
    @Qualifier("loginFlowRegistry")
    private ObjectProvider<FlowDefinitionRegistry> loginFlowDefinitionRegistry;

    @Autowired
    private ObjectProvider<FlowBuilderServices> flowBuilderServices;

    @Autowired
    @Qualifier("defaultTicketRegistrySupport")
    private ObjectProvider<TicketRegistrySupport> ticketRegistrySupport;

    @Autowired
    private ConfigurableApplicationContext applicationContext;

    @Autowired
    private CasConfigurationProperties casProperties;

    @Autowired
    @Qualifier("nullableReturnValueResourceResolver")
    private ObjectProvider<AuditResourceResolver> nullableReturnValueResourceResolver;

    @Autowired
    @Qualifier("registeredServiceAccessStrategyEnforcer")
    private ObjectProvider<AuditableExecution> registeredServiceAccessStrategyEnforcer;

    @Bean
    @RefreshScope
    @ConditionalOnMissingBean(name = "acceptableUsagePolicySubmitAction")
    public Action acceptableUsagePolicySubmitAction() {
        return new AcceptableUsagePolicySubmitAction(acceptableUsagePolicyRepository());
    }

    @Bean
    @RefreshScope
    @ConditionalOnMissingBean(name = "acceptableUsagePolicyVerifyAction")
    public Action acceptableUsagePolicyVerifyAction() {
        return new AcceptableUsagePolicyVerifyAction(acceptableUsagePolicyRepository(),
            registeredServiceAccessStrategyEnforcer.getObject());
    }

    @Bean
    @RefreshScope
    @ConditionalOnMissingBean(name = "acceptableUsagePolicyRenderAction")
    public Action acceptableUsagePolicyRenderAction() {
        return new AcceptableUsagePolicyRenderAction(acceptableUsagePolicyRepository());
    }

    @Bean
    @RefreshScope
    @ConditionalOnMissingBean(name = "acceptableUsagePolicyVerifyServiceAction")
    public Action acceptableUsagePolicyVerifyServiceAction() {
        return new AcceptableUsagePolicyVerifyServiceAction(acceptableUsagePolicyRepository(),
            registeredServiceAccessStrategyEnforcer.getObject());
    }

    @ConditionalOnMissingBean(name = "acceptableUsagePolicyWebflowConfigurer")
    @Bean
    @DependsOn("defaultWebflowConfigurer")
    public CasWebflowConfigurer acceptableUsagePolicyWebflowConfigurer() {
        return new AcceptableUsagePolicyWebflowConfigurer(flowBuilderServices.getObject(),
            loginFlowDefinitionRegistry.getObject(), applicationContext, casProperties);
    }

    @ConditionalOnMissingBean(name = "acceptableUsagePolicyRepository")
    @Bean
    @RefreshScope
    public AcceptableUsagePolicyRepository acceptableUsagePolicyRepository() {
        val groovy = casProperties.getAcceptableUsagePolicy().getGroovy();
        if (groovy.getLocation() != null) {
            return new GroovyAcceptableUsagePolicyRepository(ticketRegistrySupport.getObject(),
                casProperties.getAcceptableUsagePolicy(),
                new WatchableGroovyScriptResource(groovy.getLocation()), applicationContext);
        }

        return new DefaultAcceptableUsagePolicyRepository(
            ticketRegistrySupport.getObject(),
            casProperties.getAcceptableUsagePolicy());
    }

    @ConditionalOnMissingBean(name = "casAcceptableUsagePolicyAuditTrailRecordResolutionPlanConfigurer")
    @Bean
    public AuditTrailRecordResolutionPlanConfigurer casAcceptableUsagePolicyAuditTrailRecordResolutionPlanConfigurer() {
        return plan -> {
            plan.registerAuditResourceResolver("AUP_VERIFY_RESOURCE_RESOLVER",
                nullableReturnValueResourceResolver.getObject());
            plan.registerAuditActionResolver("AUP_VERIFY_ACTION_RESOLVER",
                new DefaultAuditActionResolver(AuditTrailConstants.AUDIT_ACTION_POSTFIX_TRIGGERED, StringUtils.EMPTY));

            plan.registerAuditResourceResolver("AUP_SUBMIT_RESOURCE_RESOLVER",
                nullableReturnValueResourceResolver.getObject());
            plan.registerAuditActionResolver("AUP_SUBMIT_ACTION_RESOLVER",
                new DefaultAuditActionResolver(AuditTrailConstants.AUDIT_ACTION_POSTFIX_TRIGGERED, StringUtils.EMPTY));
        };
    }

    @ConditionalOnMissingBean(name = "casAcceptableUsagePolicyWebflowExecutionPlanConfigurer")
    @Bean
    public CasWebflowExecutionPlanConfigurer casAcceptableUsagePolicyWebflowExecutionPlanConfigurer() {
        return plan -> plan.registerWebflowConfigurer(acceptableUsagePolicyWebflowConfigurer());
    }
}
