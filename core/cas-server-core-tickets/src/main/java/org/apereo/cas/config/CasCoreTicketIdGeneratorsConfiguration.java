package org.apereo.cas.config;

import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.configuration.features.CasFeatureModule;
import org.apereo.cas.ticket.UniqueTicketIdGenerator;
import org.apereo.cas.ticket.UniqueTicketIdGeneratorConfigurer;
import org.apereo.cas.util.spring.boot.ConditionalOnFeatureEnabled;

import lombok.val;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.ScopedProxyMode;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * This is {@link CasCoreTicketIdGeneratorsConfiguration}.
 *
 * @author Misagh Moayyed
 * @since 5.1.0
 */
@EnableConfigurationProperties(CasConfigurationProperties.class)
@ConditionalOnFeatureEnabled(feature = CasFeatureModule.FeatureCatalog.TicketRegistry)
@Configuration(value = "CasCoreTicketIdGeneratorsConfiguration", proxyBeanMethods = false)
class CasCoreTicketIdGeneratorsConfiguration {

    @Bean
    @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
    @ConditionalOnMissingBean(name = "uniqueIdGeneratorsMap")
    public Map<String, UniqueTicketIdGenerator> uniqueIdGeneratorsMap(
        final List<UniqueTicketIdGeneratorConfigurer> configurers) {
        val map = new HashMap<String, UniqueTicketIdGenerator>();
        configurers.forEach(cfg -> {
            val pair = cfg.buildUniqueTicketIdGenerators();
            pair.forEach(p -> map.put(p.getKey(), p.getValue()));
        });
        return map;
    }
}
