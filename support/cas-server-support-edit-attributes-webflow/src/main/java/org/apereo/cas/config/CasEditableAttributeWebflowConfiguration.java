package org.apereo.cas.config;

import org.apereo.cas.attributes.DefaultEditableAttributeRepository;
import org.apereo.cas.attributes.DefaultEditableAttributeValueRepository;
import org.apereo.cas.attributes.DefaultEditableAttributeValueValidator;
import org.apereo.cas.attributes.EditableAttributeRepository;
import org.apereo.cas.attributes.EditableAttributeValueRepository;
import org.apereo.cas.attributes.EditableAttributeValueValidator;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.ticket.registry.TicketRegistrySupport;
import org.apereo.cas.web.flow.CasWebflowConfigurer;
import org.apereo.cas.web.flow.EditableAttributeFormAction;
import org.apereo.cas.web.flow.EditableAttributeWebflowConfigurer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.webflow.definition.registry.FlowDefinitionRegistry;
import org.springframework.webflow.engine.builder.support.FlowBuilderServices;
import org.springframework.webflow.execution.Action;

/**
 * This is {@link CasEditableAttributeWebflowConfiguration}.
 *
 * @author Marcus Watkins
 * @since 5.2.0
 */
@Configuration("casEditableAttributesWebflowConfiguration")
@EnableConfigurationProperties(CasConfigurationProperties.class)
public class CasEditableAttributeWebflowConfiguration {

    @Autowired
    @Qualifier("loginFlowRegistry")
    private FlowDefinitionRegistry loginFlowDefinitionRegistry;

    @Autowired
    private FlowBuilderServices flowBuilderServices;

    @Autowired
    private ApplicationContext applicationContext;

    @Autowired
    private CasConfigurationProperties casProperties;

    @Autowired
    @Qualifier("defaultTicketRegistrySupport")
    private TicketRegistrySupport ticketRegistrySupport;

    @Autowired
    @Bean
    public Action editableAttributeFormAction(
            @Qualifier("editableAttributeValueRepository") final EditableAttributeValueRepository valueRepository,
            @Qualifier("editableAttributeRepository") final EditableAttributeRepository attributeRepository,
            @Qualifier("editableAttributeValueValidator") final EditableAttributeValueValidator validator) {
        return new EditableAttributeFormAction(valueRepository, attributeRepository, validator);
    }

    @ConditionalOnMissingBean(name = "editableAttributeWebflowConfigurer")
    @Bean
    public CasWebflowConfigurer editableAttributeWebflowConfigurer() {
        final CasWebflowConfigurer w = new EditableAttributeWebflowConfigurer(flowBuilderServices,
                loginFlowDefinitionRegistry, applicationContext, casProperties);
        w.initialize();
        return w;
    }

    @ConditionalOnMissingBean(name = "editableAttributeRepository")
    @Bean
    public DefaultEditableAttributeRepository editableAttributeRepository() {
        return new DefaultEditableAttributeRepository(casProperties.getEditableAttribute());
    }

    @ConditionalOnMissingBean(name = "editableAttributeValueRepository")
    @Bean
    public EditableAttributeValueRepository editableAttributeValueRepository() {
        return new DefaultEditableAttributeValueRepository(ticketRegistrySupport);
    }
    
    @ConditionalOnMissingBean(name = "editableAttributeValueValidator")
    @Bean
    public DefaultEditableAttributeValueValidator editableAttributeValueValidator() {
        return new DefaultEditableAttributeValueValidator();
    }
    
}
