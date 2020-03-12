package org.apereo.cas.config;

import org.apereo.cas.authentication.AuthenticationServiceSelectionPlan;
import org.apereo.cas.authentication.attribute.AttributeDefinitionStore;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.consent.ConsentableAttributeBuilder;
import org.apereo.cas.services.ServicesManager;
import org.apereo.cas.support.saml.services.idp.metadata.cache.SamlRegisteredServiceCachingMetadataResolver;
import org.apereo.cas.support.saml.web.flow.SamlIdPMetadataUIAction;
import org.apereo.cas.support.saml.web.flow.SamlIdPMetadataUIWebflowConfigurer;
import org.apereo.cas.support.saml.web.idp.profile.builders.attr.SamlIdPAttributeDefinition;
import org.apereo.cas.web.flow.CasWebflowConfigurer;
import org.apereo.cas.web.flow.CasWebflowExecutionPlanConfigurer;

import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
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
    private ConfigurableApplicationContext applicationContext;

    @Autowired
    private CasConfigurationProperties casProperties;

    @Autowired
    @Qualifier("authenticationServiceSelectionPlan")
    private ObjectProvider<AuthenticationServiceSelectionPlan> selectionStrategies;

    @Autowired
    @Qualifier("defaultSamlRegisteredServiceCachingMetadataResolver")
    private ObjectProvider<SamlRegisteredServiceCachingMetadataResolver> defaultSamlRegisteredServiceCachingMetadataResolver;

    @Autowired
    @Qualifier("attributeDefinitionStore")
    private ObjectProvider<AttributeDefinitionStore> attributeDefinitionStore;

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

    @Configuration(value = "SamlIdPConsentWebflowConfiguration", proxyBeanMethods = false)
    @ConditionalOnClass(value = ConsentableAttributeBuilder.class)
    public class SamlIdPConsentWebflowConfiguration {

        @Bean
        @ConditionalOnMissingBean(name = "samlIdPConsentableAttributeBuilder")
        @RefreshScope
        public ConsentableAttributeBuilder samlIdPConsentableAttributeBuilder() {
            return attribute -> {
                val result = attributeDefinitionStore.getObject()
                    .locateAttributeDefinition(defn -> {
                        if (defn instanceof SamlIdPAttributeDefinition) {
                            val samlAttr = SamlIdPAttributeDefinition.class.cast(defn);
                            return samlAttr.getName().equalsIgnoreCase(attribute.getName())
                                && StringUtils.isNotBlank(samlAttr.getFriendlyName());
                        }
                        return false;
                    });

                if (result.isPresent()) {
                    val samlAttr = SamlIdPAttributeDefinition.class.cast(result.get());
                    attribute.setFriendlyName(samlAttr.getFriendlyName());
                }
                return attribute;
            };
        }
    }
}
