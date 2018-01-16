package org.apereo.cas.web.flow.config;

import org.apereo.cas.authentication.AuthenticationServiceSelectionPlan;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.services.ServicesManager;
import org.apereo.cas.util.CollectionUtils;
import org.apereo.cas.web.flow.CasWebflowConfigurer;
import org.apereo.cas.web.flow.Pac4jErrorViewResolver;
import org.apereo.cas.web.flow.Pac4jWebflowConfigurer;
import org.apereo.cas.web.support.ArgumentExtractor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.web.ErrorViewResolver;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.DependsOn;
import org.springframework.webflow.definition.registry.FlowDefinitionRegistry;
import org.springframework.webflow.engine.builder.support.FlowBuilderServices;
import org.springframework.webflow.execution.Action;
import org.apereo.cas.web.flow.initialFlowSetupP4jAction;

/**
 * This is {@link Pac4jWebflowConfiguration}.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 */
@Configuration("pac4jWebflowConfiguration")
@EnableConfigurationProperties(CasConfigurationProperties.class)
public class Pac4jWebflowConfiguration {

    @Autowired
    @Qualifier("loginFlowRegistry")
    private FlowDefinitionRegistry loginFlowDefinitionRegistry;

    @Autowired
    private FlowBuilderServices flowBuilderServices;

    @Autowired
    private ApplicationContext applicationContext;

    @Autowired
    private CasConfigurationProperties casProperties;

    @Autowired
    @Qualifier("saml2ClientLogoutAction")
    private Action saml2ClientLogoutAction;

    @Autowired
    @Qualifier("logoutFlowRegistry")
    private FlowDefinitionRegistry logoutFlowDefinitionRegistry;

    @Autowired
    @Qualifier("authenticationServiceSelectionPlan")
    private AuthenticationServiceSelectionPlan authenticationRequestServiceSelectionStrategies;

    @Autowired
    @Qualifier("servicesManager")
    private ServicesManager servicesManager;

    @ConditionalOnMissingBean(name = "pac4jWebflowConfigurer")
    @Bean
    @DependsOn("defaultWebflowConfigurer")
    public CasWebflowConfigurer pac4jWebflowConfigurer() {
        final CasWebflowConfigurer w = new Pac4jWebflowConfigurer(flowBuilderServices, loginFlowDefinitionRegistry,
                logoutFlowDefinitionRegistry, saml2ClientLogoutAction, applicationContext, casProperties);
        w.initialize();
        return w;
    }

    @Bean
    public ErrorViewResolver pac4jErrorViewResolver() {
        return new Pac4jErrorViewResolver();
    }

    @RefreshScope
    @Bean
    @Autowired
    @ConditionalOnMissingBean(name = "Pac4jInitialFlowSetupAction")
    public Action Pac4jInitialFlowSetupAction(@Qualifier("argumentExtractor") final ArgumentExtractor argumentExtractor) {
        return new Pac4jInitialFlowSetupAction(
            CollectionUtils.wrap(argumentExtractor),
            servicesManager,
            authenticationRequestServiceSelectionStrategies,
            casProperties
        );
    }
}
