package org.apereo.cas.web.config;

import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.configuration.model.support.aup.AcceptableUsagePolicyProperties;
import org.apereo.cas.configuration.support.Beans;
import org.apereo.cas.ticket.registry.TicketRegistrySupport;
import org.apereo.cas.web.flow.AcceptableUsagePolicyRepository;
import org.apereo.cas.web.flow.LdapAcceptableUsagePolicyRepository;
import org.ldaptive.ConnectionFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * This is {@link CasSupportActionsAcceptableUsagePolicyLdapConfiguration}.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 */
@Configuration("casSupportActionsAcceptableUsagePolicyLdapConfiguration")
@EnableConfigurationProperties(CasConfigurationProperties.class)
public class CasSupportActionsAcceptableUsagePolicyLdapConfiguration {

    @Autowired
    @Qualifier("defaultTicketRegistrySupport")
    private TicketRegistrySupport ticketRegistrySupport;

    @Autowired
    private CasConfigurationProperties casProperties;

    @RefreshScope
    @Bean
    public AcceptableUsagePolicyRepository acceptableUsagePolicyRepository() {
        final AcceptableUsagePolicyProperties.Ldap ldap = casProperties.getAcceptableUsagePolicy().getLdap();
        final ConnectionFactory connectionFactory = Beans.newLdaptivePooledConnectionFactory(ldap);
        final LdapAcceptableUsagePolicyRepository r = new LdapAcceptableUsagePolicyRepository(ticketRegistrySupport, 
                connectionFactory, ldap.getUserFilter(), ldap.getBaseDn());
        r.setAupAttributeName(casProperties.getAcceptableUsagePolicy().getAupAttributeName());
        return r;
    }
}
