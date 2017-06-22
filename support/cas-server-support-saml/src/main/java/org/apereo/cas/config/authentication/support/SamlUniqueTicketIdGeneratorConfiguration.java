package org.apereo.cas.config.authentication.support;

import org.apache.commons.lang3.tuple.Pair;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.support.saml.authentication.principal.SamlService;
import org.apereo.cas.support.saml.util.SamlCompliantUniqueTicketIdGenerator;
import org.apereo.cas.ticket.UniqueTicketIdGenerator;
import org.apereo.cas.ticket.UniqueTicketIdGeneratorConfigurer;
import org.apereo.cas.util.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Collection;

/**
 * This is {@link SamlUniqueTicketIdGeneratorConfiguration}.
 *
 * @author Misagh Moayyed
 * @since 5.1.0
 */
@Configuration("samlUniqueTicketIdGeneratorConfiguration")
@EnableConfigurationProperties(CasConfigurationProperties.class)
public class SamlUniqueTicketIdGeneratorConfiguration implements UniqueTicketIdGeneratorConfigurer {
    @Autowired
    private CasConfigurationProperties casProperties;

    @Bean
    public UniqueTicketIdGenerator samlServiceTicketUniqueIdGenerator() {
        final SamlCompliantUniqueTicketIdGenerator gen = new SamlCompliantUniqueTicketIdGenerator(casProperties.getServer().getName());
        gen.setSaml2compliant(casProperties.getSamlCore().isTicketidSaml2());
        return gen;
    }

    @Override
    public Collection<Pair<String, UniqueTicketIdGenerator>> buildUniqueTicketIdGenerators() {
        return CollectionUtils.wrap(Pair.of(SamlService.class.getCanonicalName(), samlServiceTicketUniqueIdGenerator()));
    }
}
