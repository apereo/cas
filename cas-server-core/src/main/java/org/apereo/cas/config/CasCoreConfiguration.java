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
    
    @Bean(name="centralAuthenticationService")
    public CentralAuthenticationService centralAuthenticationService() {
        return new CentralAuthenticationServiceImpl();
    }
}
