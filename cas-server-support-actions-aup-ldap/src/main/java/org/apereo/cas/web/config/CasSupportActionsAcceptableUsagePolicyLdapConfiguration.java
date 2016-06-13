package org.apereo.cas.web.config;

import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.ticket.registry.TicketRegistrySupport;
import org.apereo.cas.web.flow.AcceptableUsagePolicyRepository;
import org.apereo.cas.web.flow.LdapAcceptableUsagePolicyRepository;
import org.ldaptive.ConnectionFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.annotation.Resource;

/**
 * This is {@link CasSupportActionsAcceptableUsagePolicyLdapConfiguration}.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 */
@Configuration("casSupportActionsAcceptableUsagePolicyLdapConfiguration")
public class CasSupportActionsAcceptableUsagePolicyLdapConfiguration {

    @Resource(name = "ldapUsagePolicyConnectionFactory")
    private ConnectionFactory connectionFactory;

    @Autowired
    @Qualifier("defaultTicketRegistrySupport")
    private TicketRegistrySupport ticketRegistrySupport;

    @Autowired
    private CasConfigurationProperties casProperties;

    @RefreshScope
    @Bean
    public AcceptableUsagePolicyRepository ldapAcceptableUsagePolicyRepository() {
        final LdapAcceptableUsagePolicyRepository r =
                new LdapAcceptableUsagePolicyRepository();
        r.setBaseDn(casProperties.getAcceptableUsagePolicy().getLdap().getBaseDn());
        r.setConnectionFactory(this.connectionFactory);
        r.setSearchFilter(casProperties.getAcceptableUsagePolicy().getLdap().getSearchFilter());
        r.setAupAttributeName(casProperties.getAcceptableUsagePolicy().getAupAttributeName());
        r.setTicketRegistrySupport(ticketRegistrySupport);
        return r;
    }
}
