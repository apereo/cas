package org.apereo.cas.config;

import org.apereo.cas.CentralAuthenticationService;
import org.apereo.cas.CentralAuthenticationServiceImpl;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * This is {@link CasCoreConfiguration}.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 */
@Configuration("casCoreConfiguration")
public class CasCoreConfiguration {

    /**
     * Central authentication service implementation.
     *
     * @return the central authentication service
     */
    @Bean
    public CentralAuthenticationService centralAuthenticationService() {
        return new CentralAuthenticationServiceImpl();
    }
}
