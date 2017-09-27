package org.apereo.cas.config;

import org.apereo.cas.attributes.DefaultEditableAttributeValueRepository;
import org.apereo.cas.attributes.EditableAttributeValueRepository;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.ticket.registry.TicketRegistrySupport;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * This is {@link CasEditableAttributeCoreConfiguration}.
 *
 * @author Marcus Watkins
 * @since 5.2.0
 */
@Configuration("casConsentCoreConfiguration")
@EnableConfigurationProperties(CasConfigurationProperties.class)
public class CasEditableAttributeCoreConfiguration {

	
    @Autowired
    @Qualifier("defaultTicketRegistrySupport")
    private TicketRegistrySupport ticketRegistrySupport;

	
    @ConditionalOnMissingBean(name = "editableAttributeRepository")
    @Bean
    public EditableAttributeValueRepository editableAttributeRepository() {
        return new DefaultEditableAttributeValueRepository(ticketRegistrySupport);
    }

}
