package org.apereo.cas.config;

import org.apereo.cas.authentication.AuthenticationServiceSelectionStrategy;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.services.ServicesManager;
import org.apereo.cas.web.flow.CasWebflowConfigurer;
import org.apereo.cas.web.flow.CasWebflowConstants;
import org.apereo.cas.web.flow.CasWebflowExecutionPlanConfigurer;
import org.apereo.cas.web.flow.config.CasCoreWebflowConfiguration;
import org.apereo.cas.ws.idp.web.flow.WSFederationIdentityProviderWebflowConfigurer;
import org.apereo.cas.ws.idp.web.flow.WSFederationMetadataUIAction;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
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
 * This is {@link CoreWsSecurityIdentityProviderWebflowConfiguration}.
 *
 * @author Misagh Moayyed
 * @since 5.3.0
 */
@Configuration(value = "coreWsSecurityIdentityProviderWebflowConfiguration", proxyBeanMethods = false)
@EnableConfigurationProperties(CasConfigurationProperties.class)
@AutoConfigureAfter(CasCoreWebflowConfiguration.class)
public class CoreWsSecurityIdentityProviderWebflowConfiguration {

    @Autowired
    @Bean
    @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
    public Action wsFederationMetadataUIAction(
        @Qualifier(ServicesManager.BEAN_NAME)
        final ServicesManager servicesManager,
        @Qualifier("wsFederationAuthenticationServiceSelectionStrategy")
        final AuthenticationServiceSelectionStrategy wsFederationAuthenticationServiceSelectionStrategy) {
        return new WSFederationMetadataUIAction(servicesManager, wsFederationAuthenticationServiceSelectionStrategy);
    }

    @ConditionalOnMissingBean(name = "wsFederationWebflowConfigurer")
    @Bean
    @Autowired
    public CasWebflowConfigurer wsFederationWebflowConfigurer(
        final ConfigurableApplicationContext applicationContext,
        final CasConfigurationProperties casProperties,
        @Qualifier(CasWebflowConstants.BEAN_NAME_LOGIN_FLOW_DEFINITION_REGISTRY)
        final FlowDefinitionRegistry loginFlowDefinitionRegistry,
        @Qualifier(CasWebflowConstants.BEAN_NAME_FLOW_BUILDER_SERVICES)
        final FlowBuilderServices flowBuilderServices) {
        return new WSFederationIdentityProviderWebflowConfigurer(flowBuilderServices,
            loginFlowDefinitionRegistry, applicationContext, casProperties);
    }

    @Bean
    @Autowired
    @ConditionalOnMissingBean(name = "wsFederationCasWebflowExecutionPlanConfigurer")
    public CasWebflowExecutionPlanConfigurer wsFederationCasWebflowExecutionPlanConfigurer(
        @Qualifier("wsFederationWebflowConfigurer")
        final CasWebflowConfigurer wsFederationWebflowConfigurer) {
        return plan -> plan.registerWebflowConfigurer(wsFederationWebflowConfigurer);
    }
}
