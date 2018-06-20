package org.apereo.cas.config;

import lombok.extern.slf4j.Slf4j;
import org.apereo.cas.aup.AcceptableUsagePolicyRepository;
import org.apereo.cas.aup.RestAcceptableUsagePolicyRepository;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.ticket.registry.TicketRegistrySupport;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * This is {@link CasAcceptableUsagePolicyRestConfiguration}.
 *
 * @author Misagh Moayyed
 * @since 5.2.0
 */
@Configuration("casAcceptableUsagePolicyRestConfiguration")
@EnableConfigurationProperties(CasConfigurationProperties.class)
@ConditionalOnProperty(prefix = "cas.acceptableUsagePolicy", name = "enabled", havingValue = "true", matchIfMissing = true)
@Slf4j
public class CasAcceptableUsagePolicyRestConfiguration {

    @Autowired
    @Qualifier("defaultTicketRegistrySupport")
    private TicketRegistrySupport ticketRegistrySupport;

    @Autowired
    private CasConfigurationProperties casProperties;

    @RefreshScope
    @Bean
    public AcceptableUsagePolicyRepository acceptableUsagePolicyRepository() {
        final var aup = casProperties.getAcceptableUsagePolicy();
        return new RestAcceptableUsagePolicyRepository(ticketRegistrySupport, aup.getAupAttributeName(), aup.getRest());
    }
}
