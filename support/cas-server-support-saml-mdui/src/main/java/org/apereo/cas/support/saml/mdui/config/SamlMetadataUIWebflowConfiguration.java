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
import org.apereo.cas.web.flow.CasWebflowExecutionPlanConfigurer;

import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.DependsOn;
import org.springframework.webflow.definition.registry.FlowDefinitionRegistry;
import org.springframework.webflow.engine.builder.support.FlowBuilderServices;
import org.springframework.webflow.execution.Action;

/**
 * This is {@link SamlMetadataUIWebflowConfiguration}.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 */
@Configuration("samlMetadataUIWebflowConfiguration")
@EnableConfigurationProperties(CasConfigurationProperties.class)
public class SamlMetadataUIWebflowConfiguration {

    @Autowired
    private CasConfigurationProperties casProperties;

    @Autowired
    @Qualifier("loginFlowRegistry")
    private ObjectProvider<FlowDefinitionRegistry> loginFlowDefinitionRegistry;

    @Autowired
    private ObjectProvider<FlowBuilderServices> flowBuilderServices;

    @Autowired
    @Qualifier("servicesManager")
    private ObjectProvider<ServicesManager> servicesManager;

    @Autowired
    @Qualifier("webApplicationServiceFactory")
    private ServiceFactory<WebApplicationService> serviceFactory;

    @Autowired
    private ConfigurableApplicationContext applicationContext;

    @Autowired
    @Qualifier("chainingSamlMetadataUIMetadataResolverAdapter")
    private ObjectProvider<MetadataResolverAdapter> chainingSamlMetadataUIMetadataResolverAdapter;

    @ConditionalOnMissingBean(name = "samlMetadataUIWebConfigurer")
    @Bean
    @DependsOn("defaultWebflowConfigurer")
    public CasWebflowConfigurer samlMetadataUIWebConfigurer() {
        return new SamlMetadataUIWebflowConfigurer(flowBuilderServices.getObject(),
            loginFlowDefinitionRegistry.getObject(), samlMetadataUIParserAction(),
            applicationContext, casProperties);
    }

    @ConditionalOnMissingBean(name = "samlMetadataUIParserAction")
    @Bean
    public Action samlMetadataUIParserAction() {
        val parameter = StringUtils.defaultIfEmpty(casProperties.getSamlMetadataUi().getParameter(), SamlProtocolConstants.PARAMETER_ENTITY_ID);
        return new SamlMetadataUIParserAction(parameter, chainingSamlMetadataUIMetadataResolverAdapter.getObject(), serviceFactory, servicesManager.getObject());
    }

    @Bean
    @ConditionalOnMissingBean(name = "samlMetadataUICasWebflowExecutionPlanConfigurer")
    public CasWebflowExecutionPlanConfigurer samlMetadataUICasWebflowExecutionPlanConfigurer() {
        return plan -> plan.registerWebflowConfigurer(samlMetadataUIWebConfigurer());
    }
}
