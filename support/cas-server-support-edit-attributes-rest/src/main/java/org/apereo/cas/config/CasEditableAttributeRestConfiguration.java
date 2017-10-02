package org.apereo.cas.config;

import org.apereo.cas.attributes.EditableAttributeValueRepository;
import org.apereo.cas.attributes.RestEditableAttributeValueRepository;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.configuration.model.support.attributes.EditableAttributeProperties;
import org.apereo.cas.ticket.registry.TicketRegistrySupport;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * This is {@link CasEditableAttributeRestConfiguration}.
 *
 * @author Marcus Watkins
 * @since 5.2.0
 */
@Configuration("casEditableAttributesRestConfiguration")
@EnableConfigurationProperties(CasConfigurationProperties.class)
public class CasEditableAttributeRestConfiguration {

    @Autowired
    @Qualifier("defaultTicketRegistrySupport")
    private TicketRegistrySupport ticketRegistrySupport;

    @Autowired
    private CasConfigurationProperties casProperties;

    @RefreshScope
    @Bean
    public EditableAttributeValueRepository editableAttributeValueRepository() {
        final EditableAttributeProperties eap = casProperties.getEditableAttribute();
        return new RestEditableAttributeValueRepository(ticketRegistrySupport, eap.getRest());
    }
}
