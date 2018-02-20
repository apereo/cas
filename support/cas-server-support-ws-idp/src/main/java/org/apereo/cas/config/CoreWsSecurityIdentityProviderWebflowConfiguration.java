package org.apereo.cas.config;

import org.apereo.cas.authentication.AuthenticationServiceSelectionStrategy;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.services.ServicesManager;
import org.apereo.cas.web.flow.CasWebflowConfigurer;
import org.apereo.cas.web.flow.config.CasCoreWebflowConfiguration;
import org.apereo.cas.ws.idp.web.flow.WSFederationMetadataUIAction;
import org.apereo.cas.ws.idp.web.flow.WSFederationWebflowConfigurer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.ApplicationContext;
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
    private ApplicationContext applicationContext;

    @Autowired
    private FlowBuilderServices flowBuilderServices;

    @Autowired
    @Qualifier("servicesManager")
    private ServicesManager servicesManager;

    @Autowired
    private CasConfigurationProperties casProperties;

    @Autowired
    @Qualifier("loginFlowRegistry")
    private FlowDefinitionRegistry loginFlowDefinitionRegistry;

    @Autowired
    @Qualifier("wsFederationAuthenticationServiceSelectionStrategy")
    private AuthenticationServiceSelectionStrategy wsFederationAuthenticationServiceSelectionStrategy;

    @Bean
    @RefreshScope
    public Action wsFederationMetadataUIAction() {
        return new WSFederationMetadataUIAction(servicesManager, wsFederationAuthenticationServiceSelectionStrategy);
    }

    @ConditionalOnMissingBean(name = "wsFederationWebflowConfigurer")
    @Bean
    @DependsOn("defaultWebflowConfigurer")
    public CasWebflowConfigurer wsFederationWebflowConfigurer() {
        final CasWebflowConfigurer w = new WSFederationWebflowConfigurer(flowBuilderServices,
            loginFlowDefinitionRegistry, wsFederationMetadataUIAction(), applicationContext, casProperties);
        w.initialize();
        return w;
    }
}
