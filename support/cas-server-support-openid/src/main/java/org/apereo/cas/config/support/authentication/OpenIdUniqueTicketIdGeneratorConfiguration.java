package org.apereo.cas.config.support.authentication;

import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.support.openid.authentication.principal.OpenIdService;
import org.apereo.cas.ticket.UniqueTicketIdGeneratorConfigurer;
import org.apereo.cas.util.CollectionUtils;
import org.apereo.cas.util.ServiceTicketIdGenerator;

import org.apache.commons.lang3.tuple.Pair;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.ScopedProxyMode;

/**
 * This is {@link OpenIdUniqueTicketIdGeneratorConfiguration}.
 *
 * @author Misagh Moayyed
 * @since 5.1.0
 * @deprecated 6.2
 */
@Configuration(value = "OpenIdUniqueTicketIdGeneratorConfiguration", proxyBeanMethods = false)
@EnableConfigurationProperties(CasConfigurationProperties.class)
@Deprecated(since = "6.2.0")
public class OpenIdUniqueTicketIdGeneratorConfiguration {

    @Bean
    @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
    public UniqueTicketIdGeneratorConfigurer openIdUniqueTicketIdGeneratorConfigurer(final CasConfigurationProperties casProperties) {
        return () -> CollectionUtils.wrap(
            Pair.of(OpenIdService.class.getCanonicalName(), new ServiceTicketIdGenerator(casProperties.getTicket().getSt().getMaxLength(), casProperties.getHost().getName())));
    }
}
