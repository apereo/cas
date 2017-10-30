package org.apereo.cas.config;

import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.discovery.CasServerProfileRegistrar;
import org.apereo.cas.services.ServicesManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * This is {@link CasDiscoveryProfileConfiguration}.
 *
 * @author Misagh Moayyed
 * @since 5.2.0
 */
@Configuration("casDiscoveryProfileConfiguration")
@EnableConfigurationProperties(CasConfigurationProperties.class)
public class CasDiscoveryProfileConfiguration {

    @Autowired
    @Qualifier("servicesManager")
    private ServicesManager servicesManager;
    
    @Autowired
    private CasConfigurationProperties casProperties;

    @Autowired
    private ApplicationContext applicationContext;
    
    @Bean
    public CasServerProfileRegistrar casServerProfileRegistrar() {
        return new CasServerProfileRegistrar(this.servicesManager);
    }
}
