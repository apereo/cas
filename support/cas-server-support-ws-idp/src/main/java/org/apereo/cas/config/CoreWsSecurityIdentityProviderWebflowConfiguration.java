package org.apereo.cas.config;

import module java.base;
import org.apereo.cas.authentication.AuthenticationServiceSelectionStrategy;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.configuration.features.CasFeatureModule;
import org.apereo.cas.services.ServicesManager;
import org.apereo.cas.util.spring.boot.ConditionalOnFeatureEnabled;
import org.apereo.cas.web.flow.CasWebflowConfigurer;
import org.apereo.cas.web.flow.CasWebflowConstants;
import org.apereo.cas.web.flow.CasWebflowExecutionPlanConfigurer;
import org.apereo.cas.web.flow.actions.WebflowActionBeanSupplier;
import org.apereo.cas.ws.idp.web.flow.WSFederationIdentityProviderWebflowConfigurer;
import org.apereo.cas.ws.idp.web.flow.WSFederationMetadataUIAction;
import org.springframework.beans.factory.annotation.Qualifier;
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
@EnableConfigurationProperties(CasConfigurationProperties.class)
@ConditionalOnFeatureEnabled(feature = CasFeatureModule.FeatureCatalog.WsFederationIdentityProvider)
@Configuration(value = "CoreWsSecurityIdentityProviderWebflowConfiguration", proxyBeanMethods = false)
class CoreWsSecurityIdentityProviderWebflowConfiguration {

    @Bean
    @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
    @ConditionalOnMissingBean(name = CasWebflowConstants.ACTION_ID_WSFEDERATION_METADATA_UI)
    public Action wsFederationMetadataUIAction(
        final ConfigurableApplicationContext applicationContext,
        final CasConfigurationProperties casProperties,
        @Qualifier(ServicesManager.BEAN_NAME)
        final ServicesManager servicesManager,
        @Qualifier("wsFederationAuthenticationServiceSelectionStrategy")
        final AuthenticationServiceSelectionStrategy wsFederationAuthenticationServiceSelectionStrategy) {
        return WebflowActionBeanSupplier.builder()
            .withApplicationContext(applicationContext)
            .withProperties(casProperties)
            .withAction(() -> new WSFederationMetadataUIAction(servicesManager, wsFederationAuthenticationServiceSelectionStrategy))
            .withId(CasWebflowConstants.ACTION_ID_WSFEDERATION_METADATA_UI)
            .build()
            .get();
    }

    @ConditionalOnMissingBean(name = "wsFederationWebflowConfigurer")
    @Bean
    @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
    public CasWebflowConfigurer wsFederationWebflowConfigurer(
        final ConfigurableApplicationContext applicationContext,
        final CasConfigurationProperties casProperties,
        @Qualifier(CasWebflowConstants.BEAN_NAME_FLOW_DEFINITION_REGISTRY)
        final FlowDefinitionRegistry flowDefinitionRegistry,
        @Qualifier(CasWebflowConstants.BEAN_NAME_FLOW_BUILDER_SERVICES)
        final FlowBuilderServices flowBuilderServices) {
        return new WSFederationIdentityProviderWebflowConfigurer(flowBuilderServices,
            flowDefinitionRegistry, applicationContext, casProperties);
    }

    @Bean
    @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
    @ConditionalOnMissingBean(name = "wsFederationCasWebflowExecutionPlanConfigurer")
    public CasWebflowExecutionPlanConfigurer wsFederationCasWebflowExecutionPlanConfigurer(
        @Qualifier("wsFederationWebflowConfigurer")
        final CasWebflowConfigurer wsFederationWebflowConfigurer) {
        return plan -> plan.registerWebflowConfigurer(wsFederationWebflowConfigurer);
    }
}
