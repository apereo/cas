package org.apereo.cas.config;

import module java.base;
import org.apereo.cas.authentication.principal.ServiceFactory;
import org.apereo.cas.authentication.principal.WebApplicationService;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.configuration.features.CasFeatureModule;
import org.apereo.cas.services.ServicesManager;
import org.apereo.cas.support.saml.SamlProtocolConstants;
import org.apereo.cas.support.saml.mdui.MetadataResolverAdapter;
import org.apereo.cas.support.saml.mdui.web.flow.SamlMetadataUIParserAction;
import org.apereo.cas.support.saml.mdui.web.flow.SamlMetadataUIWebflowConfigurer;
import org.apereo.cas.util.spring.boot.ConditionalOnFeatureEnabled;
import org.apereo.cas.web.flow.CasWebflowConfigurer;
import org.apereo.cas.web.flow.CasWebflowConstants;
import org.apereo.cas.web.flow.CasWebflowExecutionPlanConfigurer;
import org.apereo.cas.web.flow.actions.WebflowActionBeanSupplier;
import org.apereo.cas.web.support.ArgumentExtractor;
import lombok.val;
import org.apache.commons.lang3.StringUtils;
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
 * This is {@link SamlMetadataUIWebflowConfiguration}.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 */
@EnableConfigurationProperties(CasConfigurationProperties.class)
@ConditionalOnFeatureEnabled(feature = CasFeatureModule.FeatureCatalog.SAML)
@Configuration(value = "SamlMetadataUIWebflowConfiguration", proxyBeanMethods = false)
class SamlMetadataUIWebflowConfiguration {

    @ConditionalOnMissingBean(name = "samlMetadataUIWebConfigurer")
    @Bean
    @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
    public CasWebflowConfigurer samlMetadataUIWebConfigurer(
        final ConfigurableApplicationContext applicationContext,
        final CasConfigurationProperties casProperties,
        @Qualifier(CasWebflowConstants.BEAN_NAME_FLOW_DEFINITION_REGISTRY)
        final FlowDefinitionRegistry flowDefinitionRegistry,
        @Qualifier(CasWebflowConstants.BEAN_NAME_FLOW_BUILDER_SERVICES)
        final FlowBuilderServices flowBuilderServices) {
        return new SamlMetadataUIWebflowConfigurer(flowBuilderServices,
            flowDefinitionRegistry, applicationContext, casProperties);
    }

    @ConditionalOnMissingBean(name = CasWebflowConstants.ACTION_ID_SAML_METADATA_UI_PARSER)
    @Bean
    @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
    public Action samlMetadataUIParserAction(
        final ConfigurableApplicationContext applicationContext,
        @Qualifier("chainingSamlMetadataUIMetadataResolverAdapter")
        final MetadataResolverAdapter chainingSamlMetadataUIMetadataResolverAdapter,
        @Qualifier(ServicesManager.BEAN_NAME)
        final ServicesManager servicesManager,
        final CasConfigurationProperties casProperties,
        @Qualifier(ArgumentExtractor.BEAN_NAME)
        final ArgumentExtractor argumentExtractor,
        @Qualifier(WebApplicationService.BEAN_NAME_FACTORY)
        final ServiceFactory<WebApplicationService> serviceFactory) {
        return WebflowActionBeanSupplier.builder()
            .withApplicationContext(applicationContext)
            .withProperties(casProperties)
            .withAction(() -> {
                val parameter = StringUtils.defaultIfEmpty(casProperties.getSamlMetadataUi().getParameter(), SamlProtocolConstants.PARAMETER_ENTITY_ID);
                return new SamlMetadataUIParserAction(parameter, chainingSamlMetadataUIMetadataResolverAdapter,
                    serviceFactory, servicesManager, argumentExtractor);
            })
            .withId(CasWebflowConstants.ACTION_ID_SAML_METADATA_UI_PARSER)
            .build()
            .get();
    }

    @Bean
    @ConditionalOnMissingBean(name = "samlMetadataUICasWebflowExecutionPlanConfigurer")
    @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
    public CasWebflowExecutionPlanConfigurer samlMetadataUICasWebflowExecutionPlanConfigurer(
        @Qualifier("samlMetadataUIWebConfigurer")
        final CasWebflowConfigurer samlMetadataUIWebConfigurer) {
        return plan -> plan.registerWebflowConfigurer(samlMetadataUIWebConfigurer);
    }
}
