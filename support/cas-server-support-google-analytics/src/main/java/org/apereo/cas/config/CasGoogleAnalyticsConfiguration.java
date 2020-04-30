package org.apereo.cas.config;

import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.web.CasGoogleAnalyticsCookieGenerator;
import org.apereo.cas.web.cookie.CasCookieBuilder;
import org.apereo.cas.web.flow.CasGoogleAnalyticsWebflowConfigurer;
import org.apereo.cas.web.flow.CasWebflowConfigurer;
import org.apereo.cas.web.flow.CasWebflowExecutionPlanConfigurer;
import org.apereo.cas.web.flow.CreateGoogleAnalyticsCookieAction;
import org.apereo.cas.web.flow.RemoveGoogleAnalyticsCookieAction;
import org.apereo.cas.web.support.CookieUtils;

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
import org.springframework.webflow.definition.registry.FlowDefinitionRegistry;
import org.springframework.webflow.engine.builder.support.FlowBuilderServices;
import org.springframework.webflow.execution.Action;

/**
 * This is {@link CasGoogleAnalyticsConfiguration}.
 *
 * @author Misagh Moayyed
 * @since 6.1.0
 */
@Configuration("casGoogleAnalyticsConfiguration")
@EnableConfigurationProperties(CasConfigurationProperties.class)
public class CasGoogleAnalyticsConfiguration {

    @Autowired
    private CasConfigurationProperties casProperties;

    @Autowired
    @Qualifier("loginFlowRegistry")
    private ObjectProvider<FlowDefinitionRegistry> loginFlowDefinitionRegistry;

    @Autowired
    @Qualifier("logoutFlowRegistry")
    private ObjectProvider<FlowDefinitionRegistry> logoutFlowDefinitionRegistry;

    @Autowired
    private ObjectProvider<FlowBuilderServices> flowBuilderServices;

    @Autowired
    private ConfigurableApplicationContext applicationContext;

    @ConditionalOnMissingBean(name = "casGoogleAnalyticsCookieGenerator")
    @Bean
    @RefreshScope
    public CasCookieBuilder casGoogleAnalyticsCookieGenerator() {
        val props = casProperties.getGoogleAnalytics().getCookie();
        return new CasGoogleAnalyticsCookieGenerator(CookieUtils.buildCookieGenerationContext(props));
    }

    @ConditionalOnMissingBean(name = "casGoogleAnalyticsWebflowConfigurer")
    @Bean
    @DependsOn({"defaultWebflowConfigurer", "defaultLogoutWebflowConfigurer"})
    public CasWebflowConfigurer casGoogleAnalyticsWebflowConfigurer() {
        val cfg = new CasGoogleAnalyticsWebflowConfigurer(flowBuilderServices.getObject(),
            loginFlowDefinitionRegistry.getObject(),
            applicationContext, casProperties);
        cfg.setLogoutFlowDefinitionRegistry(logoutFlowDefinitionRegistry.getObject());
        return cfg;
    }

    @ConditionalOnMissingBean(name = "createGoogleAnalyticsCookieAction")
    @Bean
    @RefreshScope
    public Action createGoogleAnalyticsCookieAction() {
        return new CreateGoogleAnalyticsCookieAction(casProperties, casGoogleAnalyticsCookieGenerator());
    }

    @ConditionalOnMissingBean(name = "removeGoogleAnalyticsCookieAction")
    @Bean
    @RefreshScope
    public Action removeGoogleAnalyticsCookieAction() {
        return new RemoveGoogleAnalyticsCookieAction(casGoogleAnalyticsCookieGenerator());
    }

    @ConditionalOnMissingBean(name = "casGoogleAnalyticsWebflowExecutionPlanConfigurer")
    @Bean
    @RefreshScope
    public CasWebflowExecutionPlanConfigurer casGoogleAnalyticsWebflowExecutionPlanConfigurer() {
        return plan -> plan.registerWebflowConfigurer(casGoogleAnalyticsWebflowConfigurer());
    }
}
