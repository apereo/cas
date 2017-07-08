package org.apereo.cas.support.pac4j.web.config;

import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.support.pac4j.web.controllers.DelegatedClientEndpointController;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;

/**
 * Endpoint configuration
 *
 * @author Ghenadii Batalski
 * @since 5.2.0
 */
@Configuration("delegatedClientEndpointConfiguration")
@EnableConfigurationProperties(CasConfigurationProperties.class)
public class DelegatedClientEndpointConfiguration {

    @RefreshScope
    @Bean
    @Lazy
    public DelegatedClientEndpointController createDelegatedClientEndpointController(){
         return new DelegatedClientEndpointController();
    }
}
