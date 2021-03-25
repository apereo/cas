package org.apereo.cas.config;

import org.apereo.cas.api.AuthenticationRiskEvaluator;
import org.apereo.cas.api.AuthenticationRiskMitigator;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.web.flow.CasWebflowConfigurer;
import org.apereo.cas.web.flow.CasWebflowExecutionPlanConfigurer;
import org.apereo.cas.web.flow.RiskAwareAuthenticationWebflowConfigurer;
import org.apereo.cas.web.flow.RiskAwareAuthenticationWebflowEventResolver;
import org.apereo.cas.web.flow.resolver.CasDelegatingWebflowEventResolver;
import org.apereo.cas.web.flow.resolver.CasWebflowEventResolver;
import org.apereo.cas.web.flow.resolver.impl.CasWebflowEventResolutionConfigurationContext;

import lombok.val;
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
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.webflow.definition.registry.FlowDefinitionRegistry;
import org.springframework.webflow.engine.builder.support.FlowBuilderServices;

/**
 * This is {@link ElectronicFenceWebflowConfiguration}.
 *
 * @author Misagh Moayyed
 * @since 5.2.0
 */
@Configuration("electronicFenceWebflowConfiguration")
@EnableConfigurationProperties(CasConfigurationProperties.class)
@EnableScheduling
public class ElectronicFenceWebflowConfiguration {
    @Autowired
    @Qualifier("authenticationRiskMitigator")
    private ObjectProvider<AuthenticationRiskMitigator> authenticationRiskMitigator;

    @Autowired
    @Qualifier("authenticationRiskEvaluator")
    private ObjectProvider<AuthenticationRiskEvaluator> authenticationRiskEvaluator;

    @Autowired
    private ObjectProvider<FlowBuilderServices> flowBuilderServices;

    @Autowired
    @Qualifier("loginFlowRegistry")
    private ObjectProvider<FlowDefinitionRegistry> loginFlowDefinitionRegistry;

    @Autowired
    private CasConfigurationProperties casProperties;

    @Autowired
    private ConfigurableApplicationContext applicationContext;

    @Autowired
    @Qualifier("initialAuthenticationAttemptWebflowEventResolver")
    private ObjectProvider<CasDelegatingWebflowEventResolver> initialAuthenticationAttemptWebflowEventResolver;

    @Autowired
    @Qualifier("casWebflowConfigurationContext")
    private ObjectProvider<CasWebflowEventResolutionConfigurationContext> casWebflowConfigurationContext;

    @ConditionalOnMissingBean(name = "riskAwareAuthenticationWebflowEventResolver")
    @Bean
    @RefreshScope
    public CasWebflowEventResolver riskAwareAuthenticationWebflowEventResolver() {
        val r = new RiskAwareAuthenticationWebflowEventResolver(casWebflowConfigurationContext.getObject(),
            authenticationRiskEvaluator.getObject(),
            authenticationRiskMitigator.getObject());
        this.initialAuthenticationAttemptWebflowEventResolver.getObject().addDelegate(r, 0);
        return r;
    }

    @ConditionalOnMissingBean(name = "riskAwareAuthenticationWebflowConfigurer")
    @Bean
    @RefreshScope
    @DependsOn("defaultWebflowConfigurer")
    public CasWebflowConfigurer riskAwareAuthenticationWebflowConfigurer() {
        return new RiskAwareAuthenticationWebflowConfigurer(flowBuilderServices.getObject(),
            loginFlowDefinitionRegistry.getObject(),
            applicationContext,
            casProperties);
    }

    @Bean
    @RefreshScope
    @ConditionalOnMissingBean(name = "riskAwareCasWebflowExecutionPlanConfigurer")
    public CasWebflowExecutionPlanConfigurer riskAwareCasWebflowExecutionPlanConfigurer() {
        return plan -> plan.registerWebflowConfigurer(riskAwareAuthenticationWebflowConfigurer());
    }
}
