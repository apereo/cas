package org.apereo.cas.config;

import org.apache.commons.lang3.StringUtils;
import org.apereo.cas.attributes.EditableAttributeValueRepository;
import org.apereo.cas.attributes.JdbcEditableAttributeValueRepository;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.configuration.model.support.attributes.EditableAttributeProperties;
import org.apereo.cas.configuration.support.JpaBeans;
import org.apereo.cas.ticket.registry.TicketRegistrySupport;
import org.springframework.beans.factory.BeanCreationException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * This is {@link CasEditableAttributeJdbcConfiguration} that stores editable
 * attributes in a jdbc database.
 *
 * @author Marcus Watkins
 * @since 5.2.0
 */
@Configuration("casEditableAttributesJdbcConfiguration")
@EnableConfigurationProperties(CasConfigurationProperties.class)
public class CasEditableAttributeJdbcConfiguration {

    @Autowired
    @Qualifier("defaultTicketRegistrySupport")
    private TicketRegistrySupport ticketRegistrySupport;

    @Autowired
    private CasConfigurationProperties casProperties;

    @RefreshScope
    @Bean
    public EditableAttributeValueRepository editableAttributeValueRepository() {
        final EditableAttributeProperties.Jdbc jdbc = casProperties.getEditableAttribute().getJdbc();

        if (StringUtils.isBlank(jdbc.getTableName())) {
            throw new BeanCreationException("Database table for editable attributes must be specified.");
        }

        return new JdbcEditableAttributeValueRepository(ticketRegistrySupport, JpaBeans.newDataSource(jdbc),
                jdbc.getTableName());
    }
}
