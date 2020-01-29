package org.apereo.cas.config;

import org.apereo.cas.authentication.AuthenticationServiceSelectionStrategy;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.services.ServicesManager;
import org.apereo.cas.web.flow.CasWebflowConfigurer;
import org.apereo.cas.web.flow.CasWebflowExecutionPlanConfigurer;
import org.apereo.cas.web.flow.config.CasCoreWebflowConfiguration;
import org.apereo.cas.ws.idp.web.flow.WSFederationIdentityProviderWebflowConfigurer;
import org.apereo.cas.ws.idp.web.flow.WSFederationMetadataUIAction;

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
 * This is {@link CoreWsSecurityIdentityProviderWebflowConfiguration}.
 *
 * @author Misagh Moayyed
 * @since 5.3.0
 */
@Configuration("coreWsSecurityIdentityProviderWebflowConfiguration")
@EnableConfigurationProperties(CasConfigurationProperties.class)
@AutoConfigureAfter(CasCoreWebflowConfiguration.class)
public class CoreWsSecurityIdentityProviderWebflowConfiguration {

    @Autowired
    private ConfigurableApplicationContext applicationContext;

    @Autowired
    private ObjectProvider<FlowBuilderServices> flowBuilderServices;

    @Autowired
    @Qualifier("servicesManager")
    private ObjectProvider<ServicesManager> servicesManager;

    @Autowired
    private CasConfigurationProperties casProperties;

    @Autowired
    @Qualifier("loginFlowRegistry")
    private ObjectProvider<FlowDefinitionRegistry> loginFlowDefinitionRegistry;

    @Autowired
    @Qualifier("wsFederationAuthenticationServiceSelectionStrategy")
    private ObjectProvider<AuthenticationServiceSelectionStrategy> wsFederationAuthenticationServiceSelectionStrategy;

    @Bean
    @RefreshScope
    public Action wsFederationMetadataUIAction() {
        return new WSFederationMetadataUIAction(servicesManager.getObject(), wsFederationAuthenticationServiceSelectionStrategy.getObject());
    }

    @ConditionalOnMissingBean(name = "wsFederationWebflowConfigurer")
    @Bean
    @DependsOn("defaultWebflowConfigurer")
    public CasWebflowConfigurer wsFederationWebflowConfigurer() {
        return new WSFederationIdentityProviderWebflowConfigurer(flowBuilderServices.getObject(),
            loginFlowDefinitionRegistry.getObject(), wsFederationMetadataUIAction(),
            applicationContext, casProperties);
    }

    @Bean
    @ConditionalOnMissingBean(name = "wsFederationCasWebflowExecutionPlanConfigurer")
    public CasWebflowExecutionPlanConfigurer wsFederationCasWebflowExecutionPlanConfigurer() {
        return plan -> plan.registerWebflowConfigurer(wsFederationWebflowConfigurer());
    }
}
