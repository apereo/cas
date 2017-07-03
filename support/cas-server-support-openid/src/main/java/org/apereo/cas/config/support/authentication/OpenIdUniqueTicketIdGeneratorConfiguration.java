package org.apereo.cas.config.support.authentication;

import org.apache.commons.lang3.tuple.Pair;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.support.openid.authentication.principal.OpenIdService;
import org.apereo.cas.ticket.UniqueTicketIdGenerator;
import org.apereo.cas.ticket.UniqueTicketIdGeneratorConfigurer;
import org.apereo.cas.util.CollectionUtils;
import org.apereo.cas.util.HostNameBasedUniqueTicketIdGenerator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.util.Collection;

/**
 * This is {@link OpenIdUniqueTicketIdGeneratorConfiguration}.
 *
 * @author Misagh Moayyed
 * @since 5.1.0
 */
@Configuration("openIdUniqueTicketIdGeneratorConfiguration")
@EnableConfigurationProperties(CasConfigurationProperties.class)
public class OpenIdUniqueTicketIdGeneratorConfiguration implements UniqueTicketIdGeneratorConfigurer {
    @Autowired
    private CasConfigurationProperties casProperties;

    @Override
    public Collection<Pair<String, UniqueTicketIdGenerator>> buildUniqueTicketIdGenerators() {
        return CollectionUtils.wrap(Pair.of(OpenIdService.class.getCanonicalName(),
                new HostNameBasedUniqueTicketIdGenerator.ServiceTicketIdGenerator(
                        casProperties.getTicket().getSt().getMaxLength(),
                        casProperties.getHost().getName())));
    }
}
