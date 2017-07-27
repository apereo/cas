package org.apereo.cas.config;

import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.metadata.server.CasConfigurationMetadataRepository;
import org.apereo.cas.metadata.server.rest.CasConfigurationMetadataServerController;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * This is {@link CasCoreMetadataServerConfiguration}.
 *
 * @author Misagh Moayyed
 * @since 5.2.0
 */
@Configuration("casCoreMetadataServerConfiguration")
@EnableConfigurationProperties(CasConfigurationProperties.class)
public class CasCoreMetadataServerConfiguration {
    
    @Bean
    public CasConfigurationMetadataServerController casConfigurationMetadataServerController() {
        return new CasConfigurationMetadataServerController(casConfigurationMetadataRepository());
    }

    @Bean
    public CasConfigurationMetadataRepository casConfigurationMetadataRepository() {
        return new CasConfigurationMetadataRepository();
    }
}
