package org.apereo.cas.support.saml.mdui.config;

import org.apache.commons.lang3.StringUtils;
import org.apereo.cas.authentication.principal.ServiceFactory;
import org.apereo.cas.authentication.principal.WebApplicationService;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.services.ServicesManager;
import org.apereo.cas.support.saml.SamlProtocolConstants;
import org.apereo.cas.support.saml.mdui.MetadataResolverAdapter;
import org.apereo.cas.support.saml.mdui.web.flow.SamlMetadataUIParserAction;
import org.apereo.cas.support.saml.mdui.web.flow.SamlMetadataUIWebflowConfigurer;
import org.apereo.cas.web.flow.CasWebflowConfigurer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
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

    @Autowired(required = false)
    @Qualifier("loginFlowRegistry")
    private FlowDefinitionRegistry loginFlowDefinitionRegistry;

    @Autowired(required = false)
    private FlowBuilderServices flowBuilderServices;

    @Autowired
    @Qualifier("servicesManager")
    private ServicesManager servicesManager;

    @Autowired
    @Qualifier("webApplicationServiceFactory")
    private ServiceFactory<WebApplicationService> serviceFactory;

    @Autowired
    private ApplicationContext applicationContext;

    @Autowired
    @Qualifier("chainingSamlMetadataUIMetadataResolverAdapter")
    private MetadataResolverAdapter chainingSamlMetadataUIMetadataResolverAdapter;

    @ConditionalOnMissingBean(name = "samlMetadataUIWebConfigurer")
    @ConditionalOnBean(name = "defaultWebflowConfigurer")
    @Bean
    @DependsOn("defaultWebflowConfigurer")
    public CasWebflowConfigurer samlMetadataUIWebConfigurer() {
        final CasWebflowConfigurer w = new SamlMetadataUIWebflowConfigurer(flowBuilderServices,
                loginFlowDefinitionRegistry, samlMetadataUIParserAction(), applicationContext, casProperties);
        w.initialize();
        return w;
    }

    @ConditionalOnMissingBean(name = "samlMetadataUIParserAction")
    @Bean
    public Action samlMetadataUIParserAction() {
        final String parameter = StringUtils.defaultIfEmpty(casProperties.getSamlMetadataUi().getParameter(), SamlProtocolConstants.PARAMETER_ENTITY_ID);
        return new SamlMetadataUIParserAction(parameter, chainingSamlMetadataUIMetadataResolverAdapter, serviceFactory, servicesManager);
    }
}
