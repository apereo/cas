package org.apereo.cas.support.saml.mdui.config;

import org.apereo.cas.authentication.principal.ServiceFactory;
import org.apereo.cas.authentication.principal.WebApplicationService;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.services.ServicesManager;
import org.apereo.cas.support.saml.SamlProtocolConstants;
import org.apereo.cas.support.saml.mdui.MetadataResolverAdapter;
import org.apereo.cas.support.saml.mdui.web.flow.SamlMetadataUIParserAction;
import org.apereo.cas.support.saml.mdui.web.flow.SamlMetadataUIWebflowConfigurer;
import org.apereo.cas.web.flow.CasWebflowConfigurer;
import org.apereo.cas.web.flow.CasWebflowConstants;
import org.apereo.cas.web.flow.CasWebflowExecutionPlanConfigurer;

import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.webflow.definition.registry.FlowDefinitionRegistry;
import org.springframework.webflow.engine.builder.support.FlowBuilderServices;
import org.springframework.webflow.execution.Action;

/**
 * This is {@link SamlMetadataUIWebflowConfiguration}.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 */
@Configuration(value = "samlMetadataUIWebflowConfiguration", proxyBeanMethods = false)
@EnableConfigurationProperties(CasConfigurationProperties.class)
public class SamlMetadataUIWebflowConfiguration {

    @ConditionalOnMissingBean(name = "samlMetadataUIWebConfigurer")
    @Bean
    public CasWebflowConfigurer samlMetadataUIWebConfigurer(
        final ConfigurableApplicationContext applicationContext,
        final CasConfigurationProperties casProperties,
        @Qualifier(CasWebflowConstants.BEAN_NAME_LOGIN_FLOW_DEFINITION_REGISTRY)
        final FlowDefinitionRegistry loginFlowDefinitionRegistry,
        @Qualifier(CasWebflowConstants.BEAN_NAME_FLOW_BUILDER_SERVICES)
        final FlowBuilderServices flowBuilderServices) {
        return new SamlMetadataUIWebflowConfigurer(flowBuilderServices,
            loginFlowDefinitionRegistry, applicationContext, casProperties);
    }

    @ConditionalOnMissingBean(name = "samlMetadataUIParserAction")
    @Bean
    @Autowired
    public Action samlMetadataUIParserAction(
        @Qualifier("chainingSamlMetadataUIMetadataResolverAdapter")
        final MetadataResolverAdapter chainingSamlMetadataUIMetadataResolverAdapter,
        @Qualifier(ServicesManager.BEAN_NAME)
        final ServicesManager servicesManager,
        final CasConfigurationProperties casProperties,
        @Qualifier("webApplicationServiceFactory")
        final ServiceFactory<WebApplicationService> serviceFactory) {
        val parameter = StringUtils.defaultIfEmpty(casProperties.getSamlMetadataUi().getParameter(), SamlProtocolConstants.PARAMETER_ENTITY_ID);
        return new SamlMetadataUIParserAction(parameter, chainingSamlMetadataUIMetadataResolverAdapter, serviceFactory, servicesManager);
    }

    @Bean
    @Autowired
    @ConditionalOnMissingBean(name = "samlMetadataUICasWebflowExecutionPlanConfigurer")
    public CasWebflowExecutionPlanConfigurer samlMetadataUICasWebflowExecutionPlanConfigurer(
        @Qualifier("samlMetadataUIWebConfigurer")
        final CasWebflowConfigurer samlMetadataUIWebConfigurer) {
        return plan -> plan.registerWebflowConfigurer(samlMetadataUIWebConfigurer);
    }
}
