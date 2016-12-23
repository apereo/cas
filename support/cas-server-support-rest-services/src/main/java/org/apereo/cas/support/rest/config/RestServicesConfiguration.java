package org.apereo.cas.support.rest.config;

import org.apereo.cas.CentralAuthenticationService;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.services.ServicesManager;
import org.apereo.cas.support.rest.RegisteredServiceResource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * This is {@link RestServicesConfiguration}.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 */
@Configuration("restServicesConfiguration")
@EnableConfigurationProperties(CasConfigurationProperties.class)
public class RestServicesConfiguration {

    @Autowired
    @Qualifier("centralAuthenticationService")
    private CentralAuthenticationService centralAuthenticationService;

    @Autowired
    @Qualifier("servicesManager")
    private ServicesManager servicesManager;

    @Autowired
    private CasConfigurationProperties casProperties;

    @Bean
    public RegisteredServiceResource registeredServiceResourceRestController() {
        return new RegisteredServiceResource(servicesManager, centralAuthenticationService, casProperties.getRest().getAttributeName(),
                casProperties.getRest().getAttributeValue());
    }
}



