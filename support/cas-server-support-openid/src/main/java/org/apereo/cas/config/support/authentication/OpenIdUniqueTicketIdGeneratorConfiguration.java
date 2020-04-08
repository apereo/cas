package org.apereo.cas.config.support.authentication;

import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.support.openid.authentication.principal.OpenIdService;
import org.apereo.cas.ticket.UniqueTicketIdGeneratorConfigurer;
import org.apereo.cas.util.CollectionUtils;
import org.apereo.cas.util.ServiceTicketIdGenerator;

import org.apache.commons.lang3.tuple.Pair;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * This is {@link OpenIdUniqueTicketIdGeneratorConfiguration}.
 *
 * @author Misagh Moayyed
 * @deprecated 6.2
 * @since 5.1.0
 */
@Configuration(value = "openIdUniqueTicketIdGeneratorConfiguration", proxyBeanMethods = false)
@EnableConfigurationProperties(CasConfigurationProperties.class)
@Deprecated(since = "6.2.0")
public class OpenIdUniqueTicketIdGeneratorConfiguration {
    @Autowired
    private CasConfigurationProperties casProperties;

    @Bean
    public UniqueTicketIdGeneratorConfigurer openIdUniqueTicketIdGeneratorConfigurer() {
        return () -> CollectionUtils.wrap(Pair.of(OpenIdService.class.getCanonicalName(),
            new ServiceTicketIdGenerator(
                casProperties.getTicket().getSt().getMaxLength(),
                casProperties.getHost().getName())));
    }

}
