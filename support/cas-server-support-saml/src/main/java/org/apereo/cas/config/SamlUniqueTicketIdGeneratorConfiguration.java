package org.apereo.cas.config;

import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.configuration.features.CasFeatureModule;
import org.apereo.cas.support.saml.authentication.principal.SamlService;
import org.apereo.cas.support.saml.util.SamlCompliantUniqueTicketIdGenerator;
import org.apereo.cas.ticket.UniqueTicketIdGenerator;
import org.apereo.cas.ticket.UniqueTicketIdGeneratorConfigurer;
import org.apereo.cas.util.CollectionUtils;
import org.apereo.cas.util.spring.boot.ConditionalOnFeatureEnabled;

import lombok.val;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.ScopedProxyMode;

/**
 * This is {@link SamlUniqueTicketIdGeneratorConfiguration}.
 *
 * @author Misagh Moayyed
 * @since 5.1.0
 */
@EnableConfigurationProperties(CasConfigurationProperties.class)
@ConditionalOnFeatureEnabled(feature = CasFeatureModule.FeatureCatalog.SAML)
@Configuration(value = "SamlUniqueTicketIdGeneratorConfiguration", proxyBeanMethods = false)
class SamlUniqueTicketIdGeneratorConfiguration {

    @Bean
    @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
    public UniqueTicketIdGenerator samlServiceTicketUniqueIdGenerator(final CasConfigurationProperties casProperties) {
        val gen = new SamlCompliantUniqueTicketIdGenerator(casProperties.getServer().getName());
        gen.setSaml2compliant(casProperties.getSamlCore().isTicketidSaml2());
        return gen;
    }

    @Bean
    @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
    public UniqueTicketIdGeneratorConfigurer samlServiceTicketUniqueTicketIdGeneratorConfigurer(
        @Qualifier("samlServiceTicketUniqueIdGenerator")
        final UniqueTicketIdGenerator samlServiceTicketUniqueIdGenerator) {
        return () -> CollectionUtils.wrap(Pair.of(SamlService.class.getCanonicalName(), samlServiceTicketUniqueIdGenerator));
    }
}
