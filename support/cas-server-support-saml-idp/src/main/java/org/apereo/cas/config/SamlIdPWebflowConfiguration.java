package org.apereo.cas.config;

import org.apereo.cas.authentication.AuthenticationServiceSelectionPlan;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.services.ServicesManager;
import org.apereo.cas.support.saml.services.idp.metadata.cache.SamlRegisteredServiceCachingMetadataResolver;
import org.apereo.cas.support.saml.web.flow.SamlIdPMetadataUIAction;
import org.apereo.cas.support.saml.web.flow.SamlIdPMetadataUIWebflowConfigurer;
import org.apereo.cas.web.flow.CasWebflowConfigurer;
import org.apereo.cas.web.flow.CasWebflowExecutionPlanConfigurer;

import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.DependsOn;
import org.springframework.webflow.definition.registry.FlowDefinitionRegistry;
import org.springframework.webflow.engine.builder.support.FlowBuilderServices;
import org.springframework.webflow.execution.Action;

/**
 * This is {@link SamlIdPWebflowConfiguration}.
 *
 * @author Misagh Moayyed
 * @since 5.1.0
 */
@Configuration("samlIdPWebflowConfiguration")
@EnableConfigurationProperties(CasConfigurationProperties.class)
public class SamlIdPWebflowConfiguration {

    @Autowired
    @Qualifier("servicesManager")
    private ObjectProvider<ServicesManager> servicesManager;

    @Autowired
    @Qualifier("loginFlowRegistry")
    private ObjectProvider<FlowDefinitionRegistry> loginFlowDefinitionRegistry;

    @Autowired
    private ObjectProvider<FlowBuilderServices> flowBuilderServices;

    @Autowired
    private ApplicationContext applicationContext;

    @Autowired
    private CasConfigurationProperties casProperties;

    @Autowired
    @Qualifier("authenticationServiceSelectionPlan")
    private ObjectProvider<AuthenticationServiceSelectionPlan> selectionStrategies;

    @Autowired
    @Qualifier("defaultSamlRegisteredServiceCachingMetadataResolver")
    private ObjectProvider<SamlRegisteredServiceCachingMetadataResolver> defaultSamlRegisteredServiceCachingMetadataResolver;

    @ConditionalOnMissingBean(name = "samlIdPMetadataUIWebConfigurer")
    @Bean
    @DependsOn("defaultWebflowConfigurer")
    public CasWebflowConfigurer samlIdPMetadataUIWebConfigurer() {
        return new SamlIdPMetadataUIWebflowConfigurer(flowBuilderServices.getObject(),
            loginFlowDefinitionRegistry.getObject(),
            samlIdPMetadataUIParserAction(),
            applicationContext,
            casProperties);
    }

    @ConditionalOnMissingBean(name = "samlIdPMetadataUIParserAction")
    @Bean
    public Action samlIdPMetadataUIParserAction() {
        return new SamlIdPMetadataUIAction(servicesManager.getObject(),
            defaultSamlRegisteredServiceCachingMetadataResolver.getObject(),
            selectionStrategies.getObject());
    }

    @Bean
    @ConditionalOnMissingBean(name = "samlIdPCasWebflowExecutionPlanConfigurer")
    public CasWebflowExecutionPlanConfigurer samlIdPCasWebflowExecutionPlanConfigurer() {
        return plan -> plan.registerWebflowConfigurer(samlIdPMetadataUIWebConfigurer());
    }
}
