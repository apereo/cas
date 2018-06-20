package org.apereo.cas.config;

import lombok.extern.slf4j.Slf4j;
import org.apereo.cas.aup.AcceptableUsagePolicyRepository;
import org.apereo.cas.aup.LdapAcceptableUsagePolicyRepository;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.ticket.registry.TicketRegistrySupport;
import org.apereo.cas.util.LdapUtils;
import org.ldaptive.ConnectionFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * This is {@link CasAcceptableUsagePolicyLdapConfiguration}.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 */
@Configuration("casAcceptableUsagePolicyLdapConfiguration")
@EnableConfigurationProperties(CasConfigurationProperties.class)
@ConditionalOnProperty(prefix = "cas.acceptableUsagePolicy", name = "enabled", havingValue = "true", matchIfMissing = true)
@Slf4j
public class CasAcceptableUsagePolicyLdapConfiguration {

    @Autowired
    @Qualifier("defaultTicketRegistrySupport")
    private TicketRegistrySupport ticketRegistrySupport;

    @Autowired
    private CasConfigurationProperties casProperties;

    @RefreshScope
    @Bean
    public AcceptableUsagePolicyRepository acceptableUsagePolicyRepository() {
        final var ldap = casProperties.getAcceptableUsagePolicy().getLdap();
        final ConnectionFactory connectionFactory = LdapUtils.newLdaptivePooledConnectionFactory(ldap);
        return new LdapAcceptableUsagePolicyRepository(ticketRegistrySupport,
                casProperties.getAcceptableUsagePolicy().getAupAttributeName(),
                connectionFactory, ldap.getSearchFilter(), ldap.getBaseDn());
    }
}
