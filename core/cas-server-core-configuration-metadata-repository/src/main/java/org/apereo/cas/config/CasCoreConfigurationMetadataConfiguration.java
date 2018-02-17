package org.apereo.cas.config;

import lombok.extern.slf4j.Slf4j;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.metadata.CasConfigurationMetadataRepository;
import org.apereo.cas.metadata.rest.CasConfigurationMetadataServerEndpoint;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.actuate.autoconfigure.endpoint.condition.ConditionalOnEnabledEndpoint;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * This is {@link CasCoreConfigurationMetadataConfiguration}.
 *
 * @author Misagh Moayyed
 * @since 5.2.0
 */
@Configuration("casCoreConfigurationMetadataConfiguration")
@EnableConfigurationProperties(CasConfigurationProperties.class)
@Slf4j
public class CasCoreConfigurationMetadataConfiguration {
    @Autowired
    private CasConfigurationProperties casProperties;

    @Bean
    @ConditionalOnWebApplication
    @ConditionalOnEnabledEndpoint
    public CasConfigurationMetadataServerEndpoint casConfigurationMetadataServerController() {
        return new CasConfigurationMetadataServerEndpoint(casConfigurationMetadataRepository(), casProperties);
    }

    @Bean
    public CasConfigurationMetadataRepository casConfigurationMetadataRepository() {
        return new CasConfigurationMetadataRepository();
    }
}
