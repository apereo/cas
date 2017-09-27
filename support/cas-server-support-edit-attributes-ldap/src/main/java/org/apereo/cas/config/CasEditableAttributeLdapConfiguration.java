package org.apereo.cas.config;

import org.apereo.cas.attributes.EditableAttributeValueRepository;
import org.apereo.cas.attributes.LdapEditableAttributeValueRepository;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.configuration.model.support.attributes.EditableAttributeProperties;
import org.apereo.cas.ticket.registry.TicketRegistrySupport;
import org.apereo.cas.util.LdapUtils;
import org.ldaptive.ConnectionFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * This is {@link CasEditableAttributeLdapConfiguration}.
 *
 * @author Misagh Moayyed
 * @since 5.2.0
 */
@Configuration("casEditableAttributesLdapConfiguration")
@EnableConfigurationProperties(CasConfigurationProperties.class)
public class CasEditableAttributeLdapConfiguration {

    @Autowired
    @Qualifier("defaultTicketRegistrySupport")
    private TicketRegistrySupport ticketRegistrySupport;

    @Autowired
    private CasConfigurationProperties casProperties;

    @RefreshScope
    @Bean
    public EditableAttributeValueRepository editableAttributeValueRepository() {
        final EditableAttributeProperties.Ldap ldap = casProperties.getEditableAttribute().getLdap();
        final ConnectionFactory connectionFactory = LdapUtils.newLdaptivePooledConnectionFactory(ldap);
        return new LdapEditableAttributeValueRepository(ticketRegistrySupport,
                connectionFactory, ldap.getUserFilter(), ldap.getBaseDn());
    }
}
