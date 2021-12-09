package org.apereo.cas.config;

import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.metadata.CasConfigurationMetadataRepository;
import org.apereo.cas.metadata.rest.CasConfigurationMetadataServerEndpoint;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.actuate.autoconfigure.endpoint.condition.ConditionalOnAvailableEndpoint;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.ScopedProxyMode;

/**
 * This is {@link CasCoreConfigurationMetadataConfiguration}.
 *
 * @author Misagh Moayyed
 * @since 5.2.0
 */
@Configuration(value = "CasCoreConfigurationMetadataConfiguration", proxyBeanMethods = false)
@EnableConfigurationProperties(CasConfigurationProperties.class)
public class CasCoreConfigurationMetadataConfiguration {
    @Bean
    @ConditionalOnAvailableEndpoint
    public CasConfigurationMetadataServerEndpoint configurationMetadataServerEndpoint(
        @Qualifier("casConfigurationMetadataRepository")
        final CasConfigurationMetadataRepository casConfigurationMetadataRepository,
        final CasConfigurationProperties casProperties) {
        return new CasConfigurationMetadataServerEndpoint(casProperties, casConfigurationMetadataRepository);
    }

    @Bean
    @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
    public CasConfigurationMetadataRepository casConfigurationMetadataRepository() {
        return new CasConfigurationMetadataRepository();
    }
}
