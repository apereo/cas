package org.apereo.cas.config;

import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.metadata.server.CasConfigurationMetadataServerController;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * This is {@link CasCoreMetadataServerConfiguration}.
 *
 * @author Misagh Moayyed
 * @since 5.2.0
 */
@Configuration("casLoggingConfiguration")
@EnableConfigurationProperties(CasConfigurationProperties.class)
public class CasCoreMetadataServerConfiguration {
    
    @Bean
    public CasConfigurationMetadataServerController casConfigurationMetadataServerController() {
        return new CasConfigurationMetadataServerController();
    }
}
